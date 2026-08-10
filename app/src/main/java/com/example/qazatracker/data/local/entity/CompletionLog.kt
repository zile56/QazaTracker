package com.example.qazatracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.qazatracker.domain.model.PrayerType
import java.time.Instant

@Entity
data class CompletionLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val prayerType: PrayerType,
    val timestamp: Instant,
    val batchId: String? = null
)
