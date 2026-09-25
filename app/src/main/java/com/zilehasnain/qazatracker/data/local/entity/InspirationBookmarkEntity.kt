package com.zilehasnain.qazatracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

/**
 * A saved verse or hadith. Only the id of the built-in item is stored: the text itself ships in
 * the app, so it can never go stale or be duplicated here.
 */
@Entity(tableName = "inspiration_bookmarks")
data class InspirationBookmarkEntity(
    @PrimaryKey val itemId: String,
    val savedAt: Instant
)
