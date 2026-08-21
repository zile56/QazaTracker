package com.example.qazatracker.ui.dashboard

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.qazatracker.domain.model.CompletionProjection
import com.example.qazatracker.ui.common.displayName
import com.example.qazatracker.ui.theme.QazaShapes
import kotlin.math.roundToInt

@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    DashboardContent(uiState = uiState, modifier = modifier)
}

@Composable
fun DashboardContent(uiState: DashboardUiState, modifier: Modifier = Modifier) {
    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp)
                .padding(top = 8.dp, bottom = 28.dp)
        ) {
            Text(
                text = "QAZA TRACKER",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )

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
                        }
                    )
                }
            }
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
    tintContent: Color
) {
    Surface(
        shape = QazaShapes.cardShape,
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
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
