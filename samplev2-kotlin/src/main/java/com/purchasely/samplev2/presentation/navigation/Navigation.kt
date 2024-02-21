package com.purchasely.samplev2.presentation.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.purchasely.samplev2.presentation.screen.attributes.AttributesScreen
import com.purchasely.samplev2.presentation.screen.home.HomeScreen
import com.purchasely.samplev2.presentation.screen.paywall.PaywallScreen
import com.purchasely.samplev2.presentation.screen.products.ProductsScreen
import com.purchasely.samplev2.presentation.screen.settings.SettingsScreen

const val animationDuration = 200
val enterTransition = slideInHorizontally(
    initialOffsetX = { 400 }, animationSpec = tween(animationDuration)
) + fadeIn(animationSpec = tween(animationDuration))

val exitTransition = slideOutHorizontally(
    targetOffsetX = { -400 },
    animationSpec = tween(animationDuration)
) + fadeOut(animationSpec = tween(animationDuration))

val popEnterTransition = slideInHorizontally(
    initialOffsetX = { -400 },
    animationSpec = tween(animationDuration)
) + fadeIn(animationSpec = tween(animationDuration))

val popExitTransition = slideOutHorizontally(
    targetOffsetX = { 400 },
    animationSpec = tween(animationDuration)
) + fadeOut(animationSpec = tween(animationDuration))


internal sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Paywall : Screen("paywall")
    object Products : Screen("products")
    object Settings : Screen("settings")
    object Attributes : Screen("attributes")
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun Navigation() {
    val navController = rememberAnimatedNavController()
    AnimatedNavHost(navController, startDestination = Screen.Home.route) {
        composable(route = Screen.Home.route) {
            HomeScreen(navController)
        }
        composable(
            route = Screen.Products.route,
            enterTransition = { enterTransition },
            exitTransition = { exitTransition },
            popEnterTransition = { popEnterTransition},
            popExitTransition = { popExitTransition }
        ) {
            ProductsScreen(navController)
        }
        composable(
            route = Screen.Settings.route,
            enterTransition = { enterTransition },
            exitTransition = { exitTransition },
            popEnterTransition = { popEnterTransition},
            popExitTransition = { popExitTransition }
        ) {
            SettingsScreen(navController)
        }
        composable(
            route = Screen.Attributes.route,
            enterTransition = { enterTransition },
            exitTransition = { exitTransition },
            popEnterTransition = { popEnterTransition},
            popExitTransition = { popExitTransition }
        ) {
            AttributesScreen(navController)
        }
        composable(route = Screen.Paywall.route) {
            PaywallScreen(navController)
        }
    }
}