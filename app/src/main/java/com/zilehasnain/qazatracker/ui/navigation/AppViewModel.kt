package com.zilehasnain.qazatracker.ui.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zilehasnain.qazatracker.domain.repository.QazaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Decides where the nav graph should start: straight to the dashboard once a baseline
 * exists, or onboarding for a first-time user. Null means "still checking" — the graph
 * shouldn't render yet, to avoid a startDestination flicker.
 */
@HiltViewModel
class AppViewModel @Inject constructor(
    repository: QazaRepository
) : ViewModel() {

    private val _hasBaseline = MutableStateFlow<Boolean?>(null)
    val hasBaseline: StateFlow<Boolean?> = _hasBaseline.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeHasBaseline().collect { _hasBaseline.value = it }
        }
    }
}
