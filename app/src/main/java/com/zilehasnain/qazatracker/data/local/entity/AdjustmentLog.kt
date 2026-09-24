package com.zilehasnain.qazatracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.zilehasnain.qazatracker.domain.model.AdjustmentReason
import com.zilehasnain.qazatracker.domain.model.PrayerType
import java.time.Instant

@Entity
data class AdjustmentLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val prayerType: PrayerType,
    val delta: Int,
    val reason: AdjustmentReason,
    val note: String? = null,
    val timestamp: Instant
)
