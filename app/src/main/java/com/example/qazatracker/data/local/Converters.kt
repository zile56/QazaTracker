package com.example.qazatracker.data.local

import androidx.room.TypeConverter
import java.time.Instant

class Converters {
    @TypeConverter
    fun fromEpochMilli(value: Long?): Instant? = value?.let(Instant::ofEpochMilli)

    @TypeConverter
    fun toEpochMilli(instant: Instant?): Long? = instant?.toEpochMilli()
}
