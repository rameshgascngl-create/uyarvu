package org.uyarvu.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "syllabus_units",
    foreignKeys = [
        ForeignKey(
            entity = CourseEntity::class,
            parentColumns = ["id"],
            childColumns = ["courseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("courseId")]
)
data class SyllabusUnitEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val courseId: Long,
    val unitNumber: Int,
    val titleEn: String,
    val titleTa: String,
    val learningObjective: String = "",
    val difficultyLevel: Int = 3, // 1 (Easy) .. 5 (Hard)
    val studentConfidence: Int = 3, // 1 (Low) .. 5 (High)
    val isCompleted: Boolean = false,
    val needsRevision: Boolean = false
)

enum class ExamType {
    INTERNAL_ASSESSMENT,
    SEMESTER_THEORY,
    PRACTICAL,
    ARREAR,
    COMPETITIVE
}

@Entity(
    tableName = "exams",
    foreignKeys = [
        ForeignKey(
            entity = CourseEntity::class,
            parentColumns = ["id"],
            childColumns = ["courseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("courseId"), Index("examDateEpochDay")]
)
data class ExamEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val courseId: Long,
    val titleEn: String,
    val titleTa: String,
    val examType: ExamType,
    val examDateEpochDay: Long,
    val preparationProgressPct: Int = 0,
    val reminderEnabled: Boolean = true
)

enum class TaskCategory {
    ASSIGNMENT,
    LAB_RECORD,
    SEMINAR,
    PROJECT,
    LIBRARY,
    REVISION,
    PERSONAL_STUDY
}

@Entity(
    tableName = "academic_tasks",
    foreignKeys = [
        ForeignKey(
            entity = CourseEntity::class,
            parentColumns = ["id"],
            childColumns = ["courseId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("courseId"), Index("dueDateEpochDay")]
)
data class AcademicTaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val courseId: Long? = null,
    val titleEn: String,
    val titleTa: String,
    val category: TaskCategory,
    val priority: Int = 2, // 1=Low, 2=Medium, 3=High
    val dueDateEpochDay: Long,
    val estimatedMinutes: Int = 30,
    val isCompleted: Boolean = false,
    val completedAtEpochDay: Long? = null,
    val isRecurringWeekly: Boolean = false,
    val subStepsJson: String = "[]"
)

@Entity(tableName = "study_sessions")
data class StudySessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val courseId: Long?,
    val taskId: Long?,
    val scheduledDateEpochDay: Long,
    val durationMinutes: Int,
    val breakMinutes: Int = 10,
    val reasonLabel: String,
    val isCompleted: Boolean = false
)
