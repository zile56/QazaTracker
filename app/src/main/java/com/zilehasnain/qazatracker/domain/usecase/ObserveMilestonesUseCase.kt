package com.zilehasnain.qazatracker.domain.usecase

import com.zilehasnain.qazatracker.domain.model.MilestoneData
import com.zilehasnain.qazatracker.domain.model.PrayerMilestone
import com.zilehasnain.qazatracker.domain.repository.QazaRepository
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Milestones as displayable data, most recent first, with [MilestoneData.isNewToday] resolved. */
class ObserveMilestonesUseCase(
    private val repository: QazaRepository,
    private val today: () -> LocalDate = LocalDate::now,
    private val zone: () -> ZoneId = ZoneId::systemDefault
) {
    @Inject constructor(repository: QazaRepository) : this(repository, LocalDate::now, ZoneId::systemDefault)

    operator fun invoke(): Flow<List<MilestoneData>> =
        repository.observeMilestones().map { entries ->
            val currentDay = today()
            val currentZone = zone()
            entries.map { entry ->
                val achievedDate = entry.achievedAt.atZone(currentZone).toLocalDate()
                MilestoneData(
                    milestone = PrayerMilestone.forPrayer(entry.prayerType),
                    milestoneAchievedDate = achievedDate,
                    isNewToday = achievedDate == currentDay
                )
            }
        }
}
