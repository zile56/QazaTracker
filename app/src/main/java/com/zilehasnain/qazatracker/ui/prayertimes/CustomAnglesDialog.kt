package com.zilehasnain.qazatracker.ui.prayertimes

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

/** Lets someone whose country isn't listed enter the Fajr and Isha sun angles their authority publishes. */
@Composable
fun CustomAnglesDialog(
    initialFajr: Double,
    initialIsha: Double,
    onSave: (fajr: Double, isha: Double) -> Boolean,
    onDismiss: () -> Unit
) {
    var fajr by remember { mutableStateOf(initialFajr.toString()) }
    var isha by remember { mutableStateOf(initialIsha.toString()) }
    var showError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Custom angles") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "The sun's angle below the horizon at Fajr and at Isha, in degrees (5 to 25).",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = fajr,
                    onValueChange = { fajr = it; showError = false },
                    label = { Text("Fajr angle") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                OutlinedTextField(
                    value = isha,
                    onValueChange = { isha = it; showError = false },
                    label = { Text("Isha angle") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                if (showError) {
                    Text(
                        text = "Enter angles between 5 and 25.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val f = fajr.replace(',', '.').toDoubleOrNull()
                val i = isha.replace(',', '.').toDoubleOrNull()
                if (f == null || i == null || !onSave(f, i)) showError = true
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
