package com.kvssrt.brewlog.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        CoffeeBagEntity::class,
        PourMethodEntity::class,
        PourLogEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class BrewlogDatabase : RoomDatabase() {
    abstract fun brewlogDao(): BrewlogDao

    companion object {
        @Volatile
        private var instance: BrewlogDatabase? = null

        fun getInstance(context: Context): BrewlogDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    BrewlogDatabase::class.java,
                    "brewlog.db",
                ).build().also { instance = it }
            }
    }
}
