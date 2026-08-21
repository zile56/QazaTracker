package com.example.qazatracker.ui.baseline

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.qazatracker.domain.model.PrayerType
import com.example.qazatracker.ui.common.displayName
import com.example.qazatracker.ui.theme.QazaShapes

@Composable
fun BaselineSummaryScreen(
    modifier: Modifier = Modifier,
    viewModel: BaselineSummaryViewModel = hiltViewModel(),
    onBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    BaselineSummaryContent(
        uiState = uiState,
        onBack = onBack,
        onCountChanged = viewModel::updateCount,
        onConfirmClicked = viewModel::confirmBaseline,
        modifier = modifier
    )
}

@Composable
fun BaselineSummaryContent(
    uiState: BaselineSummaryUiState,
    onBack: () -> Unit,
    onCountChanged: (PrayerType, String) -> Unit,
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

                Spacer(Modifier.height(4.dp))

                Text(
                    text = "Your baseline",
                    style = MaterialTheme.typography.headlineSmall.copy(fontSize = 26.sp, lineHeight = 30.sp)
                )

                Spacer(Modifier.height(6.dp))

                Text(
                    text = uiState.explainerText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(20.dp))

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    PrayerType.entries.forEachIndexed { index, prayerType ->
                        val isPrimaryTint = index % 2 == 0
                        BaselineRow(
                            prayerType = prayerType,
                            value = uiState.counts[prayerType].orEmpty(),
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
                            onValueChange = { onCountChanged(prayerType, it) }
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Total",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(text = uiState.total.toString(), style = MaterialTheme.typography.titleLarge)
                }

                if (uiState.isConfirmed) {
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "Baseline saved.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                Spacer(Modifier.height(16.dp))
            }

            Column(modifier = Modifier.padding(horizontal = 22.dp, vertical = 16.dp)) {
                Button(
                    onClick = onConfirmClicked,
                    enabled = !uiState.isConfirmed,
                    shape = QazaShapes.pillShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(
                        text = if (uiState.isConfirmed) "Saved" else "Start tracking",
                        style = MaterialTheme.typography.labelLarge
                    )
                    if (!uiState.isConfirmed) {
                        Spacer(Modifier.width(6.dp))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun BaselineRow(
    prayerType: PrayerType,
    value: String,
    tintContainer: Color,
    tintContent: Color,
    onValueChange: (String) -> Unit
) {
    Surface(
        shape = QazaShapes.cardShape,
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .background(tintContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = prayerType.displayName().take(1),
                        style = MaterialTheme.typography.titleSmall,
                        color = tintContent
                    )
                }
                Text(text = prayerType.displayName(), style = MaterialTheme.typography.bodyLarge)
            }
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                shape = QazaShapes.pillShape,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                textStyle = MaterialTheme.typography.bodyMedium.copy(textAlign = TextAlign.End),
                modifier = Modifier.width(90.dp)
            )
        }
    }
}
