package com.purchasely.samplev2.presentation.screen.paywall

import android.app.Activity
import android.util.Log
import android.widget.FrameLayout
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.NavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.purchasely.samplev2.presentation.navigation.Screen
import com.purchasely.samplev2.presentation.util.Constants.Companion.TAG
import io.purchasely.ext.PLYInterceptResult
import io.purchasely.ext.Purchasely
import io.purchasely.ext.presentation.PLYPresentation
import io.purchasely.ext.presentation.PLYPresentationAction
import io.purchasely.ext.presentation.preload
import org.koin.androidx.compose.koinViewModel


@Composable
fun PaywallScreen(navController: NavController, viewModel: PaywallViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    val context = LocalContext.current
    val systemUiController = rememberSystemUiController()
    systemUiController.isSystemBarsVisible = false

    val activity = context as Activity
    with(WindowCompat.getInsetsController(activity.window, activity.window.decorView)) {
        systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }

    // v6: register granular, type-safe per-action interceptors (replaces setPaywallActionsInterceptor)
    DisposableEffect(Unit) {
        Purchasely.interceptAction<PLYPresentationAction.Login> { _, _ ->
            navController.navigateUp()
            navController.navigate(Screen.Settings.route)
            PLYInterceptResult.SUCCESS
        }
        Purchasely.interceptAction<PLYPresentationAction.Purchase> { info, purchase ->
            if (viewModel.observerMode()) {
                info.activity?.let { purchasingActivity ->
                    viewModel.purchase(
                        activity = purchasingActivity,
                        purchase = purchase,
                        onPurchaseSuccess = { navController.navigateUp() },
                    )
                }
                PLYInterceptResult.SUCCESS // the app handles the purchase itself in observer mode
            } else {
                PLYInterceptResult.NOT_HANDLED // let the SDK handle the purchase
            }
        }
        onDispose {
            Purchasely.removeActionInterceptor<PLYPresentationAction.Login>()
            Purchasely.removeActionInterceptor<PLYPresentationAction.Purchase>()
        }
    }

    // v6: build the presentation with the DSL, preload it, then embed buildView() in an AndroidView.
    // This single path covers both the direct and "async" loading modes — there is no longer a
    // separate synchronous presentationView() API.
    val loaded by produceState<PLYPresentation?>(
        initialValue = null,
        uiState.placementId, uiState.presentationId, uiState.contentId
    ) {
        value = try {
            PLYPresentation {
                uiState.placementId?.let { placementId(it) }
                uiState.presentationId?.let { screenId(it) }
                uiState.contentId?.let { contentId(it) }
                onCloseRequested { navController.navigateUp() }
            }.preload()
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching paywall", e)
            null
        }
    }

    val presentation = loaded
    if (presentation != null) {
        AndroidView(
            factory = { ctx -> presentation.buildView(ctx) ?: FrameLayout(ctx) }
        )
    } else if (uiState.asyncLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}
