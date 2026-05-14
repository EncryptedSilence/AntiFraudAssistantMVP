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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.qalqan.antifraud.WebEntrySheet
import com.qalqan.antifraud.database.Repositories
import com.qalqan.antifraud.database.crypto.InMemoryCryptoBox
import com.qalqan.antifraud.database.crypto.KeyStoreCryptoBox
import com.qalqan.antifraud.database.manual.ManualEntry
import com.qalqan.antifraud.domain.CallDirection
import com.qalqan.antifraud.settings.UserSettings
import com.qalqan.antifraud.ui.campaign.CampaignDetailRoute
import com.qalqan.antifraud.ui.campaign.CampaignDetailViewModel
import com.qalqan.antifraud.ui.campaign.CampaignListRoute
import com.qalqan.antifraud.ui.campaign.CampaignsViewModel
import com.qalqan.antifraud.ui.home.HomeRoute
import com.qalqan.antifraud.ui.home.HomeViewModel
import com.qalqan.antifraud.ui.patterns.PatternsRoute
import com.qalqan.antifraud.ui.patterns.PatternsViewModel
import com.qalqan.antifraud.ui.privacy.PrivacyRoute
import com.qalqan.antifraud.ui.privacy.PrivacyViewModel
import com.qalqan.antifraud.ui.references.ReferencesRoute
import com.qalqan.antifraud.ui.references.ReferencesViewModel
import com.qalqan.antifraud.ui.settings.SettingsRoute
import com.qalqan.antifraud.ui.settings.SettingsViewModel
import com.qalqan.antifraud.ui.home.SuspiciousCallSheet
import com.qalqan.antifraud.ui.home.SuspiciousSmsSheet
import com.qalqan.antifraud.web.DomainNormalizer
import com.qalqan.antifraud.web.DomainSeenChecker
import com.qalqan.antifraud.web.LookalikeDetector
import com.qalqan.antifraud.web.LookalikeSeedCatalog
import com.qalqan.antifraud.web.WebEventBuilder
import com.qalqan.antifraud.web.WebManualCapture
import com.qalqan.antifraud.web.WebObserverActionLog
import com.qalqan.antifraud.database.manual.WebEntryDigest
import kotlinx.coroutines.launch
import java.time.Instant

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
                HomeHost(navController = navController, repos = repos)
            }
            composable(AntifraudDestination.Campaigns.route) {
                val app = LocalContext.current.applicationContext as Application
                val vm = remember(repos) { CampaignsViewModel(app, repos) }
                LaunchedEffect(vm) { vm.refresh() }
                val campaignsState by vm.state.collectAsState()
                CampaignListRoute(
                    state = campaignsState,
                    onOpenCampaign = { id ->
                        navController.navigate(AntifraudDestination.CampaignDetail(id).route)
                    },
                )
            }
            composable(
                route = AntifraudDestination.CampaignDetail.ROUTE_PATTERN,
                arguments =
                    listOf(
                        navArgument(AntifraudDestination.CampaignDetail.ARG_CAMPAIGN_ID) {
                            type = NavType.StringType
                        },
                    ),
            ) { entry ->
                val app = LocalContext.current.applicationContext as Application
                val campaignId =
                    entry.arguments
                        ?.getString(AntifraudDestination.CampaignDetail.ARG_CAMPAIGN_ID)
                        .orEmpty()
                val vm =
                    remember(repos) {
                        CampaignDetailViewModel(app, repos, UserSettings(app))
                    }
                LaunchedEffect(campaignId) { vm.load(campaignId) }
                val detailState by vm.state.collectAsState()
                CampaignDetailRoute(
                    state = detailState,
                    onClose = { vm.closeCampaign() },
                    onFalseAlarm = { vm.markFalseAlarm() },
                    onMarkSuspicious = {},
                    onExport = {},
                    onCreatePattern = {},
                )
            }
            composable(AntifraudDestination.Patterns.route) {
                val app = LocalContext.current.applicationContext as Application
                val vm = remember(repos) { PatternsViewModel(app, repos) }
                LaunchedEffect(vm) { vm.refresh() }
                val patternsState by vm.state.collectAsState()
                PatternsRoute(
                    state = patternsState,
                    onToggle = { id, enabled ->
                        vm.setEnabled(id, enabled)
                        vm.refresh()
                    },
                    onResetDefaults = {
                        vm.resetDefaults()
                        vm.refresh()
                    },
                )
            }
            composable(AntifraudDestination.References.route) {
                val app = LocalContext.current.applicationContext as Application
                val vm = remember(repos) { ReferencesViewModel(app, repos) }
                LaunchedEffect(vm) { vm.refresh() }
                val referencesState by vm.state.collectAsState()
                ReferencesRoute(state = referencesState)
            }
            composable(AntifraudDestination.Privacy.route) {
                val app = LocalContext.current.applicationContext as Application
                val vm =
                    remember(repos) {
                        PrivacyViewModel(app, repos, UserSettings(app))
                    }
                LaunchedEffect(vm) { vm.refresh() }
                val privacyState by vm.state.collectAsState()
                PrivacyRoute(
                    state = privacyState,
                    onDeleteAll = { vm.deleteAll() },
                    onDisableSync = { vm.disableSync() },
                    onResetPermissions = { vm.resetPermissions() },
                    onOpenSettings = {
                        navController.navigate(AntifraudDestination.Settings.route)
                    },
                )
            }
            composable(AntifraudDestination.Settings.route) {
                val app = LocalContext.current.applicationContext as Application
                val vm =
                    remember(repos) {
                        SettingsViewModel(app, repos, UserSettings(app))
                    }
                LaunchedEffect(vm) { vm.refresh() }
                val settingsState by vm.state.collectAsState()
                SettingsRoute(
                    state = settingsState,
                    onSensitivityChange = { vm.setSensitivity(it) },
                    onToggleChange = { key, enabled -> vm.setToggle(key, enabled) },
                )
            }
        }
    }
}

