package com.example.qazatracker.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qazatracker.domain.model.CompletionProjection
import com.example.qazatracker.domain.model.PrayerType
import com.example.qazatracker.domain.model.RemainingPrayerCount
import com.example.qazatracker.domain.repository.QazaRepository
import com.example.qazatracker.domain.usecase.ProjectCompletionDateUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

@HiltViewModel
class DashboardViewModel @Inject constructor(
    repository: QazaRepository,
    private val projectCompletionDate: ProjectCompletionDateUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                repository.observeRemainingCounts(),
                repository.observeBaselineStartedAt()
            ) { counts, startedAt -> counts to startedAt }
                .collect { (counts, startedAt) ->
                    _uiState.value = buildUiState(counts, startedAt)
                }
        }
    }

    private fun buildUiState(
        counts: List<RemainingPrayerCount>,
        startedAt: Instant?
    ): DashboardUiState {
        val byType = counts.associateBy { it.prayerType }
        val rows = PrayerType.entries.map { type ->
            val count = byType[type]
            PrayerRowUiState(
                prayerType = type,
                completed = count?.completed ?: 0,
                remaining = (count?.remaining ?: 0).coerceAtLeast(0)
            )
        }

        val totalRemaining = rows.sumOf { it.remaining }
        val totalCompleted = rows.sumOf { it.completed }

        val projection = if (startedAt == null) {
            CompletionProjection.InsufficientData
        } else {
            projectCompletionDate(
                totalRemaining = totalRemaining,
                totalCompleted = totalCompleted,
                trackingStartedAt = startedAt.atZone(ZoneId.systemDefault()).toLocalDate()
            )
        }

        return DashboardUiState(rows = rows, projection = projection)
    }
}
