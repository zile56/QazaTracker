package com.zilehasnain.qazatracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.zilehasnain.qazatracker.domain.model.CalculationMethod
import com.zilehasnain.qazatracker.domain.model.PrayerType
import java.time.Instant

@Entity
data class BaselineSnapshot(
    @PrimaryKey val prayerType: PrayerType,
    val initialCount: Int,
    val calculatedAt: Instant,
    val method: CalculationMethod
)