@Composable
private fun HomeHost(
    navController: NavHostController,
    repos: Repositories,
) {
    val context = LocalContext.current
    val app = context.applicationContext as Application
    val viewModel = remember(repos) { HomeViewModel(app, repos) }
    LaunchedEffect(viewModel) { viewModel.refresh() }
    val state by viewModel.state.collectAsState()

    var showCallSheet by remember { mutableStateOf(false) }
    var showSmsSheet by remember { mutableStateOf(false) }
    var showSiteSheet by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val manual =
        remember(repos) {
            val box =
                runCatching { KeyStoreCryptoBox.create(app, alias = "antifraud.field_box") }
                    .getOrElse { InMemoryCryptoBox() }
            ManualEntry.create(app, repos, box)
        }

    HomeRoute(
        state = state,
        onSuspiciousCall = { showCallSheet = true },
        onSuspiciousSms = { showSmsSheet = true },
        onSuspiciousSite = { showSiteSheet = true },
        onOpenCampaign = { id ->
            navController.navigate(AntifraudDestination.CampaignDetail(id).route)
        },
        onOpenPrivacy = {
            navController.navigate(AntifraudDestination.Privacy.route)
        },
    )

    if (showCallSheet) {
        SuspiciousCallSheet(
            onDismiss = { showCallSheet = false },
            onSubmit = { raw ->
                scope.launch {
                    runCatching {
                        manual.calls.submit(
                            rawNumber = raw,
                            direction = CallDirection.INCOMING,
                            startedAt = Instant.now(),
                            durationSec = 0,
                            isKnownContact = false,
                        )
                    }
                    viewModel.refresh()
                }
            },
        )
    }
    if (showSmsSheet) {
        SuspiciousSmsSheet(
            onDismiss = { showSmsSheet = false },
            onSubmit = { sender, body ->
                scope.launch {
                    runCatching { manual.sms.submit(sender, Instant.now(), body) }
                    viewModel.refresh()
                }
            },
        )
    }
    if (showSiteSheet) {
        WebEntrySheet(
            onDismiss = { showSiteSheet = false },
            onSubmit = { rawInput, onResult ->
                scope.launch {
                    val capture =
                        WebManualCapture(
                            normalizer = DomainNormalizer(),
                            detector = LookalikeDetector(LookalikeSeedCatalog.seeds),
                            seenChecker = DomainSeenChecker(repos.web),
                            builder = WebEventBuilder(WebEntryDigest.create(app)),
                            repo = repos.web,
                            actionLog = WebObserverActionLog(repos.actionLogger),
                        )
                    val outcome = capture.submit(rawInput, Instant.now())
                    onResult(outcome)
                    viewModel.refresh()
                }
            },
        )
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
