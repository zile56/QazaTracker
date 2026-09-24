package com.zilehasnain.qazatracker.ui.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zilehasnain.qazatracker.domain.model.MonthlyStatsData
import com.zilehasnain.qazatracker.domain.repository.QazaRepository
import com.zilehasnain.qazatracker.domain.usecase.CalculateMonthlyStatsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.YearMonth
import java.time.ZoneId
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * The month can be moved between the month tracking began (there is nothing earlier) and the
 * current month (there is nothing later); [onMonthChanged] clamps into that range.
 */
@HiltViewModel
class StatisticsViewModel(
    calculateMonthlyStats: CalculateMonthlyStatsUseCase,
    repository: QazaRepository,
    val currentMonth: YearMonth,
    zone: ZoneId
) : ViewModel() {

    // Hilt is pointed at this one; the primary constructor exists so tests can fix the clock.
    @Inject constructor(
        calculateMonthlyStats: CalculateMonthlyStatsUseCase,
        repository: QazaRepository
    ) : this(calculateMonthlyStats, repository, YearMonth.now(), ZoneId.systemDefault())

    private val _selectedMonth = MutableStateFlow(currentMonth)
    val selectedMonth: StateFlow<YearMonth> = _selectedMonth.asStateFlow()

    /** The month the baseline was set; before tracking began there is nothing to show. */
    val earliestMonth: StateFlow<YearMonth> = repository.observeBaselineStartedAt()
        .map { startedAt ->
            startedAt?.let { minOf(YearMonth.from(it.atZone(zone)), currentMonth) } ?: currentMonth
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, currentMonth)

    @OptIn(ExperimentalCoroutinesApi::class)
    val currentMonthStats: StateFlow<MonthlyStatsData> = _selectedMonth
        .flatMapLatest { month -> calculateMonthlyStats(month) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MonthlyStatsData.empty(currentMonth))

    fun onMonthChanged(month: YearMonth) {
        _selectedMonth.value = month.clampedTo(earliest = earliestMonth.value, latest = currentMonth)
    }
}

internal fun YearMonth.clampedTo(earliest: YearMonth, latest: YearMonth): YearMonth {
    val floor = minOf(earliest, latest)
    return when {
        isBefore(floor) -> floor
        isAfter(latest) -> latest
        else -> this
    }
}
