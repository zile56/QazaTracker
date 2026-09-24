package com.zilehasnain.qazatracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.zilehasnain.qazatracker.data.local.entity.CompletionLog
import com.zilehasnain.qazatracker.domain.model.PrayerType
import kotlinx.coroutines.flow.Flow

@Dao
interface CompletionLogDao {
    @Insert
    suspend fun insert(log: CompletionLog): Long

    @Insert
    suspend fun insertAll(logs: List<CompletionLog>): List<Long>

    @Query("SELECT * FROM CompletionLog ORDER BY timestamp DESC")
    fun observeAll(): Flow<List<CompletionLog>>

    @Query("SELECT * FROM CompletionLog WHERE prayerType = :prayerType ORDER BY timestamp DESC")
    fun observeByType(prayerType: PrayerType): Flow<List<CompletionLog>>
}
