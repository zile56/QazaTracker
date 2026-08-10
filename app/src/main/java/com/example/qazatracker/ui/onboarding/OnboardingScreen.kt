package com.example.qazatracker.ui.onboarding

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.qazatracker.domain.model.BaselineCalculation
import com.example.qazatracker.domain.model.CalculationMethod
import com.example.qazatracker.domain.model.PrayerType
import com.example.qazatracker.ui.theme.QazaShapes
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun OnboardingScreen(
    modifier: Modifier = Modifier,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    OnboardingContent(
        uiState = uiState,
        onSelectMethod = viewModel::selectMethod,
        onDateFromSelected = viewModel::onDateFromSelected,
        onDateToSelected = viewModel::onDateToSelected,
        onAgeNowChanged = viewModel::onAgeNowChanged,
        onAgeObligatoryChanged = viewModel::onAgeObligatoryChanged,
        onContinueClicked = viewModel::onContinueClicked,
        modifier = modifier
    )
}

@Composable
fun OnboardingContent(
    uiState: OnboardingUiState,
    onSelectMethod: (CalculationMethod) -> Unit,
    onDateFromSelected: (LocalDate) -> Unit,
    onDateToSelected: (LocalDate) -> Unit,
    onAgeNowChanged: (String) -> Unit,
    onAgeObligatoryChanged: (String) -> Unit,
    onContinueClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 22.dp)
                    .padding(top = 20.dp)
            ) {
                OnboardingHeader()

                Spacer(Modifier.height(18.dp))

                MethodCard(
                    title = "I know my exact dates",
                    subtitle = "Pick the date range you missed prayers",
                    icon = Icons.Default.DateRange,
                    selected = uiState.method == CalculationMethod.EXACT_DATES,
                    onClick = { onSelectMethod(CalculationMethod.EXACT_DATES) }
                ) {
                    ExactDatesInputs(
                        dateFrom = uiState.dateFrom,
                        dateTo = uiState.dateTo,
                        onDateFromSelected = onDateFromSelected,
                        onDateToSelected = onDateToSelected
                    )
                }

                Spacer(Modifier.height(14.dp))

                MethodCard(
                    title = "Estimate by age",
                    subtitle = "Tell us your age and when prayer became obligatory",
                    icon = Icons.Default.Person,
                    selected = uiState.method == CalculationMethod.AGE_ESTIMATE,
                    onClick = { onSelectMethod(CalculationMethod.AGE_ESTIMATE) }
                ) {
                    AgeEstimateInputs(
                        ageNow = uiState.ageNowInput,
                        ageObligatory = uiState.ageObligatoryInput,
                        onAgeNowChanged = onAgeNowChanged,
                        onAgeObligatoryChanged = onAgeObligatoryChanged
                    )
                }

                uiState.errorMessage?.let { message ->
                    Spacer(Modifier.height(14.dp))
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                uiState.result?.let { result ->
                    Spacer(Modifier.height(20.dp))
                    BaselineResultCard(result)
                }

                Spacer(Modifier.height(16.dp))
            }

            Column(modifier = Modifier.padding(horizontal = 22.dp, vertical = 16.dp)) {
                Button(
                    onClick = onContinueClicked,
                    enabled = uiState.canContinue,
                    shape = QazaShapes.pillShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text("Continue", style = MaterialTheme.typography.labelLarge)
                    Spacer(Modifier.width(6.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
private fun OnboardingHeader() {
    Box(
        modifier = Modifier
            .size(44.dp)
            .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.DateRange,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.size(22.dp)
        )
    }

    Spacer(Modifier.height(18.dp))

    Text(
        text = "Let's find your starting point",
        style = MaterialTheme.typography.headlineSmall.copy(fontSize = 28.sp, lineHeight = 32.sp)
    )

    Spacer(Modifier.height(6.dp))

    Text(
        text = "Choose how you'd like to estimate the prayers you've missed. " +
            "You can fine-tune every number on the next screen.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.widthIn(max = 280.dp)
    )
}

@Composable
private fun MethodCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    expandedContent: @Composable () -> Unit
) {
    val borderColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
    val containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
    val badgeColor = if (selected) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
    } else {
        MaterialTheme.colorScheme.secondaryContainer
    }

    Surface(
        onClick = onClick,
        shape = QazaShapes.cardShape,
        color = containerColor,
        border = BorderStroke(1.5.dp, borderColor),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .background(badgeColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(17.dp)
                    )
                }
                Column {
                    Text(text = title, style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (selected) {
                Spacer(Modifier.height(16.dp))
                expandedContent()
            }
        }
    }
}

@Composable
private fun ExactDatesInputs(
    dateFrom: LocalDate?,
    dateTo: LocalDate?,
    onDateFromSelected: (LocalDate) -> Unit,
    onDateToSelected: (LocalDate) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        DateField(
            label = "From",
            value = dateFrom,
            onDateSelected = onDateFromSelected,
            modifier = Modifier.weight(1f)
        )
        DateField(
            label = "To",
            value = dateTo,
            onDateSelected = onDateToSelected,
            modifier = Modifier.weight(1f)
        )
    }
}

private val dateFieldFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.getDefault())

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateField(
    label: String,
    value: LocalDate?,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    var showPicker by remember { mutableStateOf(false) }

    Column(modifier) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(5.dp))
        OutlinedButton(
            onClick = { showPicker = true },
            shape = QazaShapes.pillShape,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = value?.format(dateFieldFormatter) ?: "Select date")
        }
    }

    if (showPicker) {
        val initialMillis = value?.atStartOfDay(ZoneOffset.UTC)?.toInstant()?.toEpochMilli()
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        onDateSelected(Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate())
                    }
                    showPicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun AgeEstimateInputs(
    ageNow: String,
    ageObligatory: String,
    onAgeNowChanged: (String) -> Unit,
    onAgeObligatoryChanged: (String) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        AgeField(
            label = "Your age now",
            value = ageNow,
            onValueChange = onAgeNowChanged,
            modifier = Modifier.weight(1f)
        )
        AgeField(
            label = "Age prayer began",
            value = ageObligatory,
            onValueChange = onAgeObligatoryChanged,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun AgeField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(5.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            shape = QazaShapes.pillShape,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun BaselineResultCard(result: BaselineCalculation) {
    val total = result.countsByPrayerType.values.sum()

    Surface(
        shape = QazaShapes.cardShape,
        color = MaterialTheme.colorScheme.secondaryContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Estimated total",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "$total prayers across ${result.missedDays} days",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Spacer(Modifier.height(12.dp))
            result.countsByPrayerType.forEach { (type, count) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = type.displayName(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = count.toString(),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }
    }
}

private fun PrayerType.displayName(): String = when (this) {
    PrayerType.FAJR -> "Fajr"
    PrayerType.DHUHR -> "Dhuhr"
    PrayerType.ASR -> "Asr"
    PrayerType.MAGHRIB -> "Maghrib"
    PrayerType.ISHA -> "Isha"
}
