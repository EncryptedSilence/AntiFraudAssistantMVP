@file:Suppress("LongMethod")

package com.qalqan.antifraud.ui.nav

import android.Manifest
import android.app.Application
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.qalqan.antifraud.alerts.AlertPermissionRequester
import com.qalqan.antifraud.alerts.FullScreenIntentPermissionGate
import com.qalqan.antifraud.database.Repositories
import com.qalqan.antifraud.settings.OnboardingStep
import com.qalqan.antifraud.settings.UserSettings
import com.qalqan.antifraud.sms.SmsPermissionRequester
import com.qalqan.antifraud.ui.activity.ActivityRoute
import com.qalqan.antifraud.ui.add.AddRoute
import com.qalqan.antifraud.ui.campaign.CampaignDetailRoute
import com.qalqan.antifraud.ui.campaign.CampaignDetailViewModel
import com.qalqan.antifraud.ui.campaign.CampaignListRoute
import com.qalqan.antifraud.ui.campaign.CampaignsViewModel
import com.qalqan.antifraud.ui.home.HomeRoute
import com.qalqan.antifraud.ui.home.HomeViewModel
import com.qalqan.antifraud.ui.lists.ListsRoute
import com.qalqan.antifraud.ui.manual.ManualEntrySheetsHost
import com.qalqan.antifraud.ui.onboarding.OnboardingRoute
import com.qalqan.antifraud.ui.onboarding.OnboardingViewModel
import com.qalqan.antifraud.ui.patterns.PatternsRoute
import com.qalqan.antifraud.ui.patterns.PatternsViewModel
import com.qalqan.antifraud.ui.privacy.PrivacyRoute
import com.qalqan.antifraud.ui.privacy.PrivacyViewModel
import com.qalqan.antifraud.ui.profile.ProfileRoute
import com.qalqan.antifraud.ui.references.ReferencesRoute
import com.qalqan.antifraud.ui.references.ReferencesViewModel
import com.qalqan.antifraud.ui.settings.SettingsRoute
import com.qalqan.antifraud.ui.settings.SettingsViewModel

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
            composable(AntifraudDestination.Home.route) { entry ->
                HomeHost(navController = navController, repos = repos, lifecycleOwner = entry)
            }
            composable(AntifraudDestination.Activity.route) {
                ActivityRoute()
            }
            composable(AntifraudDestination.Add.route) {
                AddHost(repos = repos)
            }
            composable(AntifraudDestination.Lists.route) {
                ListsRoute()
            }
            composable(AntifraudDestination.Profile.route) {
                ProfileRoute(
                    onOpenSettings = { navController.navigate(AntifraudDestination.Settings.route) },
                    onOpenPatterns = { navController.navigate(AntifraudDestination.Patterns.route) },
                    onOpenReferences = { navController.navigate(AntifraudDestination.References.route) },
                    onOpenCampaigns = { navController.navigate(AntifraudDestination.Campaigns.route) },
                    onOpenPrivacy = { navController.navigate(AntifraudDestination.Privacy.route) },
                )
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
                    onBack = { navController.popBackStack() },
                    onClose = {
                        vm.closeCampaign()
                        navController.popBackStack()
                    },
                    onFalseAlarm = {
                        vm.markFalseAlarm()
                        navController.popBackStack()
                    },
                    onMarkSuspicious = {},
                    onExport = {},
                    onCreatePattern = {},
                    onAnswerQuestion = { kind, value ->
                        // No related event id in the current detail wire-up; pass a
                        // synthetic id so the answer is still recorded.
                        vm.answerQuestion(kind, value, relatedEventId = "ui-detail-$campaignId")
                    },
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
            composable(AntifraudDestination.Onboarding.route) {
                val context = LocalContext.current
                val app = context.applicationContext as Application
                val vm =
                    remember(repos) {
                        OnboardingViewModel(app, repos, UserSettings(app))
                    }
                val onboardingState by vm.state.collectAsState()
                val permissionLauncher =
                    rememberLauncherForActivityResult(
                        ActivityResultContracts.RequestMultiplePermissions(),
                    ) { results ->
                        if (results.values.all { it }) {
                            vm.grantCurrent()
                        } else {
                            vm.skipCurrent()
                        }
                    }
                val settingsLauncher =
                    rememberLauncherForActivityResult(
                        ActivityResultContracts.StartActivityForResult(),
                    ) {
                        val step = onboardingState.currentStep
                        val granted =
                            when (step) {
                                OnboardingStep.FULL_SCREEN_INTENT ->
                                    FullScreenIntentPermissionGate(context).fullScreenAllowed()
                                OnboardingStep.OVERLAY_WINDOW -> Settings.canDrawOverlays(context)
                                OnboardingStep.BATTERY_OPTIMIZATION -> isIgnoringBatteryOptimizations(context)
                                else -> false
                            }
                        if (granted) {
                            vm.grantCurrent()
                        } else {
                            vm.skipCurrent()
                        }
                    }
                OnboardingRoute(
                    state = onboardingState,
                    onGrant = {
                        when (onboardingState.currentStep) {
                            OnboardingStep.NOTIFICATIONS ->
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    permissionLauncher.launch(arrayOf(Manifest.permission.POST_NOTIFICATIONS))
                                } else {
                                    vm.grantCurrent()
                                }
                            OnboardingStep.PHONE ->
                                permissionLauncher.launch(arrayOf(Manifest.permission.READ_PHONE_STATE))
                            OnboardingStep.CALL_LOG ->
                                permissionLauncher.launch(arrayOf(Manifest.permission.READ_CALL_LOG))
                            OnboardingStep.SMS ->
                                permissionLauncher.launch(SmsPermissionRequester.requestList().toTypedArray())
                            OnboardingStep.FULL_SCREEN_INTENT -> {
                                val intent =
                                    AlertPermissionRequester.fullScreenIntentSettingsIntent(context.packageName)
                                if (intent != null) {
                                    settingsLauncher.launch(intent)
                                } else {
                                    vm.grantCurrent()
                                }
                            }
                            OnboardingStep.OVERLAY_WINDOW ->
                                settingsLauncher.launch(
                                    AlertPermissionRequester.overlaySettingsIntent(context.packageName),
                                )
                            OnboardingStep.BATTERY_OPTIMIZATION ->
                                settingsLauncher.launch(batteryOptimizationIntent(context.packageName))
                            null -> Unit
                        }
                    },
                    onSkip = { vm.skipCurrent() },
                    onFinish = {
                        vm.finish()
                        navController.navigate(AntifraudDestination.Home.route) {
                            popUpTo(AntifraudDestination.Onboarding.route) { inclusive = true }
                        }
                    },
                )
            }
        }
    }
}

