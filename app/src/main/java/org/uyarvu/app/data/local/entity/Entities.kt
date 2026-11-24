package org.uyarvu.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "student_profile")
data class StudentProfileEntity(
    @PrimaryKey val id: Int = 1,
    val preferredName: String,
    val preferredLanguage: String,
    val institutionName: String? = null,
    val degreeDepartment: String,
    val academicYearLabel: String,
    val currentSemester: Int,
    val workingDaysMask: String = "MON,TUE,WED,THU,FRI",
    val onboardingCompleted: Boolean = false
)

@Entity(tableName = "courses")
data class CourseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val code: String,
    val nameEn: String,
    val nameTa: String,
    val semester: Int,
    val hasPractical: Boolean = false,
    val regularThresholdPct: Double = 75.0,
    val condonationFloorPct: Double = 65.0,
    val medicalCondonationFloorPct: Double = 50.0,
    val plannedTheoryClasses: Int = 60,
    val plannedPracticalClasses: Int = 0,
    val isArchived: Boolean = false
)

enum class SessionType {
    THEORY,
    PRACTICAL
}

@Entity(
    tableName = "timetable_slots",
    foreignKeys = [
        ForeignKey(
            entity = CourseEntity::class,
            parentColumns = ["id"],
            childColumns = ["courseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("courseId"), Index("dayOfWeek")]
)
data class TimetableSlotEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val courseId: Long,
    val semester: Int,
    val dayOfWeek: Int, // 1=Mon .. 7=Sun
    val hourIndex: Int, // 1..8
    val startTimeLabel: String,
    val endTimeLabel: String,
    val sessionType: SessionType = SessionType.THEORY,
    val reminderEnabled: Boolean = false
)

enum class DayOverrideType {
    HOLIDAY,
    COMPENSATORY_WORKING_DAY
}

@Entity(tableName = "calendar_day_overrides")
data class CalendarDayOverrideEntity(
    @PrimaryKey val dateEpochDay: Long,
    val overrideType: DayOverrideType,
    val followsDayOfWeek: Int? = null, // e.g., Saturday follows Monday schedule
    val label: String = ""
)

enum class AttendanceStatus {
    PRESENT,
    ABSENT,
    CANCELLED,
    NOT_RECORDED
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
    indices = [
        Index("courseId"),
        Index(value = ["courseId", "dateEpochDay", "hourIndex", "sessionType"], unique = true)
    ]
)
data class AttendanceLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val courseId: Long,
    val dateEpochDay: Long,
    val hourIndex: Int = 1,
    val sessionType: SessionType = SessionType.THEORY,
    val status: AttendanceStatus,
    val isExtraClass: Boolean = false,
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
