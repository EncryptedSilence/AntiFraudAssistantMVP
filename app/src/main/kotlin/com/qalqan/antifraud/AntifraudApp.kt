package com.qalqan.antifraud

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import com.qalqan.antifraud.ui.nav.AntifraudDestination
import com.qalqan.antifraud.ui.nav.AntifraudNavGraph

@Composable
fun AntifraudApp(startDestination: String = AntifraudDestination.Home.route) {
    MaterialTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            AntifraudNavGraph(startDestination = startDestination)
        }
    }
}
