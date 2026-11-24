package org.uyarvu.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.uyarvu.app.data.local.dao.AcademicTaskDao
import org.uyarvu.app.data.local.dao.AttendanceDao
import org.uyarvu.app.data.local.dao.CourseDao
import org.uyarvu.app.data.local.dao.ExamDao
import org.uyarvu.app.data.local.dao.FlashcardDao
import org.uyarvu.app.data.local.dao.StudentProfileDao
import org.uyarvu.app.data.local.dao.StudySessionDao
import org.uyarvu.app.data.local.dao.SyllabusDao
import org.uyarvu.app.data.local.dao.TimetableDao
import org.uyarvu.app.data.local.entity.AcademicTaskEntity
import org.uyarvu.app.data.local.entity.AttendanceLogEntity
import org.uyarvu.app.data.local.entity.AttendanceStatus
import org.uyarvu.app.data.local.entity.CalendarDayOverrideEntity
import org.uyarvu.app.data.local.entity.CourseEntity
import org.uyarvu.app.data.local.entity.DayOverrideType
import org.uyarvu.app.data.local.entity.ExamEntity
import org.uyarvu.app.data.local.entity.ExamType
import org.uyarvu.app.data.local.entity.FlashcardEntity
import org.uyarvu.app.data.local.entity.SessionType
import org.uyarvu.app.data.local.entity.StudentProfileEntity
import org.uyarvu.app.data.local.entity.StudySessionEntity
import org.uyarvu.app.data.local.entity.SyllabusUnitEntity
import org.uyarvu.app.data.local.entity.TaskCategory
import org.uyarvu.app.data.local.entity.TimetableSlotEntity

class UyarvuConverters {
    @TypeConverter fun fromAttendanceStatus(v: AttendanceStatus): String = v.name
    @TypeConverter fun toAttendanceStatus(v: String): AttendanceStatus = AttendanceStatus.valueOf(v)
    @TypeConverter fun fromSessionType(v: SessionType): String = v.name
    @TypeConverter fun toSessionType(v: String): SessionType = SessionType.valueOf(v)
    @TypeConverter fun fromDayOverrideType(v: DayOverrideType): String = v.name
    @TypeConverter fun toDayOverrideType(v: String): DayOverrideType = DayOverrideType.valueOf(v)
    @TypeConverter fun fromExamType(v: ExamType): String = v.name
    @TypeConverter fun toExamType(v: String): ExamType = ExamType.valueOf(v)
    @TypeConverter fun fromTaskCategory(v: TaskCategory): String = v.name
    @TypeConverter fun toTaskCategory(v: String): TaskCategory = TaskCategory.valueOf(v)
}

@Database(
    entities = [
        StudentProfileEntity::class,
        CourseEntity::class,
        TimetableSlotEntity::class,
        CalendarDayOverrideEntity::class,
        AttendanceLogEntity::class,
        FlashcardEntity::class,
        SyllabusUnitEntity::class,
        ExamEntity::class,
        AcademicTaskEntity::class,
        StudySessionEntity::class
    ],
    version = 3,
    exportSchema = true
)
@TypeConverters(UyarvuConverters::class)
abstract class UyarvuDatabase : RoomDatabase() {
    abstract fun studentProfileDao(): StudentProfileDao
    abstract fun courseDao(): CourseDao
    abstract fun timetableDao(): TimetableDao
    abstract fun attendanceDao(): AttendanceDao
    abstract fun flashcardDao(): FlashcardDao
    abstract fun syllabusDao(): SyllabusDao
    abstract fun examDao(): ExamDao
    abstract fun academicTaskDao(): AcademicTaskDao
    abstract fun studySessionDao(): StudySessionDao

    companion object {
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS syllabus_units (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        courseId INTEGER NOT NULL,
                        unitNumber INTEGER NOT NULL,
                        titleEn TEXT NOT NULL,
                        titleTa TEXT NOT NULL,
                        learningObjective TEXT NOT NULL,
                        difficultyLevel INTEGER NOT NULL,
                        studentConfidence INTEGER NOT NULL,
                        isCompleted INTEGER NOT NULL,
                        needsRevision INTEGER NOT NULL,
                        FOREIGN KEY(courseId) REFERENCES courses(id) ON DELETE CASCADE
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS index_syllabus_units_courseId ON syllabus_units(courseId)")

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS exams (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        courseId INTEGER NOT NULL,
                        titleEn TEXT NOT NULL,
                        titleTa TEXT NOT NULL,
                        examType TEXT NOT NULL,
                        examDateEpochDay INTEGER NOT NULL,
                        preparationProgressPct INTEGER NOT NULL,
                        reminderEnabled INTEGER NOT NULL,
                        FOREIGN KEY(courseId) REFERENCES courses(id) ON DELETE CASCADE
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS index_exams_courseId ON exams(courseId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_exams_examDateEpochDay ON exams(examDateEpochDay)")

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS academic_tasks (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        courseId INTEGER,
                        titleEn TEXT NOT NULL,
                        titleTa TEXT NOT NULL,
                        category TEXT NOT NULL,
                        priority INTEGER NOT NULL,
                        dueDateEpochDay INTEGER NOT NULL,
                        estimatedMinutes INTEGER NOT NULL,
                        isCompleted INTEGER NOT NULL,
                        completedAtEpochDay INTEGER,
                        isRecurringWeekly INTEGER NOT NULL,
                        subStepsJson TEXT NOT NULL,
                        FOREIGN KEY(courseId) REFERENCES courses(id) ON DELETE SET NULL
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS index_academic_tasks_courseId ON academic_tasks(courseId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_academic_tasks_dueDateEpochDay ON academic_tasks(dueDateEpochDay)")

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS study_sessions (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        courseId INTEGER,
                        taskId INTEGER,
                        scheduledDateEpochDay INTEGER NOT NULL,
                        durationMinutes INTEGER NOT NULL,
                        breakMinutes INTEGER NOT NULL,
                        reasonLabel TEXT NOT NULL,
                        isCompleted INTEGER NOT NULL
                    )
                """.trimIndent())
            }
        }
    }
}
