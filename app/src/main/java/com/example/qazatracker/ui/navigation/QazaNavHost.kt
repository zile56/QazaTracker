package com.example.qazatracker.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.qazatracker.ui.baseline.BaselineSummaryScreen
import com.example.qazatracker.ui.onboarding.OnboardingScreen

@Composable
fun QazaNavHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.ONBOARDING,
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
            BaselineSummaryScreen(onBack = { navController.popBackStack() })
        }
    }
}
