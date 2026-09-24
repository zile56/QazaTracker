package com.zilehasnain.qazatracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.zilehasnain.qazatracker.domain.model.PrayerType
import java.time.Instant

/**
 * One row per "every prayer of this type is now completed" moment. Append-only, like the other
 * log tables: it records a fact that happened, it isn't a counter, so it doesn't conflict with
 * remaining counts always being derived. A type can appear more than once if the user later adds
 * missed prayers back and finishes them again.
 */
@Entity(tableName = "prayer_milestones")
data class MilestoneEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val prayerType: PrayerType,
    val achievedAt: Instant
)
