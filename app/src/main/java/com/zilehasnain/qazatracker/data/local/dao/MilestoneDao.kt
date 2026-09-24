package com.zilehasnain.qazatracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.zilehasnain.qazatracker.data.local.entity.MilestoneEntity
import com.zilehasnain.qazatracker.domain.model.PrayerType
import kotlinx.coroutines.flow.Flow

@Dao
interface MilestoneDao {
    @Insert
    suspend fun insertMilestone(milestone: MilestoneEntity): Long

    /** Most recent first. */
    @Query("SELECT * FROM prayer_milestones ORDER BY achievedAt DESC, id DESC")
    fun getMilestones(): Flow<List<MilestoneEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM prayer_milestones WHERE prayerType = :prayerType)")
    fun hasAchievedMilestone(prayerType: PrayerType): Flow<Boolean>
}
