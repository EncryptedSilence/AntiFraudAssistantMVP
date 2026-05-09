package com.qalqan.antifraud

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun StatusScreen(viewModel: StatusViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text("AntiFraud Assistant — Stage 1 status board", style = MaterialTheme.typography.titleLarge)
            Text("Calls captured: ${state.calls}")
            Text("SMS captured: ${state.sms}")
            Button(onClick = { viewModel.runDemo() }) { Text("Run demo (Fast attack)") }
            Button(onClick = { viewModel.wipe() }) { Text("Wipe all data") }
        }
    }
}
