package org.uyarvu.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import org.uyarvu.app.data.local.entity.AttendanceLogEntity
import org.uyarvu.app.data.local.entity.CourseEntity
import org.uyarvu.app.data.local.entity.FlashcardEntity
import org.uyarvu.app.data.local.entity.StudentProfileEntity

@Dao
interface StudentProfileDao {
    @Query("SELECT * FROM student_profile WHERE id = 1")
    fun observeProfile(): Flow<StudentProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProfile(profile: StudentProfileEntity)

    @Query("DELETE FROM student_profile")
    suspend fun clearProfile()
}

@Dao
interface CourseDao {
    @Query("SELECT * FROM courses ORDER BY code ASC")
    fun observeCourses(): Flow<List<CourseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourse(course: CourseEntity): Long
}

@Dao
interface AttendanceDao {
    @Query("SELECT * FROM attendance_logs WHERE courseId = :courseId ORDER BY dateEpochDay DESC")
    fun observeLogsForCourse(courseId: Long): Flow<List<AttendanceLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: AttendanceLogEntity): Long

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
