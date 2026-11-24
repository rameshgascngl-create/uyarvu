package org.uyarvu.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.uyarvu.app.domain.attendance.AttendanceCalculator
import org.uyarvu.app.domain.attendance.EligibilityCategory
import org.uyarvu.app.domain.attendance.InstitutionalAttendancePolicy
import org.uyarvu.app.domain.attendance.SubjectAttendanceInput

class AttendanceCalculatorTest {
    @Test
    fun zeroClasses_returnsNoClassesConducted() {
        val result = AttendanceCalculator.evaluate(attended = 0, conducted = 0, remainingPlannedClasses = 60)
        assertEquals(EligibilityCategory.NO_CLASSES_CONDUCTED, result.category)
        assertTrue(result.isRegularTargetAchievable)
    }

    @Test
    fun perfectAttendance_computesSafeAbsenceAllowance() {
        val result = AttendanceCalculator.evaluate(attended = 30, conducted = 30, remainingPlannedClasses = 30)
        assertEquals(EligibilityCategory.NORMAL_ELIGIBLE, result.category)
        assertEquals(10, result.safeAbsenceAllowance)
        assertEquals(0, result.classesNeededForRegular)
    }

    @Test
    fun exactThresholdAndNoUpwardRounding_boundaryCheck() {
        // 749 / 1000 = 74.9% -> MUST NOT round up to 75%
        val below = AttendanceCalculator.evaluate(attended = 749, conducted = 1000, remainingPlannedClasses = 20)
        assertEquals(EligibilityCategory.CONDONATION_ELIGIBLE, below.category)
        assertEquals(4, below.classesNeededForRegular)

        // Exact 75.0% -> Normal eligible
        val exact = AttendanceCalculator.evaluate(attended = 75, conducted = 100, remainingPlannedClasses = 20)
        assertEquals(EligibilityCategory.NORMAL_ELIGIBLE, exact.category)
    }

    @Test
    fun cancelledClassesExcludedFromDenominator() {
        val subject = SubjectAttendanceInput(courseId = 1L, attended = 15, absent = 5, cancelled = 10, remainingPlannedClasses = 10)
        val result = AttendanceCalculator.evaluateSubject(subject)
        assertEquals(20, result.conductedClasses)
        assertEquals(75.0, result.percentage, 0.001)
        assertEquals(EligibilityCategory.NORMAL_ELIGIBLE, result.category)
    }

    @Test
    fun aggregateAttendanceAcrossSubjects() {
        val s1 = SubjectAttendanceInput(courseId = 1L, attended = 18, absent = 2)
        val s2 = SubjectAttendanceInput(courseId = 2L, attended = 12, absent = 8)
        val agg = AttendanceCalculator.evaluateAggregate(listOf(s1, s2))
        assertEquals(40, agg.conductedClasses)
        assertEquals(30, agg.attendedClasses)
        assertEquals(75.0, agg.percentage, 0.001)
    }

    @Test
    fun mathematicallyImpossibleRecovery_flagsUnachievable() {
        val result = AttendanceCalculator.evaluate(attended = 10, conducted = 40, remainingPlannedClasses = 5)
        assertEquals(EligibilityCategory.SEMESTER_REPEAT_RISK, result.category)
        assertFalse(result.isRegularTargetAchievable)
    }

    @Test
    fun customInstitutionPolicy_overridesDefaultThresholds() {
        val policy = InstitutionalAttendancePolicy(regularThresholdPct = 80.0, condonationFloorPct = 70.0, medicalCondonationFloorPct = 60.0)
        val result = AttendanceCalculator.evaluate(attended = 15, conducted = 20, remainingPlannedClasses = 20, policy = policy)
        assertEquals(EligibilityCategory.CONDONATION_ELIGIBLE, result.category)
        assertEquals(5, result.classesNeededForRegular)
    }
}
