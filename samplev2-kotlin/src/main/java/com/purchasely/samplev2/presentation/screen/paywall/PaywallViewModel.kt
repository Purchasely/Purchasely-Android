package com.purchasely.samplev2.presentation.screen.paywall

import android.app.Activity
import android.widget.Toast
import androidx.lifecycle.ViewModel
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.QueryProductDetailsParams
import com.purchasely.samplev2.data.repository.PreferencesRepositoryImpl.Companion.CONTENT_ID
import com.purchasely.samplev2.data.repository.PreferencesRepositoryImpl.Companion.IS_ASYNC_LOADING
import com.purchasely.samplev2.data.repository.PreferencesRepositoryImpl.Companion.IS_OBSERVER_MODE
import com.purchasely.samplev2.data.repository.PreferencesRepositoryImpl.Companion.PLACEMENT_ID
import com.purchasely.samplev2.data.repository.PreferencesRepositoryImpl.Companion.PRESENTATION_ID
import com.purchasely.samplev2.domain.preferences.PreferencesRepository
import io.purchasely.common.sha256
import io.purchasely.ext.PLYLogger
import io.purchasely.ext.Purchasely
import io.purchasely.ext.presentation.PLYPresentationAction
import io.purchasely.managers.PLYManager
import kotlinx.coroutines.flow.MutableStateFlow

class PaywallViewModel(
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    var uiState = MutableStateFlow(PaywallUiState())

    init {
        with(preferencesRepository) {
            uiState.value = uiState.value.copy(
                placementId = getString(PLACEMENT_ID).takeUnless { it?.isBlank() == true },
                presentationId = getString(PRESENTATION_ID).takeUnless { it?.isBlank() == true },
                contentId = getString(CONTENT_ID).takeUnless { it?.isBlank() == true },
                asyncLoading = getBoolean(IS_ASYNC_LOADING),
            )
        }
    }

    fun observerMode() = preferencesRepository.getBoolean(IS_OBSERVER_MODE)

    /**
     * Handles a [PLYPresentationAction.Purchase] intercepted in observer mode by running the
     * purchase through the host app's own Google Play Billing flow. Fire-and-forget: the
     * interceptor returns [io.purchasely.ext.PLYInterceptResult.SUCCESS] right after calling this
     * (the app is handling the purchase), and [onPurchaseSuccess] is invoked once Billing confirms.
     */
    fun purchase(
        activity: Activity,
        purchase: PLYPresentationAction.Purchase,
        onPurchaseSuccess: () -> Unit
    ) {
        PLYLogger.d("Purchase action intercepted: $purchase")
        val billingClient = BillingClient.newBuilder(activity.applicationContext)
            .setListener { billingResult, _ ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    PLYLogger.d("Purchased ${purchase.plan.vendorId} successfully")
                    // In observer mode, notify Purchasely so it can validate the receipt.
                    Purchasely.synchronize()
                    // acknowledge purchase
                    onPurchaseSuccess()
                } else {
                    PLYLogger.e("Error: ${billingResult.responseCode}")
                    Toast.makeText(
                        activity.applicationContext,
                        "Error: ${billingResult.responseCode}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .enablePendingPurchases(
                PendingPurchasesParams.newBuilder()
                    .enableOneTimeProducts()
                    .build()
            )
            .build()

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingServiceDisconnected() {
                PLYLogger.e("Unable to connect to billing service")
                Toast.makeText(activity.applicationContext, "Unable to connect to billing service", Toast.LENGTH_SHORT)
                    .show()
            }

            override fun onBillingSetupFinished(p0: BillingResult) {
                val sku = purchase.subscriptionOffer?.subscriptionId ?: let {
                    PLYLogger.e("Unable to find subscription ${purchase.subscriptionOffer}")
                    Toast.makeText(
                        activity.applicationContext,
                        "Unable to find subscription ${purchase.subscriptionOffer}",
                        Toast.LENGTH_SHORT
                    ).show()
                    return
                }

                val params = QueryProductDetailsParams.newBuilder().setProductList(
                    listOf(
                        QueryProductDetailsParams.Product.newBuilder()
                            .setProductId(sku)
                            .setProductType(BillingClient.ProductType.SUBS)
                            .build())
                ).build()

                billingClient.queryProductDetailsAsync(params) { result, queryResult ->
                    if(result.responseCode == BillingClient.BillingResponseCode.OK) {
                        queryResult.productDetailsList.orEmpty().forEach { productDetails ->
                            val productDetailsParamsList =
                                listOf(
                                    BillingFlowParams.ProductDetailsParams.newBuilder()
                                        .setProductDetails(productDetails).apply {
                                            //should be null for OTP only
                                            purchase.subscriptionOffer?.offerToken?.let {
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
                        Toast.makeText(activity.applicationContext, "Error: ${result.responseCode}", Toast.LENGTH_SHORT).show()
                    }
                }
            }

        })
    }
}
