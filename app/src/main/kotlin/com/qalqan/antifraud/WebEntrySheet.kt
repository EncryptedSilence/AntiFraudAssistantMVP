package com.qalqan.antifraud

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.qalqan.antifraud.web.WebCaptureOutcome

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebEntrySheet(
    onDismiss: () -> Unit,
    onSubmit: (String, (WebCaptureOutcome) -> Unit) -> Unit,
) {
    var input by remember { mutableStateOf("") }
    var status by remember { mutableStateOf<String?>(null) }
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Add a suspicious site")
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                label = { Text("Domain or URL") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            status?.let { Text(it) }
            Button(
                onClick = {
                    onSubmit(input) { outcome ->
                        status =
                            when (outcome) {
                                is WebCaptureOutcome.Saved -> "Saved ${outcome.canonical} (${outcome.status})"
                                WebCaptureOutcome.Rejected.Empty -> "Please enter a domain."
                                is WebCaptureOutcome.Rejected.Invalid -> "Could not parse: ${outcome.input}"
                            }
                    }
                },
            ) {
                Text("Submit")
            }
        }
    }
}
