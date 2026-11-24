package org.uyarvu.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import org.uyarvu.app.data.local.entity.AcademicTaskEntity
import org.uyarvu.app.data.local.entity.ExamEntity
import org.uyarvu.app.data.local.entity.StudySessionEntity
import org.uyarvu.app.data.local.entity.SyllabusUnitEntity

@Dao
interface SyllabusDao {
    @Query("SELECT * FROM syllabus_units WHERE courseId = :courseId ORDER BY unitNumber ASC")
    fun observeUnitsForCourse(courseId: Long): Flow<List<SyllabusUnitEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUnit(unit: SyllabusUnitEntity): Long

    @Update
    suspend fun updateUnit(unit: SyllabusUnitEntity)
}

@Dao
interface ExamDao {
    @Query("SELECT * FROM exams ORDER BY examDateEpochDay ASC")
    fun observeAllExams(): Flow<List<ExamEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExam(exam: ExamEntity): Long

    @Update
    suspend fun updateExam(exam: ExamEntity)
}

@Dao
interface AcademicTaskDao {
    @Query("SELECT * FROM academic_tasks ORDER BY isCompleted ASC, dueDateEpochDay ASC, priority DESC")
    fun observeAllTasks(): Flow<List<AcademicTaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: AcademicTaskEntity): Long

    @Update
    suspend fun updateTask(task: AcademicTaskEntity)
}

@Dao
interface StudySessionDao {
    @Query("SELECT * FROM study_sessions WHERE scheduledDateEpochDay = :epochDay")
    fun observeSessionsForDay(epochDay: Long): Flow<List<StudySessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: StudySessionEntity): Long
}
