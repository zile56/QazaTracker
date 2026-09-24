package com.zilehasnain.qazatracker.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zilehasnain.qazatracker.domain.model.CompletionEstimate
import com.zilehasnain.qazatracker.domain.model.EstimateConfidence
import com.zilehasnain.qazatracker.domain.model.EstimateHorizon
import com.zilehasnain.qazatracker.ui.common.formatPercent
import com.zilehasnain.qazatracker.ui.theme.PaceGreenAccent
import com.zilehasnain.qazatracker.ui.theme.PaceGreenAccentDark
import com.zilehasnain.qazatracker.ui.theme.PaceGreenContainer
import com.zilehasnain.qazatracker.ui.theme.PaceGreenContainerDark
import com.zilehasnain.qazatracker.ui.theme.PaceGreenContent
import com.zilehasnain.qazatracker.ui.theme.PaceGreenContentDark
import com.zilehasnain.qazatracker.ui.theme.PaceOrangeAccent
import com.zilehasnain.qazatracker.ui.theme.PaceOrangeAccentDark
import com.zilehasnain.qazatracker.ui.theme.PaceOrangeContainer
import com.zilehasnain.qazatracker.ui.theme.PaceOrangeContainerDark
import com.zilehasnain.qazatracker.ui.theme.PaceOrangeContent
import com.zilehasnain.qazatracker.ui.theme.PaceOrangeContentDark
import com.zilehasnain.qazatracker.ui.theme.PaceYellowAccent
import com.zilehasnain.qazatracker.ui.theme.PaceYellowAccentDark
import com.zilehasnain.qazatracker.ui.theme.PaceYellowContainer
import com.zilehasnain.qazatracker.ui.theme.PaceYellowContainerDark
import com.zilehasnain.qazatracker.ui.theme.PaceYellowContent
import com.zilehasnain.qazatracker.ui.theme.PaceYellowContentDark
import com.zilehasnain.qazatracker.ui.theme.QazaShapes
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

internal data class PaceColors(val container: Color, val content: Color, val accent: Color)

internal fun paceColorsFor(horizon: EstimateHorizon, darkTheme: Boolean): PaceColors = when (horizon) {
    EstimateHorizon.WITHIN_THREE_MONTHS ->
        if (darkTheme) PaceColors(PaceGreenContainerDark, PaceGreenContentDark, PaceGreenAccentDark)
        else PaceColors(PaceGreenContainer, PaceGreenContent, PaceGreenAccent)

    EstimateHorizon.THREE_TO_SIX_MONTHS ->
        if (darkTheme) PaceColors(PaceYellowContainerDark, PaceYellowContentDark, PaceYellowAccentDark)
        else PaceColors(PaceYellowContainer, PaceYellowContent, PaceYellowAccent)

    EstimateHorizon.OVER_SIX_MONTHS ->
        if (darkTheme) PaceColors(PaceOrangeContainerDark, PaceOrangeContentDark, PaceOrangeAccentDark)
        else PaceColors(PaceOrangeContainer, PaceOrangeContent, PaceOrangeAccent)
}

/**
 * The dashboard's headline: when the current pace finishes the backlog, colour-coded by how far
 * off that is, with a timeline of how much of the way there has been covered. Tapping opens the
 * numbers behind it. No red anywhere: a distant date is orange, not an alarm.
 */
@Composable
internal fun CompletionEstimateCard(
    estimate: CompletionEstimate,
    modifier: Modifier = Modifier,
    today: LocalDate = LocalDate.now()
) {
    var showBreakdown by rememberSaveable { mutableStateOf(false) }
    val darkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val colors = paceColorsFor(estimate.horizon(today), darkTheme)

    Surface(
        onClick = { showBreakdown = true },
        shape = QazaShapes.cardShape,
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "You'll catch up by",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            Surface(shape = QazaShapes.pillShape, color = colors.container) {
                Text(
                    text = formatEstimateDate(estimate.estimatedDate, today),
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 24.sp, lineHeight = 30.sp),
                    fontWeight = FontWeight.SemiBold,
                    color = colors.content,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 6.dp)
                )
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = "${formatRemaining(estimate.daysRemaining)} remaining at current pace",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = formatPace(estimate.averagePrayersPerDay),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            if (estimate.confidence == EstimateConfidence.LOW) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Based on limited history",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
            Spacer(Modifier.height(16.dp))
            EstimateTimeline(estimate, colors, today)
            Spacer(Modifier.height(10.dp))
            Text(
                text = "Tap for breakdown",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }

    if (showBreakdown) {
        EstimateBreakdownDialog(estimate, today, onDismiss = { showBreakdown = false })
    }
}

