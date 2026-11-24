package org.uyarvu.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.uyarvu.app.domain.attendance.AttendanceCalculator
import org.uyarvu.app.domain.attendance.EligibilityCategory

class AttendanceCalculatorTest {
    @Test
    fun zeroClasses_returnsNoClassesConducted() {
        val result = AttendanceCalculator.evaluate(attended = 0, conducted = 0, remainingPlannedClasses = 60)
        assertEquals(EligibilityCategory.NO_CLASSES_CONDUCTED, result.category)
        assertTrue(result.isRegularTargetAchievable)
    }

    @Test
    fun condonationZone_computesExactRecoveryClasses() {
        val result = AttendanceCalculator.evaluate(attended = 21, conducted = 30, remainingPlannedClasses = 20)
        assertEquals(EligibilityCategory.CONDONATION_ELIGIBLE, result.category)
        assertEquals(6, result.classesNeededForRegular)
        assertTrue(result.isRegularTargetAchievable)
    }

    @Test
    fun mathematicallyImpossibleRecovery_flagsUnachievable() {
        val result = AttendanceCalculator.evaluate(attended = 10, conducted = 40, remainingPlannedClasses = 5)
        assertEquals(EligibilityCategory.INSUFFICIENT_ATTENDANCE, result.category)
        assertFalse(result.isRegularTargetAchievable)
    }
}
