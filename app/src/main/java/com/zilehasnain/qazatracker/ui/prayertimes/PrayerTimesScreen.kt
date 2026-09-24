package com.zilehasnain.qazatracker.ui.prayertimes

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zilehasnain.qazatracker.domain.model.PrayerCalculationMethod
import com.zilehasnain.qazatracker.domain.model.PrayerTimeData
import com.zilehasnain.qazatracker.domain.model.PrayerType
import com.zilehasnain.qazatracker.ui.achievements.AchievementEmoji
import com.zilehasnain.qazatracker.ui.common.displayName
import com.zilehasnain.qazatracker.ui.theme.QazaShapes
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

private const val SUNRISE_EMOJI = "🌄" // 🌄

@Composable
fun PrayerTimesScreen(
    modifier: Modifier = Modifier,
    viewModel: PrayerTimesViewModel = hiltViewModel(),
    onBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        viewModel.refreshLocation()
    }

    PrayerTimesContent(
        uiState = uiState,
        onBack = onBack,
        onAllowLocation = { permissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION) },
        onChangeRegion = { viewModel.openRegionFinder() },
        onSearch = viewModel::onSearchRegion,
        onRegionTapped = viewModel::onRegionTapped,
        onMethodTapped = viewModel::onMethodTapped,
        onRegionConfirmed = viewModel::onRegionConfirmed,
        onRegionFinderDismissed = viewModel::onRegionFinderDismissed,
        onOpenCustomAngles = viewModel::openCustomAngles,
        onCustomAngles = viewModel::onCustomAngles,
        onCustomAnglesDismissed = viewModel::onCustomAnglesDismissed,
        modifier = modifier
    )
}

@Composable
fun PrayerTimesContent(
    uiState: PrayerTimesUiState,
    onBack: () -> Unit = {},
    onAllowLocation: () -> Unit = {},
    onChangeRegion: () -> Unit = {},
    onSearch: (String) -> Unit = {},
    onRegionTapped: (com.zilehasnain.qazatracker.domain.model.RegionInfo) -> Unit = {},
    onMethodTapped: (PrayerCalculationMethod) -> Unit = {},
    onRegionConfirmed: () -> Unit = {},
    onRegionFinderDismissed: () -> Unit = {},
    onOpenCustomAngles: () -> Unit = {},
    onCustomAngles: (Double, Double) -> Boolean = { _, _ -> true },
    onCustomAnglesDismissed: () -> Unit = {},
    today: LocalDate = LocalDate.now(),
    zone: ZoneId = ZoneId.systemDefault(),
    modifier: Modifier = Modifier
) {
    uiState.regionFinder?.let { finder ->
        FindRegionDialog(
            state = finder,
            onSearch = onSearch,
            onRegionTapped = onRegionTapped,
            onMethodTapped = onMethodTapped,
            onConfirm = onRegionConfirmed,
            onCustomAngles = onOpenCustomAngles,
            onDismiss = onRegionFinderDismissed
        )
    }
    if (uiState.showCustomAnglesDialog) {
        CustomAnglesDialog(
            initialFajr = uiState.customFajrAngle,
            initialIsha = uiState.customIshaAngle,
            onSave = onCustomAngles,
            onDismiss = onCustomAnglesDismissed
        )
    }

    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp)
                .padding(top = 12.dp, bottom = 28.dp)
        ) {
            TextButton(onClick = onBack, contentPadding = PaddingValues(0.dp)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.size(4.dp))
                Text("Back")
            }

            Spacer(Modifier.height(12.dp))
            Text(
                text = "Prayer times",
                style = MaterialTheme.typography.headlineSmall.copy(fontSize = 24.sp, lineHeight = 28.sp)
            )
            Text(
                text = today.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL)),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp, bottom = 16.dp)
            )

            MethodSummary(uiState)
            Spacer(Modifier.height(16.dp))

            val times = uiState.times
            if (times != null) {
                TimesList(times, zone)
            } else if (uiState.hasLoaded) {
                Text(
                    text = "Times can't be worked out for this place today. Try a different method.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(16.dp))
            LocationNote(uiState, onAllowLocation)

            Spacer(Modifier.height(16.dp))
            OutlinedButton(
                onClick = onChangeRegion,
                shape = QazaShapes.pillShape,
                modifier = Modifier.fillMaxWidth()
            ) { Text("Change region / method") }

            Spacer(Modifier.height(12.dp))
            Text(
                text = "Calculated on this device. No internet is used and your location never leaves it.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun MethodSummary(uiState: PrayerTimesUiState) {
    val method = uiState.method
    val region = uiState.region
    val headline = when {
        method == PrayerCalculationMethod.CUSTOM -> "Using custom angles"
        region != null && uiState.isRecommendedMethod ->
            "Using ${method.displayName} (recommended for ${region.name})"
        region != null -> "Using ${method.displayName} (your choice for ${region.name})"
        else -> "Using ${method.displayName}"
    }
    val angles = if (method == PrayerCalculationMethod.CUSTOM) {
        method.anglesSummary(uiState.customFajrAngle, uiState.customIshaAngle)
    } else {
        method.anglesSummary()
    }
    Column {
        Text(headline, style = MaterialTheme.typography.titleMedium)
        Text(
            text = "Fajr / Isha: $angles",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun TimesList(times: PrayerTimeData, zone: ZoneId) {
    val formatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withLocale(Locale.getDefault())
    fun format(instant: Instant): String = formatter.format(instant.atZone(zone))

    Column {
        TimeRow(AchievementEmoji.forPrayer(PrayerType.FAJR), PrayerType.FAJR.displayName(), format(times.fajr))
        TimeRow(SUNRISE_EMOJI, "Sunrise", format(times.sunrise), dimmed = true)
        listOf(PrayerType.DHUHR, PrayerType.ASR, PrayerType.MAGHRIB, PrayerType.ISHA).forEach { prayer ->
            TimeRow(AchievementEmoji.forPrayer(prayer), prayer.displayName(), format(times.timeFor(prayer)))
        }
    }
}

@Composable
private fun TimeRow(emoji: String, name: String, time: String, dimmed: Boolean = false) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        ) {
            Text("$emoji  $name", style = MaterialTheme.typography.bodyLarge)
            Text(
                text = time,
                style = MaterialTheme.typography.titleMedium,
                color = if (dimmed) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
            )
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }
}

@Composable
private fun LocationNote(uiState: PrayerTimesUiState, onAllowLocation: () -> Unit) {
    if (uiState.usingDeviceLocation) {
        Text(
            text = "Using your approximate location.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        return
    }
    Column {
        Text(
            text = if (uiState.hasLocationPermission) {
                "Your phone has no location fix yet, so these times are for Rawalpindi."
            } else {
                "These times are for Rawalpindi until you share your approximate location."
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error
        )
        if (!uiState.hasLocationPermission) {
            TextButton(onClick = onAllowLocation, contentPadding = PaddingValues(0.dp)) {
                Text("Use my approximate location")
            }
        }
    }
}
