package com.purchasely.samplev2.presentation.screen.paywall

import android.app.Activity
import android.util.Log
import android.widget.FrameLayout
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClient.BillingResponseCode
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.queryProductDetails
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.purchasely.samplev2.presentation.navigation.Screen
import com.purchasely.samplev2.presentation.util.Constants.Companion.TAG
import io.purchasely.common.sha256
import io.purchasely.ext.LogLevel
import io.purchasely.ext.PLYLogger
import io.purchasely.ext.PLYPresentationAction
import io.purchasely.ext.PLYPresentationDisplayMode
import io.purchasely.ext.PLYPresentationType
import io.purchasely.ext.PLYPresentationViewProperties
import io.purchasely.ext.Purchasely
import io.purchasely.managers.PLYManager
import io.purchasely.presentationViewTemplate
import java.util.Date


@Composable
fun PaywallScreen(navController: NavController, viewModel: PaywallViewModel = hiltViewModel()) {
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
            /*PLYPresentationAction.CLOSE -> {
                if(info.activity == activity) {
                    navController.navigateUp()
                } else {
                    processAction(true)
                }
            }*/
            PLYPresentationAction.LOGIN -> {
                navController.navigateUp()
                navController.navigate(Screen.Settings.route)
            }
            PLYPresentationAction.PURCHASE -> {
                PLYLogger.d("Purchase action intercepted, $parameters")
                if(viewModel.observerMode()) {
                    val billingClient = BillingClient.newBuilder(context)
                        .setListener { billingResult, purchases ->
                            if(billingResult.responseCode == BillingResponseCode.OK) {
                                Purchasely.synchronize()
                                // acknowledge purchase
                                navController.navigateUp()
                            } else {
                                PLYLogger.e("Error: ${billingResult.responseCode}")
                                Toast.makeText(context, "Error: ${billingResult.responseCode}", Toast.LENGTH_SHORT).show()
                            }
                            processAction(false)
                        }
                        .enablePendingPurchases()
                        .build()
                    billingClient.startConnection(object: BillingClientStateListener {
                        override fun onBillingServiceDisconnected() {
                            PLYLogger.e("Unable to connect to billing service")
                            Toast.makeText(context, "Unable to connect to billing service", Toast.LENGTH_SHORT).show()
                            processAction(false)
                        }

                        override fun onBillingSetupFinished(p0: BillingResult) {

                            val sku = parameters.subscriptionOffer?.subscriptionId ?: let {
                                PLYLogger.e("Unable to find subscription ${parameters.subscriptionOffer}")
                                Toast.makeText(context, "Unable to find subscription ${parameters.subscriptionOffer}", Toast.LENGTH_SHORT).show()
                                processAction(false)
                                return
                            }

                            val params = QueryProductDetailsParams.newBuilder().setProductList(
                                listOf(QueryProductDetailsParams.Product.newBuilder()
                                    .setProductId(sku)
                                    .setProductType(BillingClient.ProductType.SUBS)
                                    .build())
                            ).build()

                            billingClient.queryProductDetailsAsync(params) { result, list ->
                                if(result.responseCode == BillingResponseCode.OK) {
                                    list.forEach { productDetails ->
                                        val productDetailsParamsList =
                                            listOf(
                                                BillingFlowParams.ProductDetailsParams.newBuilder()
                                                    .setProductDetails(productDetails).apply {
                                                        //should be null for OTP only
                                                        parameters.subscriptionOffer?.offerToken?.let {
                                                            setOfferToken(it)
                                                        }
                                                    }
                                                    .build()
                                            )
                                        val billingFlowParams =
                                            BillingFlowParams.newBuilder()
                                                .setProductDetailsParamsList(productDetailsParamsList)
                                                .setObfuscatedAccountId(Purchasely.anonymousUserId.sha256())

                                        val userId = PLYManager.storage.vendorUserId
                                        if (!userId.isNullOrEmpty()) {
                                            billingFlowParams.setObfuscatedProfileId(userId.sha256())
                                        }

                                        billingClient.launchBillingFlow(activity, billingFlowParams.build())
                                    }
                                } else {
                                    PLYLogger.e("Error: ${result.responseCode}")
                                    Toast.makeText(context, "Error: ${result.responseCode}", Toast.LENGTH_SHORT).show()
                                    processAction(false)
                                }
                            }
                        }

                    })
                } else {
                    processAction(true)
                }
            }
            else -> {
                processAction(true)
            }
        }
    }

    if (uiState.value.asyncLoading && uiState.value.template == null) {
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
            if(uiState.value.presentation?.type == PLYPresentationType.CLIENT) {
                val metadata = uiState.value.presentation?.metadata ?: return
                val array = remember {
                    mutableMapOf<String, Any?>()
                }
                LaunchedEffect(Unit) {
                    metadata.keys().forEach {
                        val value: Any? = when (val type = metadata.type(it)) {
                            Boolean::class.java.simpleName -> metadata.getBoolean(it)
                            String::class.java.simpleName -> metadata.getString(it)
                            Date::class.java.simpleName -> metadata.getDate(it)
                            Int::class.java.simpleName -> metadata.getInt(it)
                            Float::class.java.simpleName -> metadata.getFloat(it)
                            Double::class.java.simpleName -> metadata.getDouble(it)
                            Long::class.java.simpleName -> metadata.getLong(it)
                            else -> null
                        }
                        array[it] = value
                    }
                }

                Column {
                    Text(text = "Client presentation")
                    array.forEach {
                        Text(text = "${it.key}: ${it.value}")
                    }
                }

            } else {
                AsyncPresentationView(
                    state = uiState.value,
                    onCloseClick = { navController.navigateUp() },
                    onLoginClick = {
                        navController.navigateUp()
                        navController.navigate(Screen.Settings.route)
                    }
                )
            }
        }
    } else {
        PresentationView(
            state = uiState.value,
            onCloseClick = { navController.navigateUp() },
            onLoginClick = {
                navController.navigateUp()
                navController.navigate(Screen.Settings.route)
            }
        )
    }
}

@Composable
fun PresentationView(
    state: PaywallUiState,
    onCloseClick: () -> Unit,
    onLoginClick: () -> Unit
) {
    AndroidView(
        factory = { context ->
            Purchasely.presentationViewTemplate(
                context = context,
                properties = state.properties.copy(onClose = onCloseClick),
                template = state.template?.plus(".json"),
                displayMode = PLYPresentationDisplayMode.DEFAULT
            ) { result, plan ->
                Log.d("Demo", "result: $result, plan: $plan")
                Toast.makeText(context, "$result ${if(plan != null) plan.name else ""}", Toast.LENGTH_SHORT).show()
            } ?: FrameLayout(context)
        },
        update = { }
    )
}


@Composable
fun AsyncPresentationView(
    state: PaywallUiState,
    onCloseClick: () -> Unit,
    onLoginClick: () -> Unit
) {
    AndroidView(
        factory = { context ->
            state.presentation?.buildView(
                context = context,
                viewProperties = PLYPresentationViewProperties(
                    onClose = { onCloseClick() },
                )
            ) { result, plan ->
                Log.d("Demo", "result: $result, plan: $plan")
                Toast.makeText(context, "$result ${if(plan != null) plan.name else ""}", Toast.LENGTH_SHORT).show()
            } ?: FrameLayout(context)
        },
        update = { }
    )
}