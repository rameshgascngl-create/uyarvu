package org.uyarvu.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.uyarvu.app.domain.attendance.AttendanceCalculator
import org.uyarvu.app.domain.attendance.EligibilityCategory
import org.uyarvu.app.domain.attendance.InstitutionalAttendancePolicy

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
    fun condonationZone_computesExactRecoveryClasses() {
        // 21/30 = 70% -> x = ceil((0.75*30 - 21)/0.25) = ceil(1.5/0.25) = 6
        val result = AttendanceCalculator.evaluate(attended = 21, conducted = 30, remainingPlannedClasses = 20)
        assertEquals(EligibilityCategory.CONDONATION_ELIGIBLE, result.category)
        assertEquals(6, result.classesNeededForRegular)
        assertTrue(result.isRegularTargetAchievable)
    }

    @Test
    fun medicalCondonationZone_between50And65() {
        val result = AttendanceCalculator.evaluate(attended = 12, conducted = 20, remainingPlannedClasses = 40)
        assertEquals(EligibilityCategory.MEDICAL_CONDONATION_ELIGIBLE, result.category)
    }

    @Test
    fun semesterRepeatRisk_below50() {
        val result = AttendanceCalculator.evaluate(attended = 9, conducted = 20, remainingPlannedClasses = 40)
        assertEquals(EligibilityCategory.SEMESTER_REPEAT_RISK, result.category)
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
