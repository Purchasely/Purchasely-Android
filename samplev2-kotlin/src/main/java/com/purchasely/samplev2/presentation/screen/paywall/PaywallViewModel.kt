package com.purchasely.samplev2.presentation.screen.paywall

import android.app.Activity
import android.widget.Toast
import androidx.lifecycle.ViewModel
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.QueryProductDetailsParams
import com.purchasely.samplev2.data.repository.PreferencesRepositoryImpl.Companion.CONTENT_ID
import com.purchasely.samplev2.data.repository.PreferencesRepositoryImpl.Companion.IS_ASYNC_LOADING
import com.purchasely.samplev2.data.repository.PreferencesRepositoryImpl.Companion.IS_OBSERVER_MODE
import com.purchasely.samplev2.data.repository.PreferencesRepositoryImpl.Companion.PLACEMENT_ID
import com.purchasely.samplev2.data.repository.PreferencesRepositoryImpl.Companion.PRESENTATION_ID
import com.purchasely.samplev2.domain.preferences.PreferencesRepository
import io.purchasely.common.sha256
import io.purchasely.ext.PLYCompletionHandler
import io.purchasely.ext.PLYLogger
import io.purchasely.ext.PLYPresentationActionParameters
import io.purchasely.ext.PLYPresentationProperties
import io.purchasely.ext.Purchasely
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
                asyncLoading = getBoolean(IS_ASYNC_LOADING),
                properties = getProperties()
            )
        }
    }

    /**
     * Set purchasely properties.
     */
    private fun getProperties() =
        PLYPresentationProperties(
            placementId = preferencesRepository.getString(PLACEMENT_ID).takeUnless { it?.isBlank() == true },
            presentationId = preferencesRepository.getString(PRESENTATION_ID).takeUnless { it?.isBlank() == true },
            contentId = preferencesRepository.getString(CONTENT_ID).takeUnless { it?.isBlank() == true },
            onLoaded = {}
        )

    fun observerMode() = preferencesRepository.getBoolean(IS_OBSERVER_MODE)

    fun purchase(
        activity: Activity,
        parameters: PLYPresentationActionParameters,
        processAction: PLYCompletionHandler,
        onPurchaseSuccess: () -> Unit
    ) {
        PLYLogger.d("Purchase action intercepted: $parameters")
        val billingClient = BillingClient.newBuilder(activity.applicationContext)
            .setListener { billingResult, _ ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    PLYLogger.d("Purchased ${parameters.plan?.vendorId} successfully")
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
                processAction(false)
            }
            .enablePendingPurchases()
            .build()

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingServiceDisconnected() {
                PLYLogger.e("Unable to connect to billing service")
                Toast.makeText(activity.applicationContext, "Unable to connect to billing service", Toast.LENGTH_SHORT)
                    .show()
                processAction(false)
            }

            override fun onBillingSetupFinished(p0: BillingResult) {
                val sku = parameters.subscriptionOffer?.subscriptionId ?: let {
                    PLYLogger.e("Unable to find subscription ${parameters.subscriptionOffer}")
                    Toast.makeText(
                        activity.applicationContext,
                        "Unable to find subscription ${parameters.subscriptionOffer}",
                        Toast.LENGTH_SHORT
                    ).show()
                    processAction(false)
                    return
                }

                val params = QueryProductDetailsParams.newBuilder().setProductList(
                    listOf(
                        QueryProductDetailsParams.Product.newBuilder()
                            .setProductId(sku)
                            .setProductType(BillingClient.ProductType.SUBS)
                            .build())
                ).build()

                billingClient.queryProductDetailsAsync(params) { result, list ->
                    if(result.responseCode == BillingClient.BillingResponseCode.OK) {
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
                        Toast.makeText(activity.applicationContext, "Error: ${result.responseCode}", Toast.LENGTH_SHORT).show()
                        processAction(false)
                    }
                }
            }

        })
    }
}