private fun isIgnoringBatteryOptimizations(context: android.content.Context): Boolean {
    val powerManager = context.getSystemService(PowerManager::class.java)
    return powerManager?.isIgnoringBatteryOptimizations(context.packageName) == true
}

private fun batteryOptimizationIntent(packageName: String): Intent =
    Intent(
        Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
        Uri.parse("package:$packageName"),
    )

@Composable
private fun HomeHost(
    navController: NavHostController,
    repos: Repositories,
    lifecycleOwner: LifecycleOwner,
) {
    val context = LocalContext.current
    val app = context.applicationContext as Application
    val viewModel = remember(repos) { HomeViewModel(app, repos) }
    // Home is the bottom-nav anchor (restoreState), so a one-shot LaunchedEffect would only run
    // on cold launch. Re-read on every ON_RESUME so returning to the tab (or foregrounding the
    // app) reflects campaigns closed elsewhere or data seeded under the screen.
    DisposableEffect(viewModel, lifecycleOwner) {
        val observer =
            LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) viewModel.refresh()
            }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    val state by viewModel.state.collectAsState()

    HomeRoute(
        state = state,
        onOpenCampaign = { id ->
            navController.navigate(AntifraudDestination.CampaignDetail(id).route)
        },
        onDismissEducationalCard = { viewModel.dismissEducationalCard() },
    )
}

@Composable
private fun AddHost(repos: Repositories) {
    ManualEntrySheetsHost(repos = repos) { openCall, openSms, openSite ->
        AddRoute(onAddCall = openCall, onAddSms = openSms, onAddSite = openSite)
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
                icon = {
                    val ic = dest.icon
                    if (ic != null) {
                        Icon(ic, contentDescription = null)
                    } else {
                        Text(stringResource(dest.labelResId).take(1))
                    }
                },
                label = {
                    Text(
                        text = stringResource(dest.labelResId),
                        maxLines = 1,
                        softWrap = false,
                        style = MaterialTheme.typography.labelSmall,
                    )
                },
            )
        }
    }
}
