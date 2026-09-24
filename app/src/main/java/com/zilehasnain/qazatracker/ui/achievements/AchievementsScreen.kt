package com.zilehasnain.qazatracker.ui.achievements

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zilehasnain.qazatracker.domain.model.AchievementsData
import com.zilehasnain.qazatracker.ui.theme.QazaShapes
import java.time.LocalDate
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Gap between one card starting its entrance animation and the next. */
private const val STAGGER_MILLIS = 70L

@Composable
fun AchievementsScreen(
    modifier: Modifier = Modifier,
    viewModel: AchievementsViewModel = hiltViewModel(),
    onBack: () -> Unit = {}
) {
    val achievements by viewModel.achievements.collectAsStateWithLifecycle()
    val hasLoaded by viewModel.hasLoaded.collectAsStateWithLifecycle()

    AchievementsContent(
        achievements = achievements,
        hasLoaded = hasLoaded,
        onBack = onBack,
        modifier = modifier
    )
}

@Composable
fun AchievementsContent(
    achievements: AchievementsData,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    hasLoaded: Boolean = true,
    today: LocalDate = LocalDate.now()
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

            Spacer(Modifier.height(8.dp))

            Hero(count = achievements.totalCount, hasLoaded = hasLoaded)

            // Nothing below the hero until the first read finishes, so a real collection is
            // never flashed as empty.
            if (!hasLoaded) return@Column

            Spacer(Modifier.height(24.dp))

            if (achievements.isEmpty) {
                EmptyState()
                return@Column
            }

            val milestoneOffset = 0
            val streakOffset = milestoneOffset + achievements.milestones.size
            val progressOffset = streakOffset + achievements.streaks.size

            if (achievements.milestones.isNotEmpty()) {
                Section(emoji = AchievementEmoji.PARTY, title = "Milestones") {
                    achievements.milestones.forEachIndexed { index, milestone ->
                        AchievementCard(
                            emoji = AchievementEmoji.forPrayer(milestone.prayerType),
                            title = milestoneTitle(milestone.prayerType),
                            detail = null,
                            unlocked = unlockedText(milestone.achievedDate, today),
                            container = MaterialTheme.colorScheme.primaryContainer,
                            content = MaterialTheme.colorScheme.onPrimaryContainer,
                            index = milestoneOffset + index
                        )
                    }
                }
            }

            if (achievements.streaks.isNotEmpty()) {
                Spacer(Modifier.height(24.dp))
                // One card per entry, so the headline count always matches what's on screen.
                // The streak still going carries the best for comparison; the best appears as
                // its own card only when it is a different, earlier run (or the only one left).
                val best = achievements.streaks.maxOf { it.streakLength }
                Section(emoji = AchievementEmoji.FIRE, title = "Streaks") {
                    achievements.streaks.forEachIndexed { index, streak ->
                        AchievementCard(
                            emoji = if (streak.isCurrent) AchievementEmoji.FIRE else AchievementEmoji.STAR,
                            title = if (streak.isCurrent) {
                                currentStreakTitle(streak.streakLength)
                            } else {
                                bestStreakTitle(streak.streakLength)
                            },
                            detail = if (streak.isCurrent) bestStreakDetail(best) else null,
                            unlocked = unlockedText(streak.achievedDate, today),
                            container = MaterialTheme.colorScheme.secondaryContainer,
                            content = MaterialTheme.colorScheme.onSecondaryContainer,
                            index = streakOffset + index
                        )
                    }
                }
            }

            if (achievements.progressBadges.isNotEmpty()) {
                Spacer(Modifier.height(24.dp))
                Section(emoji = AchievementEmoji.CHART, title = "Progress") {
                    achievements.progressBadges.forEachIndexed { index, badge ->
                        AchievementCard(
                            emoji = AchievementEmoji.forProgress(badge.percentage),
                            title = progressTitle(badge.percentage),
                            detail = null,
                            unlocked = unlockedText(badge.achievedDate, today),
                            container = MaterialTheme.colorScheme.surface,
                            content = MaterialTheme.colorScheme.onSurface,
                            index = progressOffset + index
                        )
                    }
                }
            }
        }
    }
}

/** A big trophy that springs in, with the running total underneath. */
@Composable
private fun Hero(count: Int, hasLoaded: Boolean) {
    val scale = remember { Animatable(0.3f) }
    LaunchedEffect(Unit) {
        scale.animateTo(1f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow))
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = AchievementEmoji.TROPHY,
            fontSize = 72.sp,
            modifier = Modifier.graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
            }
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Achievements",
            style = MaterialTheme.typography.headlineSmall.copy(fontSize = 26.sp, lineHeight = 32.sp)
        )
        if (hasLoaded) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = unlockedCountText(count),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun EmptyState() {
    Surface(
        shape = QazaShapes.cardShape,
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = AchievementEmoji.SEEDLING, fontSize = 48.sp)
            Spacer(Modifier.height(12.dp))
            Text(
                text = "No achievements yet",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Log a few prayers and your first streak, progress badge and milestones will show up here.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun Section(emoji: String, title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "$emoji  $title",
            style = MaterialTheme.typography.titleMedium
        )
        content()
    }
}

/** Slides up and fades in, each card a beat after the one before it. */
@Composable
private fun AchievementCard(
    emoji: String,
    title: String,
    detail: String?,
    unlocked: String,
    container: Color,
    content: Color,
    index: Int
) {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        launch {
            delay(index * STAGGER_MILLIS)
            progress.animateTo(1f, animationSpec = tween(durationMillis = 450))
        }
    }

    Surface(
        shape = QazaShapes.cardShape,
        color = container,
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                alpha = progress.value
                translationY = (1f - progress.value) * 24.dp.toPx()
            }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = emoji, fontSize = 38.sp)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = content
                )
                if (detail != null) {
                    Spacer(Modifier.height(2.dp))
                    Text(text = detail, style = MaterialTheme.typography.bodyMedium, color = content)
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = unlocked,
                    style = MaterialTheme.typography.bodySmall,
                    color = content.copy(alpha = 0.8f)
                )
            }
        }
    }
}
