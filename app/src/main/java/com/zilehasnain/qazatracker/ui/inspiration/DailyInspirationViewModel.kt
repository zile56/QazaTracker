package com.zilehasnain.qazatracker.ui.inspiration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zilehasnain.qazatracker.domain.model.DailyInspiration
import com.zilehasnain.qazatracker.domain.model.Hadith
import com.zilehasnain.qazatracker.domain.model.InspirationCatalog
import com.zilehasnain.qazatracker.domain.model.InspirationItem
import com.zilehasnain.qazatracker.domain.model.QuranVerse
import com.zilehasnain.qazatracker.domain.repository.InspirationRepository
import com.zilehasnain.qazatracker.domain.usecase.GetDailyInspirationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import javax.inject.Inject
import kotlin.random.Random
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class InspirationTab { TODAY, SAVED }

data class InspirationUiState(
    val inspiration: DailyInspiration,
    /** 0 is the day's own pair; negative is earlier picks, positive later ones. */
    val offset: Int = 0,
    /** True when the last move went forward (Next/Refresh), so the screen slides the right way. */
    val movedForward: Boolean = true,
    val savedIds: Set<String> = emptySet(),
    /** Saved items, most recently saved first. Ids that are no longer in the library are dropped. */
    val saved: List<InspirationItem> = emptyList(),
    val tab: InspirationTab = InspirationTab.TODAY
) {
    val savedCount: Int get() = saved.size
    fun isSaved(id: String): Boolean = id in savedIds
}

@HiltViewModel
class DailyInspirationViewModel(
    private val repository: InspirationRepository,
    private val getDailyInspiration: GetDailyInspirationUseCase,
    private val today: () -> LocalDate,
    private val random: Random
) : ViewModel() {

    @Inject
    constructor(
        repository: InspirationRepository,
        getDailyInspiration: GetDailyInspirationUseCase
    ) : this(repository, getDailyInspiration, { LocalDate.now() }, Random.Default)

    private data class Position(val offset: Int, val forward: Boolean)

    private val position = MutableStateFlow(Position(0, true))
    private val tab = MutableStateFlow(InspirationTab.TODAY)

    val uiState: StateFlow<InspirationUiState> = combine(
        position, repository.observeBookmarkedIds(), tab
    ) { pos, ids, selectedTab ->
        InspirationUiState(
            inspiration = getDailyInspiration(today(), pos.offset),
            offset = pos.offset,
            movedForward = pos.forward,
            savedIds = ids.toSet(),
            saved = ids.mapNotNull(InspirationCatalog::find),
            tab = selectedTab
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), initialState())

    val dailyVerse: StateFlow<QuranVerse> = uiState.map { it.inspiration.verse }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), uiState.value.inspiration.verse)

    val dailyHadith: StateFlow<Hadith> = uiState.map { it.inspiration.hadith }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), uiState.value.inspiration.hadith)

    private fun initialState() = InspirationUiState(getDailyInspiration(today(), 0))

    fun onPrevious() = position.update { Position(it.offset - 1, forward = false) }

    fun onNext() = position.update { Position(it.offset + 1, forward = true) }

    /** A random pair from anywhere in the library. */
    fun onRefresh() = position.update { Position(random.nextInt(1, RANDOM_SPAN), forward = true) }

    fun onTabSelected(selected: InspirationTab) {
        tab.value = selected
    }

    fun onBookmark(item: InspirationItem) {
        val saved = !uiState.value.isSaved(item.id)
        viewModelScope.launch { repository.setBookmarked(item.id, saved) }
    }

    private companion object {
        const val RANDOM_SPAN = 1_000
    }
}
