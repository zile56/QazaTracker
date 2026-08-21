package com.example.qazatracker.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.qazatracker.ui.baseline.BaselineSummaryScreen
import com.example.qazatracker.ui.batch.BatchLoggingScreen
import com.example.qazatracker.ui.dashboard.DashboardScreen
import com.example.qazatracker.ui.onboarding.OnboardingScreen

/**
 * Onboarding and baseline summary are first-run-only: once a baseline exists, the graph
 * starts straight at the dashboard, and confirming a baseline clears them from the back
 * stack (see popUpTo below) so back-navigation can't return to them. Re-reaching them
 * later is meant to go through a future reset/re-onboard action, not normal navigation.
 */
@Composable
fun QazaNavHost(modifier: Modifier = Modifier, appViewModel: AppViewModel = hiltViewModel()) {
    val hasBaseline by appViewModel.hasBaseline.collectAsStateWithLifecycle()
    val knownHasBaseline = hasBaseline

    if (knownHasBaseline == null) {
        Box(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background))
        return
    }

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = if (knownHasBaseline) Routes.DASHBOARD else Routes.ONBOARDING,
        modifier = modifier
    ) {
        composable(Routes.ONBOARDING) {
            OnboardingScreen(
                onBaselineCalculated = { result ->
                    navController.navigate(Routes.baselineSummary(result.missedDays, result.method))
                }
            )
        }

        composable(
            route = Routes.BASELINE_SUMMARY_ROUTE,
            arguments = listOf(
                navArgument(Routes.MISSED_DAYS_ARG) { type = NavType.LongType },
                navArgument(Routes.METHOD_ARG) { type = NavType.StringType }
            )
        ) {
            BaselineSummaryScreen(
                onBack = { navController.popBackStack() },
                onBaselineConfirmed = {
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.DASHBOARD) {
            DashboardScreen(onLogBatchClicked = { navController.navigate(Routes.BATCH_LOGGING) })
        }

        composable(Routes.BATCH_LOGGING) {
            BatchLoggingScreen(
                onBack = { navController.popBackStack() },
                onLogged = { navController.popBackStack() }
            )
        }
    }
}
