@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.qalqan.antifraud

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.qalqan.antifraud.export.AnonymizationOption
import com.qalqan.antifraud.export.ExportCategory
import com.qalqan.antifraud.export.ExportFormat
import com.qalqan.antifraud.export.ExportOrchestrator
import com.qalqan.antifraud.export.ExportRequest
import kotlinx.coroutines.launch

/**
 * Spec §17.5 — the export sheet. Drives the three-step UI:
 *
 *   1. Pick categories (one or more), format (exactly one), anonymization options
 *      (zero or more).
 *   2. Generate preview → display the exact bytes that will be written, verbatim,
 *      in a scrollable monospace block.
 *   3. Save → SAF `CreateDocument` launcher → orchestrator.write(...) lands the bytes.
 *
 * The "Save" button is disabled until [previewState] contains a non-null preview AND the
 * preview's request matches the current selection. Mutating any of the chips after the
 * preview is generated invalidates the gate (forces the user to re-generate).
 */
@Suppress("LongMethod")
@Composable
fun ExportSheet(
    onDismiss: () -> Unit,
    viewModel: StatusViewModel,
) {
    val scope = rememberCoroutineScope()
    var selectedCategories by remember { mutableStateOf(emptySet<ExportCategory>()) }
    var selectedFormat by remember { mutableStateOf(ExportFormat.JSON) }
    var selectedAnonymization by remember { mutableStateOf(emptySet<AnonymizationOption>()) }
    var previewState by remember { mutableStateOf<ExportOrchestrator.Preview?>(null) }
    var statusLine by remember { mutableStateOf("") }

    val currentRequest: ExportRequest? =
        if (selectedCategories.isEmpty()) {
            null
        } else {
            ExportRequest(selectedCategories, selectedFormat, selectedAnonymization)
        }

    val saveLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.CreateDocument(selectedFormat.mimeType),
        ) { uri: Uri? ->
            val preview = previewState
            val request = currentRequest
            if (uri != null && preview != null && request != null) {
                scope.launch {
                    val r = viewModel.writeExport(request, uri, preview.token)
                    statusLine = if (r.isSuccess) "Saved." else "Save failed."
                    previewState = null
                }
            }
        }

    val previewMatchesSelection = previewState?.request == currentRequest

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Export", style = MaterialTheme.typography.titleLarge)
            ExportPickerGroup(
                selectedCategories = selectedCategories,
                selectedFormat = selectedFormat,
                selectedAnonymization = selectedAnonymization,
                onCategoriesChange = {
                    selectedCategories = it
                    previewState = null
                },
                onFormatChange = {
                    selectedFormat = it
                    previewState = null
                },
                onAnonymizationChange = {
                    selectedAnonymization = it
                    previewState = null
                },
            )
            ExportPreviewControls(
                currentRequest = currentRequest,
                previewState = previewState,
                previewMatchesSelection = previewMatchesSelection,
                statusLine = statusLine,
                onGeneratePreview = { req ->
                    scope.launch {
                        val r = viewModel.generateExportPreview(req)
                        if (r.isSuccess) {
                            previewState = r.getOrThrow()
                            statusLine = ""
                        } else {
                            statusLine = "Preview failed."
                        }
                    }
                },
                onSave = { saveLauncher.launch("antifraud-export.${selectedFormat.fileExtension}") },
            )
        }
    }
}

@Suppress("LongParameterList")
@Composable
private fun ExportPickerGroup(
    selectedCategories: Set<ExportCategory>,
    selectedFormat: ExportFormat,
    selectedAnonymization: Set<AnonymizationOption>,
    onCategoriesChange: (Set<ExportCategory>) -> Unit,
    onFormatChange: (ExportFormat) -> Unit,
    onAnonymizationChange: (Set<AnonymizationOption>) -> Unit,
) {
    Text("Categories", style = MaterialTheme.typography.titleSmall)
    ExportCategory.entries.forEach { c ->
        FilterChip(
            selected = c in selectedCategories,
            onClick = {
                onCategoriesChange(
                    if (c in selectedCategories) selectedCategories - c else selectedCategories + c,
                )
            },
            label = { Text(c.name) },
        )
    }
    Text("Format", style = MaterialTheme.typography.titleSmall)
    ExportFormat.entries.forEach { f ->
        Row {
            RadioButton(
                selected = selectedFormat == f,
                onClick = { onFormatChange(f) },
            )
            Text(f.name)
        }
    }
    Text("Anonymization", style = MaterialTheme.typography.titleSmall)
    AnonymizationOption.OPERATIONAL.forEach { opt ->
        Row {
            Switch(
                checked = opt in selectedAnonymization,
                onCheckedChange = { on ->
                    onAnonymizationChange(
                        if (on) selectedAnonymization + opt else selectedAnonymization - opt,
                    )
                },
            )
            Text(opt.jsonValue)
        }
    }
}

@Suppress("LongParameterList")
@Composable
private fun ExportPreviewControls(
    currentRequest: ExportRequest?,
    previewState: ExportOrchestrator.Preview?,
    previewMatchesSelection: Boolean,
    statusLine: String,
    onGeneratePreview: (ExportRequest) -> Unit,
    onSave: () -> Unit,
) {
    Button(
        enabled = currentRequest != null,
        onClick = { currentRequest?.let { onGeneratePreview(it) } },
    ) { Text("Generate preview") }
    previewState?.let { preview ->
        Text(
            "Preview (this is exactly what will be saved):",
            style = MaterialTheme.typography.titleSmall,
        )
        Text(
            text = preview.bytes.toString(Charsets.UTF_8),
            style = MaterialTheme.typography.bodySmall,
        )
    }
    Button(
        enabled = previewState != null && previewMatchesSelection,
        onClick = onSave,
    ) { Text("Save…") }
    if (statusLine.isNotEmpty()) Text(statusLine)
}
