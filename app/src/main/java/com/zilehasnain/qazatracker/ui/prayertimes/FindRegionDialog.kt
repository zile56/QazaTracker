package com.zilehasnain.qazatracker.ui.prayertimes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zilehasnain.qazatracker.domain.model.PrayerCalculationMethod
import com.zilehasnain.qazatracker.domain.model.RegionInfo

private const val CHECK = "✓" // ✓
private const val ALTERNATIVE_BADGE = "Alternative"

/** "Find Your Region": search a country, see its suggested method, or pick an alternative. */
@Composable
fun FindRegionDialog(
    state: RegionFinderState,
    onSearch: (String) -> Unit,
    onRegionTapped: (RegionInfo) -> Unit,
    onMethodTapped: (PrayerCalculationMethod) -> Unit,
    onConfirm: () -> Unit,
    onCustomAngles: () -> Unit,
    onDismiss: () -> Unit
) {
    var showAlternatives by remember(state.selected?.code) { mutableStateOf(false) }
    val selected = state.selected

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Find your region") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = state.query,
                    onValueChange = onSearch,
                    label = { Text("Search country") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (state.results.isEmpty()) {
                    Text(
                        text = "No country found. If yours isn't listed, you can enter the Fajr and " +
                            "Isha angles your local authority uses.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    TextButton(onClick = onCustomAngles) { Text("Enter custom angles") }
                } else {
                    LazyColumn(modifier = Modifier.heightIn(max = 180.dp)) {
                        items(state.results, key = { it.code }) { region ->
                            val isSelected = region.code == selected?.code
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onRegionTapped(region) }
                                    .padding(vertical = 8.dp)
                            ) {
                                Text(region.flag, modifier = Modifier.padding(end = 10.dp))
                                Text(
                                    text = region.name,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )
                                if (isSelected) Text(CHECK, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }

                if (selected != null) {
                    MethodDetails(
                        region = selected,
                        chosenMethod = state.chosenMethod ?: selected.recommended,
                        showAlternatives = showAlternatives,
                        onSeeAlternatives = { showAlternatives = true },
                        onMethodTapped = onMethodTapped
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = onConfirm, enabled = selected != null) { Text("Confirm") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Not now") } }
    )
}

@Composable
private fun MethodDetails(
    region: RegionInfo,
    chosenMethod: PrayerCalculationMethod,
    showAlternatives: Boolean,
    onSeeAlternatives: () -> Unit,
    onMethodTapped: (PrayerCalculationMethod) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        MethodRow(
            method = region.recommended,
            tag = "$CHECK Recommended",
            highlighted = chosenMethod == region.recommended,
            tagColor = MaterialTheme.colorScheme.secondary,
            onClick = { onMethodTapped(region.recommended) }
        )

        if (showAlternatives) {
            region.alternatives.forEach { alternative ->
                MethodRow(
                    method = alternative,
                    tag = ALTERNATIVE_BADGE,
                    highlighted = chosenMethod == alternative,
                    tagColor = MaterialTheme.colorScheme.tertiary,
                    onClick = { onMethodTapped(alternative) }
                )
            }
        } else if (region.alternatives.isNotEmpty()) {
            TextButton(onClick = onSeeAlternatives) { Text("See alternatives") }
        }

        region.note?.let {
            Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(
            text = "Not sure? Ask your local mosque which method they follow.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun MethodRow(
    method: PrayerCalculationMethod,
    tag: String,
    highlighted: Boolean,
    tagColor: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = method.displayName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (highlighted) FontWeight.SemiBold else FontWeight.Normal
            )
            Text(
                text = method.anglesSummary(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.size(8.dp))
        AssistChip(
            onClick = onClick,
            label = { Text(if (highlighted) "$tag ●" else tag, color = tagColor, maxLines = 1, softWrap = false) }
        )
    }
}
