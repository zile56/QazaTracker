package com.zilehasnain.qazatracker.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zilehasnain.qazatracker.domain.model.CompletionProjection
import com.zilehasnain.qazatracker.domain.model.PrayerType
import com.zilehasnain.qazatracker.ui.common.displayName
import com.zilehasnain.qazatracker.ui.theme.QazaShapes
import kotlin.math.roundToInt

@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = hiltViewModel(),
    onLogBatchClicked: () -> Unit = {},
    onHistoryClicked: () -> Unit = {},
    onSettingsClicked: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    DashboardContent(
        uiState = uiState,
        onQuickLog = viewModel::onQuickLog,
        onLogBatchClicked = onLogBatchClicked,
        onHistoryClicked = onHistoryClicked,
        onSettingsClicked = onSettingsClicked,
        onAdjustClicked = viewModel::onAdjustClicked,
        onAdjustmentSignChanged = viewModel::onAdjustmentSignChanged,
        onAdjustmentMagnitudeChanged = viewModel::onAdjustmentMagnitudeChanged,
        onAdjustmentNoteChanged = viewModel::onAdjustmentNoteChanged,
        onConfirmAdjustment = viewModel::onConfirmAdjustment,
        onDismissAdjustmentDialog = viewModel::onDismissAdjustmentDialog,
        modifier = modifier
    )
}

@Composable
fun DashboardContent(
    uiState: DashboardUiState,
    onQuickLog: (PrayerType) -> Unit = {},
    onLogBatchClicked: () -> Unit = {},
    onHistoryClicked: () -> Unit = {},
    onSettingsClicked: () -> Unit = {},
    onAdjustClicked: (PrayerType) -> Unit = {},
    onAdjustmentSignChanged: (Boolean) -> Unit = {},
    onAdjustmentMagnitudeChanged: (String) -> Unit = {},
    onAdjustmentNoteChanged: (String) -> Unit = {},
    onConfirmAdjustment: () -> Unit = {},
    onDismissAdjustmentDialog: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp)
                .padding(top = 8.dp, bottom = 28.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "QAZA TRACKER",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Row {
                    IconButton(onClick = onHistoryClicked, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.List,
                            contentDescription = "History",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(onClick = onSettingsClicked, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(18.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = uiState.totalRemaining.toString(),
                    style = MaterialTheme.typography.headlineLarge.copy(fontSize = 56.sp, lineHeight = 58.sp)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "prayers remaining",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(14.dp))
                ProjectionBadge(uiState.projection)
                Spacer(Modifier.height(14.dp))
                OutlinedButton(onClick = onLogBatchClicked, shape = QazaShapes.pillShape) {
                    Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Log multiple days")
                }
            }

            Spacer(Modifier.height(20.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(Modifier.height(16.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                uiState.rows.forEachIndexed { index, row ->
                    val isPrimaryTint = index % 2 == 0
                    PrayerProgressRow(
                        row = row,
                        tintContainer = if (isPrimaryTint) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.secondaryContainer
                        },
                        tintContent = if (isPrimaryTint) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSecondaryContainer
                        },
                        onQuickLog = { onQuickLog(row.prayerType) },
                        onAdjustClicked = { onAdjustClicked(row.prayerType) }
                    )
                }
            }
        }

        uiState.adjustmentDialog?.let { dialog ->
            AdjustmentDialog(
                dialog = dialog,
                onSignChanged = onAdjustmentSignChanged,
                onMagnitudeChanged = onAdjustmentMagnitudeChanged,
                onNoteChanged = onAdjustmentNoteChanged,
                onConfirm = onConfirmAdjustment,
                onDismiss = onDismissAdjustmentDialog
            )
        }
    }
}

@Composable
private fun ProjectionBadge(projection: CompletionProjection) {
    val icon = if (projection is CompletionProjection.AlreadyCaughtUp) Icons.Default.Check else Icons.Default.DateRange
    Surface(
        shape = QazaShapes.pillShape,
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = projection.displayText(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
private fun PrayerProgressRow(
    row: PrayerRowUiState,
    tintContainer: Color,
    tintContent: Color,
    onQuickLog: () -> Unit,
    onAdjustClicked: () -> Unit
) {
    Surface(
        shape = QazaShapes.cardShape,
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .background(tintContainer, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = row.prayerType.displayName().take(1),
                            style = MaterialTheme.typography.titleSmall,
                            color = tintContent
                        )
                    }
                    Column {
                        Text(text = row.prayerType.displayName(), style = MaterialTheme.typography.bodyLarge)
                        Text(
                            text = "${row.remaining} left · ${row.completed} done",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onAdjustClicked,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Adjust ${row.prayerType.displayName()}",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(
                        onClick = onQuickLog,
                        modifier = Modifier
                            .size(40.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Log one ${row.prayerType.displayName()}",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
            Spacer(Modifier.height(9.dp))
            LinearProgressIndicator(
                progress = { row.progressFraction },
                color = MaterialTheme.colorScheme.secondary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                strokeCap = StrokeCap.Round,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(50))
            )
        }
    }
}

@Composable
private fun AdjustmentDialog(
    dialog: AdjustmentDialogState,
    onSignChanged: (Boolean) -> Unit,
    onMagnitudeChanged: (String) -> Unit,
    onNoteChanged: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Adjust ${dialog.prayerType.displayName()}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Use + to add missed prayers back, − to correct an overcount.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = !dialog.isNegative,
                        onClick = { onSignChanged(false) },
                        label = { Text("+") }
                    )
                    FilterChip(
                        selected = dialog.isNegative,
                        onClick = { onSignChanged(true) },
                        label = { Text("−") }
                    )
                }
                OutlinedTextField(
                    value = dialog.magnitudeInput,
                    onValueChange = onMagnitudeChanged,
                    label = { Text("Amount") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = dialog.note,
                    onValueChange = onNoteChanged,
                    label = { Text("Note (optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = dialog.canConfirm) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun CompletionProjection.displayText(): String = when (this) {
    CompletionProjection.AlreadyCaughtUp -> "You are fully caught up"
    CompletionProjection.InsufficientData -> "Log a few prayers to see your pace"
    is CompletionProjection.Estimated -> {
        val months = (daysRemaining / 30.0).roundToInt()
        if (months < 1) {
            "At your pace, cleared in under a month"
        } else {
            "At your pace, cleared in ~$months month${if (months == 1) "" else "s"}"
        }
    }
}
