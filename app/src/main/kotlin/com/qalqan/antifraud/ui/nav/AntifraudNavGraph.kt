package com.qalqan.antifraud.ui.nav

import android.app.Application
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.qalqan.antifraud.database.Repositories
import com.qalqan.antifraud.ui.home.HomeRoute
import com.qalqan.antifraud.ui.home.HomeViewModel

@Composable
fun AntifraudNavGraph(
    repos: Repositories,
    startDestination: String = AntifraudDestination.Home.route,
) {
    val navController: NavHostController = rememberNavController()
    Scaffold(
        bottomBar = { AntifraudBottomBar(navController) },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(AntifraudDestination.Home.route) {
                val app = LocalContext.current.applicationContext as Application
                val viewModel = remember(repos) { HomeViewModel(app, repos) }
                LaunchedEffect(viewModel) { viewModel.refresh() }
                val state by viewModel.state.collectAsState()
                HomeRoute(
                    state = state,
                    onSuspiciousCall = {},
                    onSuspiciousSms = {},
                    onSuspiciousSite = {},
                    onOpenCampaign = { id ->
                        navController.navigate(AntifraudDestination.CampaignDetail(id).route)
                    },
                    onOpenPrivacy = {
                        navController.navigate(AntifraudDestination.Privacy.route)
                    },
                )
            }
            composable(AntifraudDestination.Campaigns.route) {
                PlaceholderRoute(label = stringResource(AntifraudDestination.Campaigns.labelResId))
            }
            composable(AntifraudDestination.Patterns.route) {
                PlaceholderRoute(label = stringResource(AntifraudDestination.Patterns.labelResId))
            }
            composable(AntifraudDestination.References.route) {
                PlaceholderRoute(label = stringResource(AntifraudDestination.References.labelResId))
            }
            composable(AntifraudDestination.Privacy.route) {
                PlaceholderRoute(label = stringResource(AntifraudDestination.Privacy.labelResId))
            }
        }
    }
}

@Composable
private fun AntifraudBottomBar(navController: NavHostController) {
    val current by navController.currentBackStackEntryAsState()
    val currentRoute = current?.destination?.route
    NavigationBar {
        AntifraudDestination.topLevel().forEach { dest ->
            NavigationBarItem(
                selected = currentRoute == dest.route,
                onClick = {
                    if (currentRoute != dest.route) {
                        navController.navigate(dest.route) {
                            popUpTo(AntifraudDestination.Home.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = { Text(stringResource(dest.labelResId).take(1)) },
                label = { Text(stringResource(dest.labelResId)) },
            )
        }
    }
}

@Composable
private fun PlaceholderRoute(label: String) {
    Box(modifier = Modifier.fillMaxSize()) {
        Text(label, style = MaterialTheme.typography.titleLarge)
    }
}
