package com.zilehasnain.qazatracker.ui.statistics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zilehasnain.qazatracker.domain.model.MonthlyStatsData
import com.zilehasnain.qazatracker.domain.model.PrayerType
import com.zilehasnain.qazatracker.ui.common.displayName
import com.zilehasnain.qazatracker.ui.theme.Neutral600
import com.zilehasnain.qazatracker.ui.theme.QazaShapes
import com.zilehasnain.qazatracker.ui.theme.Sage500
import com.zilehasnain.qazatracker.ui.theme.Sage700
import com.zilehasnain.qazatracker.ui.theme.Terracotta500
import com.zilehasnain.qazatracker.ui.theme.Terracotta700
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

private val CHART_HEIGHT = 140.dp

@Composable
fun StatisticsScreen(
    modifier: Modifier = Modifier,
    viewModel: StatisticsViewModel = hiltViewModel(),
    onBack: () -> Unit = {}
) {
    val stats by viewModel.currentMonthStats.collectAsStateWithLifecycle()
    val selectedMonth by viewModel.selectedMonth.collectAsStateWithLifecycle()
    val earliestMonth by viewModel.earliestMonth.collectAsStateWithLifecycle()

    StatisticsContent(
        stats = stats,
        selectedMonth = selectedMonth,
        earliestMonth = earliestMonth,
        currentMonth = viewModel.currentMonth,
        onBack = onBack,
        onMonthChanged = viewModel::onMonthChanged,
        modifier = modifier
    )
}

@Composable
fun StatisticsContent(
    stats: MonthlyStatsData,
    selectedMonth: YearMonth,
    earliestMonth: YearMonth,
    currentMonth: YearMonth,
    onBack: () -> Unit,
    onMonthChanged: (YearMonth) -> Unit,
    modifier: Modifier = Modifier
) {
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
                Spacer(Modifier.width(4.dp))
                Text("Back")
            }

            Spacer(Modifier.height(12.dp))

            Text(
                text = "Statistics",
                style = MaterialTheme.typography.headlineSmall.copy(fontSize = 24.sp, lineHeight = 28.sp)
            )

            Spacer(Modifier.height(16.dp))

            MonthSelector(
                month = selectedMonth,
                canGoPrevious = selectedMonth.isAfter(minOf(earliestMonth, currentMonth)),
                canGoNext = selectedMonth.isBefore(currentMonth),
                onPrevious = { onMonthChanged(selectedMonth.minusMonths(1)) },
                onNext = { onMonthChanged(selectedMonth.plusMonths(1)) }
            )

            Spacer(Modifier.height(20.dp))

            TotalSummary(stats, selectedMonth)

            Spacer(Modifier.height(16.dp))

            Surface(
                shape = QazaShapes.cardShape,
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp)) {
                    Text(
                        text = "Completed per prayer",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(16.dp))
                    PrayerBarChart(stats)
                }
            }

            Spacer(Modifier.height(16.dp))

            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                PrayerType.entries.forEach { type -> PrayerStatRow(type, stats) }
            }
        }
    }
}

@Composable
private fun MonthSelector(
    month: YearMonth,
    canGoPrevious: Boolean,
    canGoNext: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPrevious, enabled = canGoPrevious) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous month")
        }
        Text(
            text = month.format(monthTitleFormatter),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = onNext, enabled = canGoNext) {
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next month")
        }
    }
}

@Composable
private fun TotalSummary(stats: MonthlyStatsData, month: YearMonth) {
    Surface(
        shape = QazaShapes.cardShape,
        color = MaterialTheme.colorScheme.secondaryContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
            Text(
                text = "${stats.totalCompleted} / ${stats.totalOwed} prayers completed " +
                    "(${formatPercent(stats.completionRate)})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Spacer(Modifier.height(10.dp))
            LinearProgressIndicator(
                progress = { (stats.completionRate / 100f).coerceIn(0f, 1f) },
                color = MaterialTheme.colorScheme.secondary,
                trackColor = MaterialTheme.colorScheme.surface,
                strokeCap = StrokeCap.Round,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(50))
            )
            if (stats.totalCompleted == 0) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "No prayers logged in ${month.format(monthTitleFormatter)}.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

/**
 * Bar height is relative to the month's busiest prayer type, not to what is owed — against a
 * multi-year backlog a month's worth would otherwise be an invisible sliver. The exact
 * counts and rates are in the rows below.
 */
@Composable
private fun PrayerBarChart(stats: MonthlyStatsData) {
    val maxCompleted = (stats.prayerStats.values.maxOrNull() ?: 0).coerceAtLeast(1)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(CHART_HEIGHT + 48.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        PrayerType.entries.forEach { type ->
            val count = stats.prayerStats[type] ?: 0
            val barHeight = if (count == 0) 3.dp else (CHART_HEIGHT * (count.toFloat() / maxCompleted)).coerceAtLeast(6.dp)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .width(28.dp)
                        .height(barHeight)
                        .background(type.chartColor(), RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = type.displayName(),
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun PrayerStatRow(type: PrayerType, stats: MonthlyStatsData) {
    val completed = stats.prayerStats[type] ?: 0
    val owed = stats.prayerTotals[type] ?: 0
    val rate = stats.rateFor(type)

    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(type.chartColor(), CircleShape)
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = type.displayName(),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "$completed / $owed (${formatPercent(rate)})",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { (rate / 100f).coerceIn(0f, 1f) },
            color = type.chartColor(),
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            strokeCap = StrokeCap.Round,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(50))
        )
    }
}

/** One colour per prayer type, all drawn from the app palette, distinguishable side by side. */
private fun PrayerType.chartColor(): Color = when (this) {
    PrayerType.FAJR -> Terracotta500
    PrayerType.DHUHR -> Sage500
    PrayerType.ASR -> Terracotta700
    PrayerType.MAGHRIB -> Sage700
    PrayerType.ISHA -> Neutral600
}

/**
 * Whole percent normally ("51%"), but one decimal below 10% so a real month against a large
 * backlog reads "1.4%" rather than a flat "1%", and "<0.1%" rather than a misleading "0.0%" for
 * a real but tiny share. Always Latin digits, like the rest of the app.
 */
internal fun formatPercent(rate: Float): String = when {
    rate <= 0f -> "0%"
    rate < 0.1f -> "<0.1%"
    rate < 9.95f -> String.format(Locale.US, "%.1f%%", rate)
    else -> "${rate.roundToInt()}%"
}

private val monthTitleFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())
