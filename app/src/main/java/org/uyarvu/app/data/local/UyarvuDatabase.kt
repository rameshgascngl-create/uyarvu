package org.uyarvu.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import org.uyarvu.app.data.local.dao.AttendanceDao
import org.uyarvu.app.data.local.dao.CourseDao
import org.uyarvu.app.data.local.dao.FlashcardDao
import org.uyarvu.app.data.local.dao.StudentProfileDao
import org.uyarvu.app.data.local.dao.TimetableDao
import org.uyarvu.app.data.local.entity.AttendanceLogEntity
import org.uyarvu.app.data.local.entity.AttendanceStatus
import org.uyarvu.app.data.local.entity.CalendarDayOverrideEntity
import org.uyarvu.app.data.local.entity.CourseEntity
import org.uyarvu.app.data.local.entity.DayOverrideType
import org.uyarvu.app.data.local.entity.FlashcardEntity
import org.uyarvu.app.data.local.entity.SessionType
import org.uyarvu.app.data.local.entity.StudentProfileEntity
import org.uyarvu.app.data.local.entity.TimetableSlotEntity

class UyarvuConverters {
    @TypeConverter fun fromAttendanceStatus(v: AttendanceStatus): String = v.name
    @TypeConverter fun toAttendanceStatus(v: String): AttendanceStatus = AttendanceStatus.valueOf(v)
    @TypeConverter fun fromSessionType(v: SessionType): String = v.name
    @TypeConverter fun toSessionType(v: String): SessionType = SessionType.valueOf(v)
    @TypeConverter fun fromDayOverrideType(v: DayOverrideType): String = v.name
    @TypeConverter fun toDayOverrideType(v: String): DayOverrideType = DayOverrideType.valueOf(v)
}

@Database(
    entities = [
        StudentProfileEntity::class,
        CourseEntity::class,
        TimetableSlotEntity::class,
        CalendarDayOverrideEntity::class,
        AttendanceLogEntity::class,
        FlashcardEntity::class
    ],
    version = 2,
    exportSchema = true
)
@TypeConverters(UyarvuConverters::class)
abstract class UyarvuDatabase : RoomDatabase() {
    abstract fun studentProfileDao(): StudentProfileDao
    abstract fun courseDao(): CourseDao
    abstract fun timetableDao(): TimetableDao
    abstract fun attendanceDao(): AttendanceDao
    abstract fun flashcardDao(): FlashcardDao
}
