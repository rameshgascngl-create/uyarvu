package org.uyarvu.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import org.uyarvu.app.data.local.entity.AttendanceLogEntity
import org.uyarvu.app.data.local.entity.CalendarDayOverrideEntity
import org.uyarvu.app.data.local.entity.CourseEntity
import org.uyarvu.app.data.local.entity.FlashcardEntity
import org.uyarvu.app.data.local.entity.StudentProfileEntity
import org.uyarvu.app.data.local.entity.TimetableSlotEntity

@Dao
interface StudentProfileDao {
    @Query("SELECT * FROM student_profile WHERE id = 1")
    fun observeProfile(): Flow<StudentProfileEntity?>

    @Query("SELECT * FROM student_profile WHERE id = 1")
    suspend fun getProfileOnce(): StudentProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProfile(profile: StudentProfileEntity)

    @Query("UPDATE student_profile SET currentSemester = :nextSemester WHERE id = 1")
    suspend fun updateSemester(nextSemester: Int)

    @Query("DELETE FROM student_profile")
    suspend fun clearProfile()
}

@Dao
interface CourseDao {
    @Query("SELECT * FROM courses WHERE semester = :semester AND isArchived = 0 ORDER BY code ASC")
    fun observeActiveCourses(semester: Int): Flow<List<CourseEntity>>

    @Query("SELECT * FROM courses ORDER BY semester ASC, code ASC")
    fun observeAllCoursesIncludingHistory(): Flow<List<CourseEntity>>

    @Query("UPDATE courses SET isArchived = 1 WHERE semester < :newSemester")
    suspend fun archivePriorSemesters(newSemester: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourse(course: CourseEntity): Long
}

@Dao
interface TimetableDao {
    @Query("SELECT * FROM timetable_slots WHERE semester = :semester AND dayOfWeek = :dayOfWeek ORDER BY hourIndex ASC")
    fun observeDaySchedule(semester: Int, dayOfWeek: Int): Flow<List<TimetableSlotEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSlot(slot: TimetableSlotEntity): Long

    @Query("DELETE FROM timetable_slots WHERE id = :slotId")
    suspend fun deleteSlot(slotId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDayOverride(override: CalendarDayOverrideEntity)

    @Query("SELECT * FROM calendar_day_overrides WHERE dateEpochDay = :epochDay")
    suspend fun getDayOverride(epochDay: Long): CalendarDayOverrideEntity?
}

@Dao
interface AttendanceDao {
    @Query("SELECT * FROM attendance_logs WHERE courseId = :courseId ORDER BY dateEpochDay DESC, hourIndex ASC")
    fun observeLogsForCourse(courseId: Long): Flow<List<AttendanceLogEntity>>

    @Query("SELECT * FROM attendance_logs")
    fun observeAllLogs(): Flow<List<AttendanceLogEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertLogPreventDuplicate(log: AttendanceLogEntity): Long

    @Update
    suspend fun updateLog(log: AttendanceLogEntity)
}

@Dao
interface FlashcardDao {
    @Query("SELECT * FROM flashcards WHERE nextReviewEpochDay <= :todayEpochDay ORDER BY nextReviewEpochDay ASC")
    fun observeDueFlashcards(todayEpochDay: Long): Flow<List<FlashcardEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFlashcard(card: FlashcardEntity): Long

    @Update
    suspend fun updateFlashcard(card: FlashcardEntity)
}
