package com.purchasely.samplev2.presentation.navigation

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.purchasely.samplev2.presentation.screen.attributes.AttributesScreen
import com.purchasely.samplev2.presentation.screen.built_in_attributes.BuiltInAttributesScreen
import com.purchasely.samplev2.presentation.screen.custom_events.CustomEventsScreen
import com.purchasely.samplev2.presentation.screen.home.HomeScreen
import com.purchasely.samplev2.presentation.screen.logs.LogsScreen
import com.purchasely.samplev2.presentation.screen.offerings.OfferingsScreen
import com.purchasely.samplev2.presentation.screen.paywall.PaywallScreen
import com.purchasely.samplev2.presentation.screen.privacy.PrivacyScreen
import com.purchasely.samplev2.presentation.screen.products.ProductsScreen
import com.purchasely.samplev2.presentation.screen.settings.SettingsScreen
import com.purchasely.samplev2.presentation.screen.subscriptions.SubscriptionsScreen

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
    object BuiltInAttributes : Screen("built_in_attributes")
    object CustomEvents : Screen("custom_events")
    object Offerings : Screen("offerings")
    object Logs : Screen("logs")
    object Privacy : Screen("privacy")
    object Subscriptions : Screen("subscriptions")
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun Navigation() {
    val navController = rememberNavController()
    NavHost(navController, startDestination = Screen.Home.route) {
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
        composable(
            route = Screen.BuiltInAttributes.route,
            enterTransition = { enterTransition },
            exitTransition = { exitTransition },
            popEnterTransition = { popEnterTransition},
            popExitTransition = { popExitTransition }
        ) {
            BuiltInAttributesScreen(navController)
        }
        composable(
            route = Screen.CustomEvents.route,
            enterTransition = { enterTransition },
            exitTransition = { exitTransition },
            popEnterTransition = { popEnterTransition},
            popExitTransition = { popExitTransition }
        ) {
            CustomEventsScreen(navController)
        }
        composable(
            route = Screen.Logs.route,
            enterTransition = { enterTransition },
            exitTransition = { exitTransition },
            popEnterTransition = { popEnterTransition},
            popExitTransition = { popExitTransition }
        ) {
            LogsScreen(navController)
        }
        composable(route = Screen.Paywall.route) {
            PaywallScreen(navController)
        }

        composable(
            route = Screen.Offerings.route,
            enterTransition = { enterTransition },
            exitTransition = { exitTransition },
            popEnterTransition = { popEnterTransition},
            popExitTransition = { popExitTransition }
        ) {
            OfferingsScreen(navController)
        }

        composable(route = Screen.Privacy.route) {
            PrivacyScreen(navController)
        }

        composable(
            route = Screen.Subscriptions.route,
            enterTransition = { enterTransition },
            exitTransition = { exitTransition },
            popEnterTransition = { popEnterTransition},
            popExitTransition = { popExitTransition }
        ) {
            SubscriptionsScreen(navController)
        }
    }
}