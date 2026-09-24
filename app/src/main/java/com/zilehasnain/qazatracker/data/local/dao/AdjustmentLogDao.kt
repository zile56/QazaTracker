package com.zilehasnain.qazatracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.zilehasnain.qazatracker.data.local.entity.AdjustmentLog
import com.zilehasnain.qazatracker.domain.model.PrayerType
import kotlinx.coroutines.flow.Flow

@Dao
interface AdjustmentLogDao {
    @Insert
    suspend fun insert(log: AdjustmentLog): Long

    @Query("SELECT * FROM AdjustmentLog ORDER BY timestamp DESC")
    fun observeAll(): Flow<List<AdjustmentLog>>

    @Query("SELECT * FROM AdjustmentLog WHERE prayerType = :prayerType ORDER BY timestamp DESC")
    fun observeByType(prayerType: PrayerType): Flow<List<AdjustmentLog>>
}
