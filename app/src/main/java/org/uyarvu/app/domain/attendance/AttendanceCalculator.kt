package org.uyarvu.app.domain.attendance

import kotlin.math.ceil
import kotlin.math.floor

enum class EligibilityCategory {
    REGULAR_ELIGIBLE,
    CONDONATION_ELIGIBLE,
    MEDICAL_CONDONATION_ELIGIBLE,
    INSUFFICIENT_ATTENDANCE,
    NO_CLASSES_CONDUCTED
}

data class AttendanceEvaluation(
    val conductedClasses: Int,
    val attendedClasses: Int,
    val percentage: Double,
    val category: EligibilityCategory,
    val safeAbsenceAllowance: Int,
    val classesNeededForRegular: Int,
    val isRegularTargetAchievable: Boolean
)

object AttendanceCalculator {
    fun evaluate(
        attended: Int,
        conducted: Int,
        remainingPlannedClasses: Int,
        regularThresholdPct: Double = 75.0,
        condonationFloorPct: Double = 65.0,
        medicalFloorPct: Double = 60.0
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
                isRegularTargetAchievable = true
            )
        }

        val pct = (attended.toDouble() / conducted.toDouble()) * 100.0
        val category = when {
            pct >= regularThresholdPct -> EligibilityCategory.REGULAR_ELIGIBLE
            pct >= condonationFloorPct -> EligibilityCategory.CONDONATION_ELIGIBLE
            pct >= medicalFloorPct -> EligibilityCategory.MEDICAL_CONDONATION_ELIGIBLE
            else -> EligibilityCategory.INSUFFICIENT_ATTENDANCE
        }

        val targetRatio = regularThresholdPct / 100.0
        val safeAllowance = if (pct >= regularThresholdPct) {
            floor((attended / targetRatio) - conducted).toInt().coerceAtLeast(0)
        } else 0

        val neededForRegular = if (pct < regularThresholdPct) {
            ceil(((targetRatio * conducted) - attended) / (1.0 - targetRatio)).toInt().coerceAtLeast(0)
        } else 0

        val maxPossiblePct = ((attended + remainingPlannedClasses).toDouble() /
            (conducted + remainingPlannedClasses).toDouble()) * 100.0

        return AttendanceEvaluation(
            conductedClasses = conducted,
            attendedClasses = attended,
            percentage = pct,
            category = category,
            safeAbsenceAllowance = safeAllowance,
            classesNeededForRegular = neededForRegular,
            isRegularTargetAchievable = maxPossiblePct >= regularThresholdPct
        )
    }
}
