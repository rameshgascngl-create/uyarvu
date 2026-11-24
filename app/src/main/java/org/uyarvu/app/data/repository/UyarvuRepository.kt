package org.uyarvu.app.data.repository

import kotlinx.coroutines.flow.Flow
import org.uyarvu.app.data.local.dao.AttendanceDao
import org.uyarvu.app.data.local.dao.CourseDao
import org.uyarvu.app.data.local.dao.StudentProfileDao
import org.uyarvu.app.data.local.dao.TimetableDao
import org.uyarvu.app.data.local.entity.AttendanceLogEntity
import org.uyarvu.app.data.local.entity.CourseEntity
import org.uyarvu.app.data.local.entity.StudentProfileEntity
import org.uyarvu.app.data.local.entity.TimetableSlotEntity

class UyarvuRepository(
    private val profileDao: StudentProfileDao,
    private val courseDao: CourseDao,
    private val timetableDao: TimetableDao,
    private val attendanceDao: AttendanceDao
) {
    fun observeProfile(): Flow<StudentProfileEntity?> = profileDao.observeProfile()
    fun observeActiveCourses(semester: Int): Flow<List<CourseEntity>> = courseDao.observeActiveCourses(semester)
    fun observeAllCoursesHistory(): Flow<List<CourseEntity>> = courseDao.observeAllCoursesIncludingHistory()
    fun observeDaySchedule(semester: Int, dayOfWeek: Int): Flow<List<TimetableSlotEntity>> =
        timetableDao.observeDaySchedule(semester, dayOfWeek)
    fun observeLogsForCourse(courseId: Long): Flow<List<AttendanceLogEntity>> =
        attendanceDao.observeLogsForCourse(courseId)

    suspend fun saveProfile(profile: StudentProfileEntity) = profileDao.upsertProfile(profile)

    /**
     * Advances semester while preserving all historical courses and attendance records.
     */
    suspend fun advanceToNextSemester(newSemester: Int) {
        courseDao.archivePriorSemesters(newSemester)
        profileDao.updateSemester(newSemester)
    }

    /**
     * Inserts attendance while enforcing unique (course, date, hour, sessionType) constraint.
     * Returns true if inserted, false if duplicate was prevented.
     */
    suspend fun logAttendanceOnce(log: AttendanceLogEntity): Boolean {
        return attendanceDao.insertLogPreventDuplicate(log) != -1L
    }

    suspend fun deleteAllLocalData() {
        profileDao.clearProfile()
    }
}
