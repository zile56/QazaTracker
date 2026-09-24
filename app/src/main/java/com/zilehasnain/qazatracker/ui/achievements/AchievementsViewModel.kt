package com.zilehasnain.qazatracker.ui.achievements

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zilehasnain.qazatracker.domain.model.AchievementsData
import com.zilehasnain.qazatracker.domain.usecase.GetAllAchievementsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class AchievementsViewModel @Inject constructor(
    getAllAchievements: GetAllAchievementsUseCase
) : ViewModel() {

    private val _hasLoaded = MutableStateFlow(false)

    /**
     * False until the first real result arrives, so the screen can wait rather than flash
     * "no achievements yet" at someone who has plenty while the database is read.
     */
    val hasLoaded: StateFlow<Boolean> = _hasLoaded.asStateFlow()

    val achievements: StateFlow<AchievementsData> = getAllAchievements()
        .onEach { _hasLoaded.value = true }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AchievementsData.Empty)
}
