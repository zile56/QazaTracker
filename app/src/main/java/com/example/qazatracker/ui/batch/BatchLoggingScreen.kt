package com.example.qazatracker.ui.batch

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.qazatracker.domain.model.PrayerType
import com.example.qazatracker.ui.common.displayName
import com.example.qazatracker.ui.theme.QazaShapes

@Composable
fun BatchLoggingScreen(
    modifier: Modifier = Modifier,
    viewModel: BatchLoggingViewModel = hiltViewModel(),
    onBack: () -> Unit = {},
    onLogged: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isComplete) {
        if (uiState.isComplete) onLogged()
    }

    BatchLoggingContent(
        uiState = uiState,
        onBack = onBack,
        onIncrementDays = viewModel::incrementDays,
        onDecrementDays = viewModel::decrementDays,
        onDaysChanged = viewModel::setDays,
        onTogglePrayerType = viewModel::togglePrayerType,
        onConfirmClicked = viewModel::confirm,
        modifier = modifier
    )
}

@Composable
fun BatchLoggingContent(
    uiState: BatchLoggingUiState,
    onBack: () -> Unit,
    onIncrementDays: () -> Unit,
    onDecrementDays: () -> Unit,
    onDaysChanged: (Int) -> Unit,
    onTogglePrayerType: (PrayerType) -> Unit,
    onConfirmClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 22.dp)
                    .padding(top = 12.dp)
            ) {
                TextButton(onClick = onBack, contentPadding = PaddingValues(0.dp)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Back")
                }

                Spacer(Modifier.height(12.dp))

                Text(
                    text = "Log multiple days",
                    style = MaterialTheme.typography.headlineSmall.copy(fontSize = 24.sp, lineHeight = 28.sp)
                )

                Spacer(Modifier.height(24.dp))

                Text(
                    text = "How many days?",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedIconButton(onClick = onDecrementDays, modifier = Modifier.size(44.dp)) {
                        Text("−", style = MaterialTheme.typography.titleLarge)
                    }
                    Text(
                        text = uiState.days.toString(),
                        style = MaterialTheme.typography.headlineMedium.copy(fontSize = 40.sp),
                        modifier = Modifier
                            .wrapContentWidth()
                            .padding(horizontal = 24.dp)
                    )
                    OutlinedIconButton(onClick = onIncrementDays, modifier = Modifier.size(44.dp)) {
                        Text("+", style = MaterialTheme.typography.titleLarge)
                    }
                }

                Spacer(Modifier.height(16.dp))

                Slider(
                    value = uiState.days.toFloat(),
                    onValueChange = { onDaysChanged(it.toInt()) },
                    valueRange = BatchLoggingUiState.DAYS_RANGE.first.toFloat()..BatchLoggingUiState.DAYS_RANGE.last.toFloat(),
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary,
                        inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )

                Spacer(Modifier.height(24.dp))

                Text(
                    text = "Which prayers?",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(10.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PrayerType.entries.forEach { prayerType ->
                        val selected = prayerType in uiState.selectedPrayerTypes
                        FilterChip(
                            selected = selected,
                            onClick = { onTogglePrayerType(prayerType) },
                            label = { Text(prayerType.displayName()) },
                            shape = QazaShapes.pillShape,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

                Surface(
                    shape = QazaShapes.cardShape,
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = batchSummaryText(uiState),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                Spacer(Modifier.height(16.dp))
            }

            Column(modifier = Modifier.padding(horizontal = 22.dp, vertical = 16.dp)) {
                Button(
                    onClick = onConfirmClicked,
                    enabled = uiState.canConfirm,
                    shape = QazaShapes.pillShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(text = "Log entries", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Normal)
                    Spacer(Modifier.width(6.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

private fun batchSummaryText(uiState: BatchLoggingUiState): String {
    if (uiState.selectedPrayerTypes.isEmpty()) {
        return "Select at least one prayer type to continue."
    }
    val dayWord = if (uiState.days == 1) "day" else "days"
    val prayerWord = if (uiState.selectedPrayerTypes.size == 1) "prayer type" else "prayer types"
    return "${uiState.days} $dayWord × ${uiState.selectedPrayerTypes.size} $prayerWord = " +
        "${uiState.totalEntries} completions logged."
}
