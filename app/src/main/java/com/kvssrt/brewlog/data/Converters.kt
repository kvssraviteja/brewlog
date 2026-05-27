package com.kvssrt.brewlog.data

import androidx.room.TypeConverter
import java.time.LocalDate

class Converters {
    @TypeConverter
    fun localDateToString(value: LocalDate): String = value.toString()

    @TypeConverter
    fun stringToLocalDate(value: String): LocalDate = LocalDate.parse(value)
}
