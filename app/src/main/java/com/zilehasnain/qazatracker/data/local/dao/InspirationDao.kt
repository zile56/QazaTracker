package com.zilehasnain.qazatracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.zilehasnain.qazatracker.data.local.entity.InspirationBookmarkEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InspirationDao {
    /** Most recently saved first. */
    @Query("SELECT * FROM inspiration_bookmarks ORDER BY savedAt DESC, itemId")
    fun observeBookmarks(): Flow<List<InspirationBookmarkEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(bookmark: InspirationBookmarkEntity)

    @Query("DELETE FROM inspiration_bookmarks WHERE itemId = :itemId")
    suspend fun delete(itemId: String)
}
