package com.zilehasnain.qazatracker.ui.tutorial

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zilehasnain.qazatracker.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class TutorialViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private var dismissed = false

    /**
     * Persists hasSeenTutorial = true, then calls [onDone]. Finishing and skipping both come
     * through here. Guarded so a double-tap can't run [onDone] (and so navigate) twice.
     */
    fun onDismiss(onDone: () -> Unit) {
        if (dismissed) return
        dismissed = true
        viewModelScope.launch {
            settingsRepository.markTutorialSeen()
            onDone()
        }
    }
}
