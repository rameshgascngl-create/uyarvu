package org.uyarvu.app.domain.planner

enum class RecoverySituation {
    BEHIND_SYLLABUS,
    STRUGGLING_SUBJECT,
    ARREAR_EXAMINATION,
    WORKLOAD_DIFFICULTY,
    LIMITED_TIME
}

data class RecoveryPlan(
    val strategyTitle: String,
    val dailyTargetMinutes: Int,
    val recommendedSteps: List<String>,
    val recommendTeacherAssistance: Boolean
)

object AcademicRecoveryAssistant {
    fun buildPlan(
        situation: RecoverySituation,
        daysRemaining: Int,
        weakUnitsCount: Int,
        availableDailyMinutes: Int
    ): RecoveryPlan {
        val safeMinutes = availableDailyMinutes.coerceIn(30, 300)
        val recommendTeacher = weakUnitsCount >= 4 || daysRemaining <= 7 || situation == RecoverySituation.ARREAR_EXAMINATION

        val steps = mutableListOf<String>()
        when {
            daysRemaining <= 7 -> {
                steps.add("Focus only on foundational concepts and high-weightage repeated topics.")
                steps.add("Use 25-minute active recall blocks followed by 5-minute error review.")
            }
            daysRemaining <= 15 -> {
                steps.add("Split weak units across the first 10 days; reserve final 5 days for timed self-tests.")
            }
            else -> {
                steps.add("Cover 1 foundational unit every 2 days with spaced recall checks.")
            }
        }

        if (recommendTeacher) {
            steps.add("Schedule a 10-minute clarification meeting with your subject teacher on core concepts.")
        }

        return RecoveryPlan(
            strategyTitle = "Constructive ${daysRemaining.coerceAtLeast(1)}-Day Recovery Plan",
            dailyTargetMinutes = safeMinutes,
            recommendedSteps = steps,
            recommendTeacherAssistance = recommendTeacher
        )
    }
}
