package com.zilehasnain.qazatracker.ui.navigation

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
import com.zilehasnain.qazatracker.ui.achievements.AchievementsScreen
import com.zilehasnain.qazatracker.ui.baseline.BaselineSummaryScreen
import com.zilehasnain.qazatracker.ui.batch.BatchLoggingScreen
import com.zilehasnain.qazatracker.ui.dashboard.DashboardScreen
import com.zilehasnain.qazatracker.ui.history.HistoryScreen
import com.zilehasnain.qazatracker.ui.inspiration.DailyInspirationScreen
import com.zilehasnain.qazatracker.ui.onboarding.OnboardingScreen
import com.zilehasnain.qazatracker.ui.prayertimes.PrayerTimesScreen
import com.zilehasnain.qazatracker.ui.settings.SettingsScreen
import com.zilehasnain.qazatracker.ui.statistics.StatisticsScreen
import com.zilehasnain.qazatracker.ui.tutorial.TutorialScreen

/**
 * Tutorial, onboarding and baseline summary are first-run-only: the tutorial shows once for a
 * brand-new user and hands off to onboarding; once a baseline exists, the graph starts straight
 * at the dashboard, and each of these clears itself from the back stack (see popUpTo below) so
 * back-navigation can't return to it. Re-reaching them later is meant to go through a future
 * reset/re-onboard action, not normal navigation.
 */
@Composable
fun QazaNavHost(modifier: Modifier = Modifier, appViewModel: AppViewModel = hiltViewModel()) {
    val resolvedStartDestination by appViewModel.startDestination.collectAsStateWithLifecycle()
    val startDestination = resolvedStartDestination

    if (startDestination == null) {
        Box(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background))
        return
    }

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Routes.TUTORIAL) {
            TutorialScreen(
                onFinished = {
                    navController.navigate(Routes.ONBOARDING) {
                        popUpTo(Routes.TUTORIAL) { inclusive = true }
                    }
                }
            )
        }

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
            DashboardScreen(
                onLogBatchClicked = { navController.navigate(Routes.BATCH_LOGGING) },
                onHistoryClicked = { navController.navigate(Routes.HISTORY) },
                onSettingsClicked = { navController.navigate(Routes.SETTINGS) },
                onStatisticsClicked = { navController.navigate(Routes.STATISTICS) },
                onAchievementsClicked = { navController.navigate(Routes.ACHIEVEMENTS) },
                onPrayerTimesClicked = { navController.navigate(Routes.PRAYER_TIMES) },
                onInspirationClicked = { navController.navigate(Routes.INSPIRATION) }
            )
        }

        composable(Routes.BATCH_LOGGING) {
            BatchLoggingScreen(
                onBack = { navController.popBackStack() },
                onLogged = { navController.popBackStack() }
            )
        }

        composable(Routes.HISTORY) {
            HistoryScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onPrayerTimesClicked = { navController.navigate(Routes.PRAYER_TIMES) }
            )
        }

        composable(Routes.STATISTICS) {
            StatisticsScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.PRAYER_TIMES) {
            PrayerTimesScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.INSPIRATION) {
            DailyInspirationScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.ACHIEVEMENTS) {
            AchievementsScreen(onBack = { navController.popBackStack() })
        }
    }
}
