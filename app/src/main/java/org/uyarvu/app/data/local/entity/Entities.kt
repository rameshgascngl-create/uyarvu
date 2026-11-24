package org.uyarvu.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "student_profile")
data class StudentProfileEntity(
    @PrimaryKey val id: Int = 1,
    val preferredLanguage: String,
    val degreeProgramme: String,
    val academicYear: Int,
    val semester: Int,
    val institutionName: String? = null,
    val dailyStudyMinutes: Int = 60,
    val onboardingCompleted: Boolean = false
)

@Entity(tableName = "courses")
data class CourseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val code: String,
    val nameEn: String,
    val nameTa: String,
    val semester: Int,
    val regularThresholdPct: Double = 75.0,
    val condonationFloorPct: Double = 65.0,
    val medicalCondonationFloorPct: Double = 60.0,
    val plannedTotalClasses: Int = 60
)

enum class AttendanceStatus {
    PRESENT,
    ABSENT,
    CANCELLED,
    NOT_CONDUCTED
}

@Entity(
    tableName = "attendance_logs",
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
data class AttendanceLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val courseId: Long,
    val dateEpochDay: Long,
    val status: AttendanceStatus,
    val note: String = ""
)

@Entity(
    tableName = "flashcards",
    foreignKeys = [
        ForeignKey(
            entity = CourseEntity::class,
            parentColumns = ["id"],
            childColumns = ["courseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("courseId"), Index("nextReviewEpochDay")]
)
data class FlashcardEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val courseId: Long,
    val promptEn: String,
    val promptTa: String,
    val answerEn: String,
    val answerTa: String,
    val easeFactor: Double = 2.5,
    val intervalDays: Int = 0,
    val repetitions: Int = 0,
    val nextReviewEpochDay: Long = 0L
)
