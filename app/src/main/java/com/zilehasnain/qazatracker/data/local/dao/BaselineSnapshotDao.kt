package com.zilehasnain.qazatracker.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.zilehasnain.qazatracker.data.local.entity.BaselineSnapshot
import com.zilehasnain.qazatracker.domain.model.PrayerType
import kotlinx.coroutines.flow.Flow

@Dao
interface BaselineSnapshotDao {
    @Upsert
    suspend fun upsert(snapshot: BaselineSnapshot)

    @Upsert
    suspend fun upsertAll(snapshots: List<BaselineSnapshot>)

    @Query("SELECT * FROM BaselineSnapshot")
    fun observeAll(): Flow<List<BaselineSnapshot>>

    @Query("SELECT * FROM BaselineSnapshot WHERE prayerType = :prayerType")
    fun observeByType(prayerType: PrayerType): Flow<BaselineSnapshot?>
}
