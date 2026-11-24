package org.uyarvu.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import org.uyarvu.app.data.local.dao.AttendanceDao
import org.uyarvu.app.data.local.dao.CourseDao
import org.uyarvu.app.data.local.dao.FlashcardDao
import org.uyarvu.app.data.local.dao.StudentProfileDao
import org.uyarvu.app.data.local.entity.AttendanceLogEntity
import org.uyarvu.app.data.local.entity.AttendanceStatus
import org.uyarvu.app.data.local.entity.CourseEntity
import org.uyarvu.app.data.local.entity.FlashcardEntity
import org.uyarvu.app.data.local.entity.StudentProfileEntity

class UyarvuConverters {
    @TypeConverter
    fun fromStatus(status: AttendanceStatus): String = status.name

    @TypeConverter
    fun toStatus(value: String): AttendanceStatus = AttendanceStatus.valueOf(value)
}

@Database(
    entities = [
        StudentProfileEntity::class,
        CourseEntity::class,
        AttendanceLogEntity::class,
        FlashcardEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(UyarvuConverters::class)
abstract class UyarvuDatabase : RoomDatabase() {
    abstract fun studentProfileDao(): StudentProfileDao
    abstract fun courseDao(): CourseDao
    abstract fun attendanceDao(): AttendanceDao
    abstract fun flashcardDao(): FlashcardDao
}
