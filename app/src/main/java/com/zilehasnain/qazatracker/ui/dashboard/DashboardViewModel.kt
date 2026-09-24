package com.zilehasnain.qazatracker.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zilehasnain.qazatracker.domain.model.AdjustmentReason
import com.zilehasnain.qazatracker.domain.model.CompletionEntry
import com.zilehasnain.qazatracker.domain.model.CompletionProjection
import com.zilehasnain.qazatracker.domain.model.PrayerType
import com.zilehasnain.qazatracker.domain.model.RemainingPrayerCount
import com.zilehasnain.qazatracker.domain.repository.QazaRepository
import com.zilehasnain.qazatracker.domain.usecase.ApplyAdjustmentUseCase
import com.zilehasnain.qazatracker.domain.usecase.CalculateStreakUseCase
import com.zilehasnain.qazatracker.domain.usecase.LogCompletionUseCase
import com.zilehasnain.qazatracker.domain.usecase.ProjectCompletionDateUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class DashboardViewModel @Inject constructor(
    repository: QazaRepository,
    private val projectCompletionDate: ProjectCompletionDateUseCase,
    private val calculateStreak: CalculateStreakUseCase,
    private val logCompletion: LogCompletionUseCase,
    private val applyAdjustment: ApplyAdjustmentUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    /**
     * Quick single-tap log for one prayer type. The dashboard doesn't refetch after this —
     * Room's Flow invalidation re-emits observeRemainingCounts() on its own, so uiState
     * updates the same way it does for any other write to the ledger tables.
     */
    fun onQuickLog(prayerType: PrayerType) {
        viewModelScope.launch { logCompletion(prayerType) }
    }

    fun onAdjustClicked(prayerType: PrayerType) {
        _uiState.update { it.copy(adjustmentDialog = AdjustmentDialogState(prayerType)) }
    }

    fun onDismissAdjustmentDialog() {
        _uiState.update { it.copy(adjustmentDialog = null) }
    }

    fun onAdjustmentSignChanged(isNegative: Boolean) {
        _uiState.update { it.copy(adjustmentDialog = it.adjustmentDialog?.copy(isNegative = isNegative)) }
    }

    fun onAdjustmentMagnitudeChanged(value: String) {
        if (value.length <= 4 && value.all(Char::isDigit)) {
            _uiState.update { it.copy(adjustmentDialog = it.adjustmentDialog?.copy(magnitudeInput = value)) }
        }
    }

    fun onAdjustmentNoteChanged(value: String) {
        _uiState.update { it.copy(adjustmentDialog = it.adjustmentDialog?.copy(note = value)) }
    }

    fun onConfirmAdjustment() {
        val dialog = _uiState.value.adjustmentDialog ?: return
        val delta = dialog.delta
        if (delta == null || delta == 0) return

        viewModelScope.launch {
            applyAdjustment(
                dialog.prayerType,
                delta,
                AdjustmentReason.MANUAL_CORRECTION,
                dialog.note.ifBlank { null }
            )
            _uiState.update { it.copy(adjustmentDialog = null) }
        }
    }

    init {
        viewModelScope.launch {
            combine(
                repository.observeRemainingCounts(),
                repository.observeBaselineStartedAt(),
                repository.observeCompletionLogs()
            ) { counts, startedAt, completions -> Triple(counts, startedAt, completions) }
                .collect { (counts, startedAt, completions) ->
                    val computed = buildUiState(counts, startedAt, completions)
                    // Only replace the repository-derived fields — a re-emission (e.g. from
                    // another screen's write) must not clobber an in-progress dialog.
                    _uiState.update {
                        it.copy(rows = computed.rows, projection = computed.projection, streak = computed.streak)
                    }
                }
        }
    }

    private fun buildUiState(
        counts: List<RemainingPrayerCount>,
        startedAt: Instant?,
        completions: List<CompletionEntry>
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

        val streak = calculateStreak(completions, LocalDate.now(), ZoneId.systemDefault())

        return DashboardUiState(rows = rows, projection = projection, streak = streak)
    }
}
