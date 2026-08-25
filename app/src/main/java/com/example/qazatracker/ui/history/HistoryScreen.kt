package com.example.qazatracker.ui.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.layout.height
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.qazatracker.domain.model.HistoryEntry
import com.example.qazatracker.domain.model.PrayerType
import com.example.qazatracker.ui.common.displayName
import com.example.qazatracker.ui.theme.QazaShapes
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun HistoryScreen(
    modifier: Modifier = Modifier,
    viewModel: HistoryViewModel = hiltViewModel(),
    onBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    HistoryContent(uiState = uiState, onBack = onBack, modifier = modifier)
}

@Composable
fun HistoryContent(
    uiState: HistoryUiState,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 22.dp)
                .padding(top = 12.dp)
        ) {
            TextButton(onClick = onBack, contentPadding = PaddingValues(0.dp)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.size(4.dp))
                Text("Back")
            }

            Spacer(Modifier.height(12.dp))

            Text(
                text = "History",
                style = MaterialTheme.typography.headlineSmall.copy(fontSize = 24.sp, lineHeight = 28.sp),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (uiState.entries.isEmpty()) {
                Text(
                    text = "Nothing logged yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(uiState.entries, key = { it.timelineKey() }) { entry ->
                        HistoryRow(entry)
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryRow(entry: HistoryEntry) {
    val (tagLabel, tagColor, tagContentColor) = entry.tagStyle()

    Surface(
        shape = QazaShapes.cardShape,
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = entry.title(),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f)
                )
                Surface(shape = QazaShapes.pillShape, color = tagColor) {
                    Text(
                        text = tagLabel,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = tagContentColor,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                    )
                }
            }
            Spacer(Modifier.height(3.dp))
            Text(
                text = entry.timestamp.formatForHistory(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun HistoryEntry.timelineKey(): String = when (this) {
    is HistoryEntry.SingleCompletion -> "single-$prayerType-$timestamp"
    is HistoryEntry.BatchCompletion -> "batch-$batchId"
    is HistoryEntry.Adjustment -> "adjustment-$prayerType-$timestamp-$delta"
}

private fun HistoryEntry.title(): String = when (this) {
    is HistoryEntry.SingleCompletion -> "${prayerType.displayName()} completed"

    is HistoryEntry.BatchCompletion -> {
        val prayerLabel = if (prayerTypes.size == PrayerType.entries.size) {
            "all prayers"
        } else {
            prayerTypes.joinToString(", ") { it.displayName() }
        }
        val dayWord = if (days == 1) "day" else "days"
        "$days $dayWord × $prayerLabel logged"
    }

    is HistoryEntry.Adjustment -> {
        val sign = if (delta > 0) "+" else ""
        "${prayerType.displayName()} adjusted $sign$delta"
    }
}

@Composable
private fun HistoryEntry.tagStyle(): Triple<String, Color, Color> = when (this) {
    is HistoryEntry.SingleCompletion -> Triple(
        "Completed",
        MaterialTheme.colorScheme.secondaryContainer,
        MaterialTheme.colorScheme.onSecondaryContainer
    )

    is HistoryEntry.BatchCompletion -> Triple(
        "Batch",
        MaterialTheme.colorScheme.primaryContainer,
        MaterialTheme.colorScheme.onPrimaryContainer
    )

    is HistoryEntry.Adjustment -> Triple(
        reason.displayName(),
        MaterialTheme.colorScheme.surfaceVariant,
        MaterialTheme.colorScheme.onSurfaceVariant
    )
}

private val historyTimestampFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy · h:mm a", Locale.getDefault())

private fun Instant.formatForHistory(): String =
    atZone(ZoneId.systemDefault()).format(historyTimestampFormatter)
