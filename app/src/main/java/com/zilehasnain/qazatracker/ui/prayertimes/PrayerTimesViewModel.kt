package com.zilehasnain.qazatracker.ui.prayertimes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zilehasnain.qazatracker.domain.model.LocationData
import com.zilehasnain.qazatracker.domain.model.PrayerCalculationMethod
import com.zilehasnain.qazatracker.domain.model.PrayerTimeData
import com.zilehasnain.qazatracker.domain.model.RegionInfo
import com.zilehasnain.qazatracker.domain.model.RegionSelection
import com.zilehasnain.qazatracker.domain.repository.LocationProvider
import com.zilehasnain.qazatracker.domain.repository.PrayerTimesSettingsRepository
import com.zilehasnain.qazatracker.domain.usecase.GetPrayerTimesUseCase
import com.zilehasnain.qazatracker.domain.usecase.GetRecommendedMethodUseCase
import com.zilehasnain.qazatracker.domain.usecase.SearchRegionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Everything the region-finder dialog needs to draw itself. */
data class RegionFinderState(
    val query: String = "",
    val results: List<RegionInfo> = emptyList(),
    /** The country the user has tapped, waiting for Confirm. */
    val selected: RegionInfo? = null,
    /** The method being previewed for [selected]: its recommendation unless they tapped an alternative. */
    val chosenMethod: PrayerCalculationMethod? = null
)

data class PrayerTimesUiState(
    val hasLoaded: Boolean = false,
    val times: PrayerTimeData? = null,
    val region: RegionInfo? = null,
    val method: PrayerCalculationMethod = PrayerCalculationMethod.MWL,
    val isRecommendedMethod: Boolean = true,
    val customFajrAngle: Double = RegionSelection.DefaultCustomAngle,
    val customIshaAngle: Double = RegionSelection.DefaultCustomAngle,
    /** False while the times are for the built-in default city rather than this device. */
    val usingDeviceLocation: Boolean = false,
    val hasLocationPermission: Boolean = false,
    val regionFinder: RegionFinderState? = null,
    val showCustomAnglesDialog: Boolean = false
)

@HiltViewModel
class PrayerTimesViewModel @Inject constructor(
    private val settings: PrayerTimesSettingsRepository,
    private val locationProvider: LocationProvider,
    private val getPrayerTimes: GetPrayerTimesUseCase,
    private val getRecommendedMethod: GetRecommendedMethodUseCase,
    private val searchRegion: SearchRegionUseCase
) : ViewModel() {

    private val location = MutableStateFlow<LocationData?>(null)
    private val hasLocationPermission = MutableStateFlow(locationProvider.hasPermission())
    private val regionFinder = MutableStateFlow<RegionFinderState?>(null)
    private val customDialog = MutableStateFlow(false)
    private val hasLoaded = MutableStateFlow(false)

    val uiState: StateFlow<PrayerTimesUiState> = combine(
        settings.observeSelection(),
        location,
        hasLocationPermission,
        combine(regionFinder, customDialog, hasLoaded) { finder, custom, loaded -> Triple(finder, custom, loaded) }
    ) { selection, device, permission, dialogs ->
        val method = getRecommendedMethod.effectiveMethod(selection)
        val region = getRecommendedMethod(selection.countryCode)
        PrayerTimesUiState(
            hasLoaded = dialogs.third,
            times = getPrayerTimes(
                date = LocalDate.now(),
                location = device ?: LocationData.Default,
                method = method,
                hanafiAsr = getRecommendedMethod.usesHanafiAsr(selection),
                customFajrAngle = selection.customFajrAngle,
                customIshaAngle = selection.customIshaAngle
            ),
            region = region,
            method = method,
            isRecommendedMethod = selection.methodOverride == null,
            customFajrAngle = selection.customFajrAngle,
            customIshaAngle = selection.customIshaAngle,
            usingDeviceLocation = device != null,
            hasLocationPermission = permission,
            regionFinder = dialogs.first,
            showCustomAnglesDialog = dialogs.second
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PrayerTimesUiState())

    init {
        viewModelScope.launch {
            val seen = settings.observeHasSeenRegionFinder().first()
            hasLoaded.value = true
            if (!seen) openRegionFinder(suggestedCountry = deviceCountry())
        }
        refreshLocation()
    }

    /** Called once the screen knows the permission answer (granted now, or already held). */
    fun refreshLocation() {
        hasLocationPermission.value = locationProvider.hasPermission()
        viewModelScope.launch { locationProvider.currentLocation()?.let { location.value = it } }
    }

    // ---- Region finder ----

    /**
     * Opens the dialog. With nothing chosen yet, the country the phone's own region setting
     * names is offered as a pre-selection: read locally, no location or network involved.
     */
    fun openRegionFinder(suggestedCountry: String? = null) {
        viewModelScope.launch {
            val current = settings.observeSelection().first()
            val preselect = getRecommendedMethod(current.countryCode ?: suggestedCountry)
            regionFinder.value = RegionFinderState(
                query = "",
                results = searchRegion(""),
                selected = preselect,
                chosenMethod = if (current.countryCode != null) current.methodOverride else null
            )
        }
    }

    fun onSearchRegion(query: String) {
        regionFinder.update { it?.copy(query = query, results = searchRegion(query)) }
    }

    fun onRegionTapped(region: RegionInfo) {
        regionFinder.update { it?.copy(selected = region, chosenMethod = null) }
    }

    fun onMethodTapped(method: PrayerCalculationMethod) {
        regionFinder.update { it?.copy(chosenMethod = method) }
    }

    /** Saves the previewed country and method. */
    fun onRegionConfirmed() {
        val finder = regionFinder.value ?: return
        val region = finder.selected ?: return
        viewModelScope.launch {
            settings.setCountry(region.code)
            finder.chosenMethod?.takeIf { it != region.recommended }?.let { settings.setMethod(it) }
            regionFinder.value = null
        }
    }

    fun onRegionFinderDismissed() {
        regionFinder.value = null
        viewModelScope.launch { settings.markRegionFinderSeen() }
    }

    // ---- Custom angles ----

    fun openCustomAngles() {
        regionFinder.value = null
        customDialog.value = true
    }

    fun onCustomAnglesDismissed() {
        customDialog.value = false
    }

    /** Returns false (and saves nothing) if either angle isn't a sensible number of degrees. */
    fun onCustomAngles(fajr: Double, isha: Double): Boolean {
        if (fajr !in ValidAngles || isha !in ValidAngles) return false
        viewModelScope.launch {
            settings.setCustomAngles(fajr, isha)
            customDialog.value = false
        }
        return true
    }

    private fun deviceCountry(): String? =
        Locale.getDefault().country.takeIf { it.isNotBlank() }

    companion object {
        val ValidAngles = 5.0..25.0
    }
}
