package com.zilehasnain.qazatracker.ui.inspiration

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zilehasnain.qazatracker.domain.model.Hadith
import com.zilehasnain.qazatracker.domain.model.InspirationItem
import com.zilehasnain.qazatracker.domain.model.QuranVerse
import com.zilehasnain.qazatracker.ui.theme.QazaShapes
import kotlinx.coroutines.delay

private const val BOOKMARK_ON = "★" // ★
private const val BOOKMARK_OFF = "☆" // ☆
private const val COPIED_FEEDBACK_MILLIS = 1_500L

@Composable
fun DailyInspirationScreen(
    modifier: Modifier = Modifier,
    viewModel: DailyInspirationViewModel = hiltViewModel(),
    onBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    DailyInspirationContent(
        uiState = uiState,
        onBack = onBack,
        onPrevious = viewModel::onPrevious,
        onNext = viewModel::onNext,
        onRefresh = viewModel::onRefresh,
        onTabSelected = viewModel::onTabSelected,
        onBookmark = viewModel::onBookmark,
        onShare = { item -> share(context, shareText(item)) },
        onCopy = { item -> copy(context, shareText(item)) },
        modifier = modifier
    )
}

private fun share(context: Context, text: String) {
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(send, null).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
}

private fun copy(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText("Qaza Tracker", text))
}

@Composable
fun DailyInspirationContent(
    uiState: InspirationUiState,
    onBack: () -> Unit = {},
    onPrevious: () -> Unit = {},
    onNext: () -> Unit = {},
    onRefresh: () -> Unit = {},
    onTabSelected: (InspirationTab) -> Unit = {},
    onBookmark: (InspirationItem) -> Unit = {},
    onShare: (InspirationItem) -> Unit = {},
    onCopy: (InspirationItem) -> Unit = {},
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
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Daily inspiration",
                style = MaterialTheme.typography.headlineSmall.copy(fontSize = 24.sp, lineHeight = 28.sp),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            TabRow(
                selectedTabIndex = uiState.tab.ordinal,
                containerColor = MaterialTheme.colorScheme.background
            ) {
                Tab(
                    selected = uiState.tab == InspirationTab.TODAY,
                    onClick = { onTabSelected(InspirationTab.TODAY) },
                    text = { Text("Today") }
                )
                Tab(
                    selected = uiState.tab == InspirationTab.SAVED,
                    onClick = { onTabSelected(InspirationTab.SAVED) },
                    text = { Text("Saved (${uiState.savedCount})") }
                )
            }

            when (uiState.tab) {
                InspirationTab.TODAY -> TodayTab(uiState, onPrevious, onNext, onRefresh, onBookmark, onShare, onCopy)
                InspirationTab.SAVED -> SavedTab(uiState, onBookmark, onShare, onCopy)
            }
        }
    }
}

