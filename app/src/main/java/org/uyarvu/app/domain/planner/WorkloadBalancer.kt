package org.uyarvu.app.domain.planner

import kotlin.math.min

data class PlannableTask(
    val id: Long,
    val title: String,
    val estimatedMinutes: Int,
    val daysUntilDue: Int,
    val priorityWeight: Int, // 1..3
    val difficultyWeight: Int = 3, // 1..5
    val lowConfidence: Boolean = false,
    val needsRevision: Boolean = false
)

data class BalancedStudyBlock(
    val taskId: Long,
    val title: String,
    val allocatedMinutes: Int,
    val restMinutesAfter: Int,
    val priorityScore: Double,
    val explanation: String
)

data class WorkloadPlanResult(
    val blocks: List<BalancedStudyBlock>,
    val totalPlannedMinutes: Int,
    val isOverloaded: Boolean,
    val unscheduledMinutes: Int,
    val sleepProtectedNotice: String
)

object WorkloadBalancer {
    private const val MAX_SINGLE_BLOCK_MINUTES = 50
    private const val BREAK_MINUTES = 10
    private const val MAX_SAFE_DAILY_STUDY_MINUTES = 360 // Protects sleep & personal time

    /**
     * Deterministically allocates study blocks within student-defined availability,
     * capping daily study to protect sleep and inserting rest breaks.
     */
    fun buildDailyPlan(
        tasks: List<PlannableTask>,
        availableMinutes: Int
    ): WorkloadPlanResult {
        val safeBudget = min(availableMinutes.coerceAtLeast(0), MAX_SAFE_DAILY_STUDY_MINUTES)
        val scored = tasks.map { task ->
            val urgency = when {
                task.daysUntilDue <= 0 -> 50.0
                task.daysUntilDue <= 3 -> 35.0
                task.daysUntilDue <= 7 -> 20.0
                else -> 10.0 / task.daysUntilDue.coerceAtLeast(1)
            }
            val score = urgency +
                (task.priorityWeight * 8.0) +
                (task.difficultyWeight * 2.5) +
                (if (task.lowConfidence) 6.0 else 0.0) +
                (if (task.needsRevision) 5.0 else 0.0)
            task to score
        }.sortedByDescending { it.second }

        val blocks = mutableListOf<BalancedStudyBlock>()
        var remainingBudget = safeBudget
        var totalRequested = tasks.sumOf { it.estimatedMinutes.coerceAtLeast(0) }

        for ((task, score) in scored) {
            if (remainingBudget < 15) break
            var taskRemaining = task.estimatedMinutes.coerceAtLeast(15)
            while (taskRemaining > 0 && remainingBudget >= 15) {
                val studyChunk = minOf(taskRemaining, MAX_SINGLE_BLOCK_MINUTES, remainingBudget)
                val rest = if (remainingBudget - studyChunk >= BREAK_MINUTES) BREAK_MINUTES else 0
                val reason = buildString {
                    append("Due in ${task.daysUntilDue.coerceAtLeast(0)}d")
                    if (task.lowConfidence) append(" · low confidence")
                    if (task.needsRevision) append(" · revision due")
                }
                blocks.add(
                    BalancedStudyBlock(
                        taskId = task.id,
                        title = task.title,
                        allocatedMinutes = studyChunk,
                        restMinutesAfter = rest,
                        priorityScore = score,
                        explanation = reason
                    )
                )
                taskRemaining -= studyChunk
                remainingBudget -= (studyChunk + rest)
            }
        }

        val totalPlanned = blocks.sumOf { it.allocatedMinutes }
        val unscheduled = (totalRequested - totalPlanned).coerceAtLeast(0)
        return WorkloadPlanResult(
            blocks = blocks,
            totalPlannedMinutes = totalPlanned,
            isOverloaded = totalRequested > safeBudget,
            unscheduledMinutes = unscheduled,
            sleepProtectedNotice = "Daily study capped at ${safeBudget}m with 10m breaks to protect sleep."
        )
    }
}
