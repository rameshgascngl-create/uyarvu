package org.uyarvu.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.uyarvu.app.domain.planner.AcademicRecoveryAssistant
import org.uyarvu.app.domain.planner.PlannableTask
import org.uyarvu.app.domain.planner.RecoverySituation
import org.uyarvu.app.domain.planner.WorkloadBalancer

class WorkloadBalancerTest {
    @Test
    fun prioritizesUrgentLowConfidenceTasksAndRespectsSleepCap() {
        val tasks = listOf(
            PlannableTask(id = 1, title = "Far Task", estimatedMinutes = 120, daysUntilDue = 14, priorityWeight = 1),
            PlannableTask(id = 2, title = "Urgent Exam Unit", estimatedMinutes = 90, daysUntilDue = 2, priorityWeight = 3, lowConfidence = true)
        )
        val plan = WorkloadBalancer.buildDailyPlan(tasks, availableMinutes = 600)
        assertTrue(plan.blocks.isNotEmpty())
        assertEquals(2L, plan.blocks.first().taskId)
        assertTrue(plan.totalPlannedMinutes <= 360)
    }

    @Test
    fun overloadedSchedule_reportsUnscheduledMinutesTransparently() {
        val tasks = listOf(
            PlannableTask(id = 1, title = "Heavy Unit", estimatedMinutes = 200, daysUntilDue = 1, priorityWeight = 3)
        )
        val plan = WorkloadBalancer.buildDailyPlan(tasks, availableMinutes = 90)
        assertTrue(plan.isOverloaded)
        assertTrue(plan.unscheduledMinutes > 0)
    }

    @Test
    fun recoveryAssistant_recommendsTeacherForArrearsOrShortTime() {
        val plan = AcademicRecoveryAssistant.buildPlan(
            situation = RecoverySituation.ARREAR_EXAMINATION,
            daysRemaining = 7,
            weakUnitsCount = 5,
            availableDailyMinutes = 120
        )
        assertTrue(plan.recommendTeacherAssistance)
        assertEquals(120, plan.dailyTargetMinutes)
    }
}
