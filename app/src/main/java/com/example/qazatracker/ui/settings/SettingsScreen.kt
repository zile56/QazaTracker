package com.example.qazatracker.ui.settings

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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.qazatracker.domain.model.NotificationFrequency
import com.example.qazatracker.ui.common.displayName
import com.example.qazatracker.ui.theme.QazaShapes

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
    onBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    SettingsContent(
        uiState = uiState,
        onBack = onBack,
        onFrequencySelected = viewModel::onFrequencySelected,
        onExportClicked = viewModel::onExportClicked,
        modifier = modifier
    )
}

@Composable
fun SettingsContent(
    uiState: SettingsUiState,
    onBack: () -> Unit = {},
    onFrequencySelected: (NotificationFrequency) -> Unit = {},
    onExportClicked: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
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
                text = "Settings",
                style = MaterialTheme.typography.headlineSmall.copy(fontSize = 24.sp, lineHeight = 28.sp),
                modifier = Modifier.padding(bottom = 20.dp)
            )

            SettingsSection(title = "Reminder frequency") {
                Text(
                    text = "How often to nudge you about your remaining prayers.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(10.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.horizontalScroll(rememberScrollState())
                ) {
                    NotificationFrequency.entries.forEach { frequency ->
                        FilterChip(
                            selected = uiState.notificationFrequency == frequency,
                            onClick = { onFrequencySelected(frequency) },
                            label = { Text(frequency.displayName(), maxLines = 1, softWrap = false) }
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(Modifier.height(20.dp))

            SettingsSection(title = "Backup") {
                Text(
                    text = "Export every logged prayer, adjustment, and baseline as a JSON file " +
                        "you can keep as a backup.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(10.dp))
                OutlinedButton(
                    onClick = onExportClicked,
                    shape = QazaShapes.pillShape,
                    enabled = uiState.exportState !is ExportState.Exporting
                ) {
                    if (uiState.exportState is ExportState.Exporting) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                    Spacer(Modifier.size(6.dp))
                    Text("Export data")
                }
                Spacer(Modifier.height(8.dp))
                ExportStatusText(uiState.exportState)
            }

            Spacer(Modifier.height(20.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(Modifier.height(20.dp))

            SettingsSection(title = "App version") {
                Text(
                    text = uiState.versionName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(20.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(Modifier.height(20.dp))

            SettingsSection(title = "About") {
                Text(
                    text = "Qaza Tracker helps you calculate, track, and systematically complete " +
                        "missed (Qaza) prayers — a debt-clearing tool for working down a backlog, " +
                        "not a daily habit checklist.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable () -> Unit) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        content()
    }
}

@Composable
private fun ExportStatusText(exportState: ExportState) {
    when (exportState) {
        is ExportState.Success -> Text(
            text = "Saved ${exportState.recordCount} records to ${exportState.filePath}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.secondary
        )

        is ExportState.Failure -> Text(
            text = "Export failed: ${exportState.message}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error
        )

        ExportState.Exporting, ExportState.Idle -> Unit
    }
}
