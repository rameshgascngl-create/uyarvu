package org.uyarvu.app.domain.attendance

import kotlin.math.ceil
import kotlin.math.floor

enum class EligibilityCategory {
    NORMAL_ELIGIBLE,
    CONDONATION_ELIGIBLE,
    MEDICAL_CONDONATION_ELIGIBLE,
    SEMESTER_REPEAT_RISK,
    NO_CLASSES_CONDUCTED
}

data class InstitutionalAttendancePolicy(
    val regularThresholdPct: Double = 75.0,
    val condonationFloorPct: Double = 65.0,
    val medicalCondonationFloorPct: Double = 50.0,
    val regulationSource: String = "Configurable institutional preset (initial TN college defaults)"
) {
    init {
        require(regularThresholdPct in 1.0..99.9) { "Regular threshold must be in (1.0, 99.9]" }
        require(condonationFloorPct in 0.0..regularThresholdPct) { "Invalid condonation floor" }
        require(medicalCondonationFloorPct in 0.0..condonationFloorPct) { "Invalid medical floor" }
    }
}

data class AttendanceEvaluation(
    val conductedClasses: Int,
    val attendedClasses: Int,
    val percentage: Double,
    val category: EligibilityCategory,
    val safeAbsenceAllowance: Int,
    val classesNeededForRegular: Int,
    val isRegularTargetAchievable: Boolean,
    val projectedSemesterEndPct: Double,
    val isDecliningWarning: Boolean
)

data class SubjectAttendanceInput(
    val courseId: Long,
    val attended: Int,
    val absent: Int,
    val cancelled: Int = 0,
    val notRecorded: Int = 0,
    val remainingPlannedClasses: Int = 0
)

object AttendanceCalculator {
    /**
     * Cancelled and not-yet-conducted classes are excluded from the conducted-class denominator.
     * Percentages are never silently rounded upwards for eligibility classification.
     */
    fun evaluate(
        attended: Int,
        conducted: Int,
        remainingPlannedClasses: Int,
        policy: InstitutionalAttendancePolicy = InstitutionalAttendancePolicy(),
        recentConsecutiveAbsences: Int = 0
    ): AttendanceEvaluation {
        require(attended >= 0 && conducted >= 0 && attended <= conducted) {
            "Invalid attendance counts: attended=$attended, conducted=$conducted"
        }
        require(remainingPlannedClasses >= 0) { "Remaining classes cannot be negative" }

        if (conducted == 0) {
            return AttendanceEvaluation(
                conductedClasses = 0,
                attendedClasses = 0,
                percentage = 0.0,
                category = EligibilityCategory.NO_CLASSES_CONDUCTED,
                safeAbsenceAllowance = 0,
                classesNeededForRegular = 0,
                isRegularTargetAchievable = true,
                projectedSemesterEndPct = 100.0,
                isDecliningWarning = false
            )
        }

        val pct = (attended.toDouble() / conducted.toDouble()) * 100.0
        val category = when {
            pct >= policy.regularThresholdPct -> EligibilityCategory.NORMAL_ELIGIBLE
            pct >= policy.condonationFloorPct -> EligibilityCategory.CONDONATION_ELIGIBLE
            pct >= policy.medicalCondonationFloorPct -> EligibilityCategory.MEDICAL_CONDONATION_ELIGIBLE
            else -> EligibilityCategory.SEMESTER_REPEAT_RISK
        }

        val p = policy.regularThresholdPct / 100.0
        val safeAllowance = if (pct >= policy.regularThresholdPct) {
            floor((attended / p) - conducted).toInt().coerceAtLeast(0)
        } else 0

        val neededForRegular = if (pct < policy.regularThresholdPct) {
            ceil(((p * conducted) - attended) / (1.0 - p)).toInt().coerceAtLeast(0)
        } else 0

        val totalAtEnd = conducted + remainingPlannedClasses
        val maxPossibleAtEnd = attended + remainingPlannedClasses
        val maxPossiblePct = if (totalAtEnd == 0) 100.0 else (maxPossibleAtEnd.toDouble() / totalAtEnd) * 100.0

        val projectedEnd = if (totalAtEnd == 0) pct else {
            val expectedAdditional = (pct / 100.0) * remainingPlannedClasses
            ((attended + expectedAdditional) / totalAtEnd) * 100.0
        }

        val decliningWarning = recentConsecutiveAbsences >= 2 ||
            (pct in policy.regularThresholdPct..(policy.regularThresholdPct + 3.0))

        return AttendanceEvaluation(
            conductedClasses = conducted,
            attendedClasses = attended,
            percentage = pct,
            category = category,
            safeAbsenceAllowance = safeAllowance,
            classesNeededForRegular = neededForRegular,
            isRegularTargetAchievable = maxPossiblePct >= policy.regularThresholdPct,
            projectedSemesterEndPct = projectedEnd,
            isDecliningWarning = decliningWarning
        )
    }

    fun evaluateSubject(
        input: SubjectAttendanceInput,
        policy: InstitutionalAttendancePolicy = InstitutionalAttendancePolicy(),
        recentConsecutiveAbsences: Int = 0
    ): AttendanceEvaluation {
        val conducted = input.attended + input.absent
        return evaluate(
            attended = input.attended,
            conducted = conducted,
            remainingPlannedClasses = input.remainingPlannedClasses,
            policy = policy,
            recentConsecutiveAbsences = recentConsecutiveAbsences
        )
    }

    fun evaluateAggregate(
        subjects: List<SubjectAttendanceInput>,
        policy: InstitutionalAttendancePolicy = InstitutionalAttendancePolicy()
    ): AttendanceEvaluation {
        val totalAttended = subjects.sumOf { it.attended }
        val totalConducted = subjects.sumOf { it.attended + it.absent }
        val totalRemaining = subjects.sumOf { it.remainingPlannedClasses }
        return evaluate(
            attended = totalAttended,
            conducted = totalConducted,
            remainingPlannedClasses = totalRemaining,
            policy = policy
        )
    }
}