@Composable
private fun TodayTab(
    uiState: InspirationUiState,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onRefresh: () -> Unit,
    onBookmark: (InspirationItem) -> Unit,
    onShare: (InspirationItem) -> Unit,
    onCopy: (InspirationItem) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        AnimatedContent(
            targetState = uiState.inspiration,
            transitionSpec = {
                val direction = if (uiState.movedForward) 1 else -1
                (slideInHorizontally { it * direction / 3 } + fadeIn())
                    .togetherWith(slideOutHorizontally { -it * direction / 3 } + fadeOut())
                    .using(SizeTransform(clip = false))
            },
            label = "inspiration",
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) { inspiration ->
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(top = 16.dp, bottom = 8.dp)
            ) {
                SectionLabel(if (uiState.offset == 0) "Today's verse" else "Verse")
                VerseCard(
                    verse = inspiration.verse,
                    saved = uiState.isSaved(inspiration.verse.id),
                    onBookmark = { onBookmark(InspirationItem.Verse(inspiration.verse)) },
                    onShare = { onShare(InspirationItem.Verse(inspiration.verse)) },
                    onCopy = { onCopy(InspirationItem.Verse(inspiration.verse)) }
                )
                SectionLabel(if (uiState.offset == 0) "Today's hadith" else "Hadith")
                HadithCard(
                    hadith = inspiration.hadith,
                    saved = uiState.isSaved(inspiration.hadith.id),
                    onBookmark = { onBookmark(InspirationItem.Saying(inspiration.hadith)) },
                    onShare = { onShare(InspirationItem.Saying(inspiration.hadith)) },
                    onCopy = { onCopy(InspirationItem.Saying(inspiration.hadith)) }
                )
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        ) {
            OutlinedButton(onClick = onPrevious, shape = QazaShapes.pillShape, modifier = Modifier.weight(1f)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.size(4.dp))
                Text("Previous", maxLines = 1)
            }
            OutlinedButton(onClick = onRefresh, shape = QazaShapes.pillShape) { Text("Random", maxLines = 1) }
            OutlinedButton(onClick = onNext, shape = QazaShapes.pillShape, modifier = Modifier.weight(1f)) {
                Text("Next", maxLines = 1)
                Spacer(Modifier.size(4.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
private fun SavedTab(
    uiState: InspirationUiState,
    onBookmark: (InspirationItem) -> Unit,
    onShare: (InspirationItem) -> Unit,
    onCopy: (InspirationItem) -> Unit
) {
    if (uiState.saved.isEmpty()) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            Text("No saved quotes yet", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Tap $BOOKMARK_OFF Save on a verse or hadith and it will be kept here.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 16.dp)
    ) {
        uiState.saved.forEach { item ->
            when (item) {
                is InspirationItem.Verse -> VerseCard(
                    verse = item.verse, saved = true,
                    onBookmark = { onBookmark(item) }, onShare = { onShare(item) }, onCopy = { onCopy(item) }
                )

                is InspirationItem.Saying -> HadithCard(
                    hadith = item.hadith, saved = true,
                    onBookmark = { onBookmark(item) }, onShare = { onShare(item) }, onCopy = { onCopy(item) }
                )
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
private fun QuoteCard(content: @Composable () -> Unit) {
    Card(
        shape = QazaShapes.cardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp),
            content = { content() }
        )
    }
}

@Composable
private fun VerseCard(
    verse: QuranVerse,
    saved: Boolean,
    onBookmark: () -> Unit,
    onShare: () -> Unit,
    onCopy: () -> Unit
) {
    QuoteCard {
        Text(
            text = verse.arabicText,
            style = MaterialTheme.typography.headlineSmall.copy(fontSize = 28.sp, lineHeight = 50.sp),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(18.dp))
        Text(
            text = verse.englishText,
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 17.sp, lineHeight = 27.sp),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = verse.source,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = verse.reflection,
            style = MaterialTheme.typography.bodyMedium,
            fontStyle = FontStyle.Italic,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        QuoteActions(saved, onBookmark, onShare, onCopy)
    }
}

@Composable
private fun HadithCard(
    hadith: Hadith,
    saved: Boolean,
    onBookmark: () -> Unit,
    onShare: () -> Unit,
    onCopy: () -> Unit
) {
    QuoteCard {
        Text(
            text = "“", // opening quotation mark
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = hadith.text,
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp, lineHeight = 29.sp),
            fontStyle = FontStyle.Italic,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(14.dp))
        Text(
            text = hadith.attribution,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )
        QuoteActions(saved, onBookmark, onShare, onCopy)
    }
}

@Composable
private fun QuoteActions(saved: Boolean, onBookmark: () -> Unit, onShare: () -> Unit, onCopy: () -> Unit) {
    var copied by remember { mutableStateOf(false) }
    LaunchedEffect(copied) {
        if (copied) {
            delay(COPIED_FEEDBACK_MILLIS)
            copied = false
        }
    }
    Row(
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp)
    ) {
        TextButton(onClick = onBookmark) { Text(if (saved) "$BOOKMARK_ON Saved" else "$BOOKMARK_OFF Save") }
        TextButton(onClick = onShare) { Text("Share") }
        TextButton(onClick = { onCopy(); copied = true }) { Text(if (copied) "Copied" else "Copy") }
    }
}
