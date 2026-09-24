package com.zilehasnain.qazatracker.ui.tutorial

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.zilehasnain.qazatracker.ui.theme.QazaShapes

private data class TutorialTip(val title: String, val description: String)

private val tutorialTips = listOf(
    TutorialTip(
        title = "Calculate your baseline",
        description = "Enter the dates you missed, or a rough age estimate, and we'll work out " +
            "how many prayers you owe."
    ),
    TutorialTip(
        title = "Log prayers as you go",
        description = "Tap + next to a prayer to log one, or use \"Log multiple days\" to catch " +
            "up in bulk."
    ),
    TutorialTip(
        title = "Watch your progress",
        description = "Your dashboard shows what's left for each prayer and how long it will " +
            "take at your pace. No streaks, no pressure."
    ),
    TutorialTip(
        title = "Gentle reminders",
        description = "Choose how often we remind you, or turn reminders off, any time in Settings."
    )
)

@Composable
fun TutorialScreen(
    modifier: Modifier = Modifier,
    viewModel: TutorialViewModel = hiltViewModel(),
    onFinished: () -> Unit = {}
) {
    TutorialContent(
        onGetStarted = { viewModel.onDismiss(onFinished) },
        onSkip = { viewModel.onDismiss(onFinished) },
        modifier = modifier
    )
}

@Composable
fun TutorialContent(
    onGetStarted: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 22.dp)
                    .padding(top = 32.dp)
            ) {
                Text(
                    text = "QAZA TRACKER",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(Modifier.height(12.dp))

                Text(
                    text = "Welcome to Qaza Tracker",
                    style = MaterialTheme.typography.headlineLarge.copy(fontSize = 36.sp, lineHeight = 42.sp)
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "A few quick tips before you begin.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(28.dp))

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    tutorialTips.forEachIndexed { index, tip ->
                        TipRow(number = index + 1, tip = tip, tinted = index % 2 == 0)
                    }
                }

                Spacer(Modifier.height(24.dp))
            }

            Column(
                modifier = Modifier.padding(horizontal = 22.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = onGetStarted,
                    shape = QazaShapes.pillShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text(
                        text = "Let's get started",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Medium
                    )
                }

                TextButton(onClick = onSkip) {
                    Text(
                        text = "Skip",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun TipRow(number: Int, tip: TutorialTip, tinted: Boolean) {
    val badgeColor = if (tinted) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.secondaryContainer
    }
    val badgeContentColor = if (tinted) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSecondaryContainer
    }

    Surface(
        shape = QazaShapes.cardShape,
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(badgeColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = number.toString(),
                    style = MaterialTheme.typography.titleSmall,
                    color = badgeContentColor
                )
            }
            Column {
                Text(
                    text = tip.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = tip.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
