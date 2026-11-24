package org.uyarvu.app

import android.app.Application
import androidx.room.Room
import org.uyarvu.app.data.local.UyarvuDatabase

class UyarvuApplication : Application() {
    val database: UyarvuDatabase by lazy {
        Room.databaseBuilder(
            applicationContext,
            UyarvuDatabase::class.java,
            "uyarvu_db"
        ).build()
    }
}
