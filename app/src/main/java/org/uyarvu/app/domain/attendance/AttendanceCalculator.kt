package org.uyarvu.app.domain.attendance

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

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
    private val HUNDRED = BigDecimal(100)
    private val MC = MathContext(16, RoundingMode.HALF_UP)

    /**
     * Cancelled and not-yet-conducted classes are excluded from the conducted-class denominator.
     * All eligibility comparisons, recovery counts, and safe allowances use exact BigDecimal
     * arithmetic so IEEE-754 binary float representation never skews boundary classifications.
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

        val bdAttended = BigDecimal(attended)
        val bdConducted = BigDecimal(conducted)
        val bdRegularPct = BigDecimal.valueOf(policy.regularThresholdPct)
        val bdCondonationPct = BigDecimal.valueOf(policy.condonationFloorPct)
        val bdMedicalPct = BigDecimal.valueOf(policy.medicalCondonationFloorPct)

        // Exact cross-multiplied comparisons: attended * 100 >= thresholdPct * conducted
        val attendedTimes100 = bdAttended.multiply(HUNDRED)
        val regularReq = bdRegularPct.multiply(bdConducted)
        val condonationReq = bdCondonationPct.multiply(bdConducted)
        val medicalReq = bdMedicalPct.multiply(bdConducted)

        val category = when {
            attendedTimes100.compareTo(regularReq) >= 0 -> EligibilityCategory.NORMAL_ELIGIBLE
            attendedTimes100.compareTo(condonationReq) >= 0 -> EligibilityCategory.CONDONATION_ELIGIBLE
            attendedTimes100.compareTo(medicalReq) >= 0 -> EligibilityCategory.MEDICAL_CONDONATION_ELIGIBLE
            else -> EligibilityCategory.SEMESTER_REPEAT_RISK
        }

        val p = bdRegularPct.divide(HUNDRED, MC)
        val oneMinusP = BigDecimal.ONE.subtract(p)

        val safeAllowance = if (category == EligibilityCategory.NORMAL_ELIGIBLE) {
            val maxConducted = bdAttended.divide(p, 10, RoundingMode.FLOOR)
            maxConducted.subtract(bdConducted).setScale(0, RoundingMode.FLOOR).toInt().coerceAtLeast(0)
        } else 0

        val neededForRegular = if (category != EligibilityCategory.NORMAL_ELIGIBLE) {
            val numerator = p.multiply(bdConducted).subtract(bdAttended)
            if (numerator.compareTo(BigDecimal.ZERO) <= 0) 0
            else numerator.divide(oneMinusP, 0, RoundingMode.CEILING).toInt().coerceAtLeast(0)
        } else 0

        val totalAtEnd = conducted + remainingPlannedClasses
        val maxPossibleAtEnd = attended + remainingPlannedClasses
        val isAchievable = if (totalAtEnd == 0) {
            true
        } else {
            BigDecimal(maxPossibleAtEnd).multiply(HUNDRED)
                .compareTo(bdRegularPct.multiply(BigDecimal(totalAtEnd))) >= 0
        }

        val pctDecimal = attendedTimes100.divide(bdConducted, 6, RoundingMode.HALF_UP)
        val projectedEnd = if (totalAtEnd == 0) {
            pctDecimal.toDouble()
        } else {
            val expectedAdditional = pctDecimal.divide(HUNDRED, MC).multiply(BigDecimal(remainingPlannedClasses))
            bdAttended.add(expectedAdditional)
                .divide(BigDecimal(totalAtEnd), 6, RoundingMode.HALF_UP)
                .multiply(HUNDRED)
                .toDouble()
        }

        val pctDouble = pctDecimal.toDouble()
        val decliningWarning = recentConsecutiveAbsences >= 2 ||
            (category == EligibilityCategory.NORMAL_ELIGIBLE &&
                pctDecimal.compareTo(bdRegularPct.add(BigDecimal(3))) <= 0)

        return AttendanceEvaluation(
            conductedClasses = conducted,
            attendedClasses = attended,
            percentage = pctDouble,
            category = category,
            safeAbsenceAllowance = safeAllowance,
            classesNeededForRegular = neededForRegular,
            isRegularTargetAchievable = isAchievable,
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
