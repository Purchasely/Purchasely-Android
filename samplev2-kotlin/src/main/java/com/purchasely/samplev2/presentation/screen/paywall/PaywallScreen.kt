package com.purchasely.samplev2.presentation.screen.paywall

import android.app.Activity
import android.util.Log
import android.widget.FrameLayout
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.NavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.purchasely.samplev2.presentation.navigation.Screen
import com.purchasely.samplev2.presentation.util.Constants.Companion.TAG
import io.purchasely.ext.PLYPresentationAction
import io.purchasely.ext.PLYPresentationProperties
import io.purchasely.ext.Purchasely
import org.koin.androidx.compose.koinViewModel


@Composable
fun PaywallScreen(navController: NavController, viewModel: PaywallViewModel = koinViewModel()) {
    val uiState = viewModel.uiState.collectAsState()

    val context = LocalContext.current
    val systemUiController = rememberSystemUiController()
    systemUiController.isSystemBarsVisible = false

    val activity = context as Activity
    with(WindowCompat.getInsetsController(activity.window, activity.window.decorView)) {
        systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }

    Purchasely.setPaywallActionsInterceptor { info, action, parameters, processAction ->
        if (info?.activity == null) {
            processAction(true)
            return@setPaywallActionsInterceptor
        }
        when (action) {
            PLYPresentationAction.LOGIN -> {
                navController.navigateUp()
                navController.navigate(Screen.Settings.route)
            }
            PLYPresentationAction.PURCHASE -> {
                if(viewModel.observerMode()) {
                    viewModel.purchase(
                        activity = activity,
                        parameters = parameters,
                        processAction = processAction,
                        onPurchaseSuccess = {
                            navController.navigateUp()
                        },
                    )
                }
                else {
                    processAction(true)
                }
            }
            else -> {
                processAction(true)
            }
        }
    }

    if (uiState.value.asyncLoading) {
        val isFinished = remember { mutableStateOf(false) }
        LaunchedEffect(Unit) {
            Purchasely.fetchPresentation(properties = uiState.value.properties) { presentation, error ->
                error?.let {
                    Log.e(TAG, "Error fetching paywall", error)
                    return@fetchPresentation
                }
                uiState.value.presentation = presentation
                isFinished.value = true
            }
        }

        if (isFinished.value) {
            AsyncPresentationView(
                state = uiState.value,
                onCloseClick = { navController.navigateUp() }
            )
        }
    } else {
        PresentationView(
            state = uiState.value,
            onCloseClick = { navController.navigateUp() }
        )
    }
}

@Composable
fun PresentationView(state: PaywallUiState, onCloseClick: () -> Unit) {
    AndroidView(
        factory = { context ->
            Purchasely.presentationView(
                context = context,
                properties = state.properties.copy(onClose = onCloseClick),
            ) ?: FrameLayout(context)
        }
    )
}


@Composable
fun AsyncPresentationView(state: PaywallUiState, onCloseClick: () -> Unit) {
    AndroidView(
        factory = { context ->
            state.presentation?.buildView(
                context = context,
                properties = PLYPresentationProperties(
                    onClose = { onCloseClick() },
                )
            ) ?: FrameLayout(context)
        }
    )
}