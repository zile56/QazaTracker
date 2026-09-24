package com.zilehasnain.qazatracker.ui.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zilehasnain.qazatracker.domain.repository.QazaRepository
import com.zilehasnain.qazatracker.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Decides where the nav graph should start — tutorial for a brand-new user, onboarding once
 * the tutorial has been seen, or the dashboard once a baseline exists. Null means "still
 * checking": the graph shouldn't render yet, to avoid a startDestination flicker.
 *
 * Resolved once per launch, not observed live: the flags flip while the user is mid-flow
 * (dismissing the tutorial sets hasSeenTutorial), and a NavHost whose startDestination changes
 * underneath it would be rebuilt mid-navigation.
 */
@HiltViewModel
class AppViewModel @Inject constructor(
    repository: QazaRepository,
    settingsRepository: SettingsRepository
) : ViewModel() {

    private val _startDestination = MutableStateFlow<String?>(null)
    val startDestination: StateFlow<String?> = _startDestination.asStateFlow()

    init {
        viewModelScope.launch {
            val hasBaseline = repository.observeHasBaseline().first()
            val hasSeenTutorial = settingsRepository.observeHasSeenTutorial().first()
            _startDestination.value = Routes.startDestinationFor(hasBaseline, hasSeenTutorial)
        }
    }
}