/**
 * From when tracking started to the estimated finish, filled to where today falls. A "Today"
 * label sits above the end of the fill by splitting a row's width in the same ratio.
 */
@Composable
private fun EstimateTimeline(estimate: CompletionEstimate, colors: PaceColors, today: LocalDate) {
    val fraction = estimate.fractionElapsed

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Spacer(Modifier.weight(fraction.coerceAtLeast(0.001f)))
            Text(
                text = "Today",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = colors.accent,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.weight((1f - fraction).coerceAtLeast(0.001f)))
        }
        Spacer(Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { fraction },
            color = colors.accent,
            trackColor = colors.container.copy(alpha = 0.5f),
            strokeCap = StrokeCap.Round,
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(QazaShapes.pillShape)
        )
        Spacer(Modifier.height(6.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Started ${formatShortDate(estimate.trackingStartedOn, today)}",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = formatShortDate(estimate.estimatedDate, today),
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.End,
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = "${formatPercent(fraction * 100f)} of estimated time passed",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun EstimateBreakdownDialog(estimate: CompletionEstimate, today: LocalDate, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Pace breakdown") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                BreakdownRow(
                    label = "Days logged so far",
                    value = estimate.daysLogged.toString(),
                    detail = "of ${daysPhrase(estimate.daysTracked)} since you started"
                )
                BreakdownRow(label = "Average prayers per day", value = formatPaceValue(estimate.averagePrayersPerDay))
                BreakdownRow(label = "Remaining prayers", value = estimate.prayersRemaining.toString())
                BreakdownRow(label = "Confidence", value = estimate.confidence.label)
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Text(
                    text = projectionDetails(estimate, today),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } }
    )
}

@Composable
private fun BreakdownRow(label: String, value: String, detail: String? = null) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, style = MaterialTheme.typography.bodyMedium)
            if (detail != null) {
                Text(
                    text = detail,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        Text(text = value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
    }
}

// ---- Text helpers (internal so they can be unit-tested) ----

/** "December 15", or "December 15, 2028" once the year isn't the current one. */
internal fun formatEstimateDate(date: LocalDate, today: LocalDate): String =
    date.format(dateFormatter(long = true, includeYear = date.year != today.year))

/** "Sep 24", or "Sep 24, 2028" once the year isn't the current one. */
internal fun formatShortDate(date: LocalDate, today: LocalDate): String =
    date.format(dateFormatter(long = false, includeYear = date.year != today.year))

private fun dateFormatter(long: Boolean, includeYear: Boolean): DateTimeFormatter {
    val month = if (long) "MMMM" else "MMM"
    return DateTimeFormatter.ofPattern(if (includeYear) "$month d, yyyy" else "$month d", Locale.getDefault())
}

/** "~1 day", "~45 days", then months, then years, so a far-off finish stays readable. */
internal fun formatRemaining(days: Int): String = when {
    days <= 1 -> "~1 day"
    days <= 60 -> "~$days days"
    days <= 730 -> "~${(days / 30.44).roundToInt()} months"
    else -> {
        val years = days / 365.25
        if (years < 9.95) String.format(Locale.US, "~%.1f years", years) else "~${years.roundToInt()} years"
    }
}

internal fun formatPaceValue(pace: Float): String = String.format(Locale.US, "%.1f", pace)

internal fun formatPace(pace: Float): String = "Logging ~${formatPaceValue(pace)} prayers/day"

private fun daysPhrase(days: Int): String = if (days == 1) "1 day" else "$days days"

private fun projectionDetails(estimate: CompletionEstimate, today: LocalDate): String {
    val confidenceNote = when (estimate.confidence) {
        EstimateConfidence.LOW ->
            "Confidence is low: prayers were logged on fewer than ${EstimateConfidence.LOW_BELOW_DAYS} different days."
        EstimateConfidence.MEDIUM ->
            "Confidence is medium: prayers were logged on ${EstimateConfidence.LOW_BELOW_DAYS} to " +
                "${EstimateConfidence.HIGH_FROM_DAYS - 1} different days."
        EstimateConfidence.HIGH ->
            "Confidence is high: prayers were logged on ${EstimateConfidence.HIGH_FROM_DAYS} or more different days."
    }
    return "At ${formatPaceValue(estimate.averagePrayersPerDay)} prayers a day, your " +
        "${estimate.prayersRemaining} remaining prayers would take about ${daysPhrase(estimate.daysRemaining)}, " +
        "finishing ${formatEstimateDate(estimate.estimatedDate, today)}. Pace is the " +
        "${estimate.prayersCompleted} prayers you've completed divided by the ${daysPhrase(estimate.daysTracked)} " +
        "since you started on ${formatShortDate(estimate.trackingStartedOn, today)}. $confidenceNote"
}
