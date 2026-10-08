package com.purchasely.samplev2.presentation.util

import android.app.Activity
import android.app.AlertDialog
import android.widget.Toast
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingFlowParams.SubscriptionUpdateParams.ReplacementMode
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.QueryProductDetailsParams
import io.purchasely.common.sha256
import io.purchasely.ext.DistributionType
import io.purchasely.ext.PLYInterceptResult
import io.purchasely.ext.PLYLogger
import io.purchasely.ext.StoreType
import io.purchasely.ext.presentation.PLYPresentationAction
import io.purchasely.ext.Purchasely
import com.purchasely.samplev2.SampleV2Application
import com.purchasely.samplev2.data.repository.PreferencesRepositoryImpl.Companion.USER_ID
import java.util.concurrent.atomic.AtomicInteger
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine


suspend fun purchase(
    activity: Activity,
    action: PLYPresentationAction.Purchase,
    onCloseRequested: () -> Unit
): PLYInterceptResult = suspendCancellableCoroutine { continuation ->
    PLYLogger.d("Purchase action intercepted: $action")

    // The interceptor stays suspended while we run the async BillingClient flow, which keeps
    // the paywall button spinning (the SDK only calls hideProgress() once the interceptor
    // returns). We resume exactly once: SUCCESS the moment the success dialog shows, FAILED on
    // error/cancellation — mirroring Full mode, where the spinner lasts the whole purchase.
    fun finish(result: PLYInterceptResult) {
        if (continuation.isActive) continuation.resume(result)
    }

    fun reportError(message: String) {
        PLYLogger.e(message)
        Toast.makeText(activity.applicationContext, message, Toast.LENGTH_SHORT).show()
        finish(PLYInterceptResult.FAILED)
    }

    lateinit var billingClient: BillingClient
    billingClient = BillingClient.newBuilder(activity.applicationContext)
        .setListener { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val purchaseList = purchases.orEmpty()
                val purchased = purchaseList.filter {
                    it.purchaseState == Purchase.PurchaseState.PURCHASED
                }

                if (purchased.isEmpty()) {
                    // No completed purchase yet: the payment is PENDING (slow card, cash,
                    // SEPA, parental approval…) or Google returned a deferred response with no
                    // purchase. Mirror the SDK: do NOT acknowledge and do NOT grant access —
                    // a pending purchase only unlocks once Google approves the payment. We
                    // still notify Purchasely so it tracks the transaction; it re-validates
                    // the purchase on its next product refresh / synchronize() (which picks up
                    // any not-yet-acknowledged purchase), so no client-side tracking is needed.
                    PLYLogger.d("Purchase pending for ${action.plan?.vendorId}; awaiting Google approval")
                    Toast.makeText(
                        activity.applicationContext,
                        "Your purchase is pending. Access will unlock once the payment is approved.",
                        Toast.LENGTH_LONG
                    ).show()
                    Purchasely.synchronize(onSuccess = {}, onError = {})
                    billingClient.safeEndConnection()
                    onCloseRequested()
                    finish(PLYInterceptResult.SUCCESS)
                    return@setListener
                }

                PLYLogger.d("Purchased ${action.plan?.vendorId} successfully")
                // In Observer mode the app owns the BillingClient, so the app must
                // acknowledge (or consume) the purchase itself — Google revokes and refunds
                // any purchase left unacknowledged for three days. Do this *before* ending
                // the connection, then sync entitlements with Purchasely.
                // https://developer.android.com/google/play/billing/integrate#process
                billingClient.acknowledgePurchases(
                    purchases = purchased,
                    isConsumable = action.plan?.type == DistributionType.CONSUMABLE
                ) {
                    // The SDK refreshes its subscriptions cache from the receipt-validation
                    // response before firing onSuccess, so subsequent reads see the new
                    // subscription without a separate userSubscriptions refresh. Showing the
                    // dialog ends the interceptor (SUCCESS) so the spinner stops exactly as the
                    // dialog appears; the paywall is closed by onCloseRequested() only when the
                    // user taps the dialog button.
                    val confirmPurchase = {
                        showPurchaseSuccessDialog(activity, onConfirmed = onCloseRequested)
                        finish(PLYInterceptResult.SUCCESS)
                    }
                    Purchasely.synchronize(
                        onSuccess = { confirmPurchase() },
                        onError = { error ->
                            PLYLogger.e("Purchase succeeded; SDK sync failed (will retry on next foreground): " + (error?.message ?: "unknown"))
                            confirmPurchase()
                        }
                    )
                    billingClient.safeEndConnection()
                }
            } else {
                reportError("Error: ${billingResult.responseCode}")
                billingClient.safeEndConnection()
            }
        }
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder().enableOneTimeProducts().build()
        )
        .build()

    // If the paywall/interceptor is cancelled while the flow is in-flight, release the client.
    continuation.invokeOnCancellation { billingClient.safeEndConnection() }

    billingClient.startConnection(object : BillingClientStateListener {
        override fun onBillingServiceDisconnected() {
            reportError("Unable to connect to billing service")
            billingClient.safeEndConnection()
        }

        override fun onBillingSetupFinished(p0: BillingResult) {
            val sku = action.subscriptionOffer?.subscriptionId ?: let {
                reportError("Unable to find subscription ${action.subscriptionOffer}")
                billingClient.safeEndConnection()
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
                    val productDetails = list.productDetailsList.firstOrNull() ?: run {
                        reportError("No product details for $sku")
                        billingClient.safeEndConnection()
                        return@queryProductDetailsAsync
                    }
                    // If the user already owns a subscription for this product, replace it
                    // (upgrade/downgrade) instead of starting a parallel one. The replacement
                    // mode is chosen from the plans' levels, mirroring the Purchasely SDK.
                    resolveSubscriptionUpdate(action, newSku = sku) { subscriptionUpdate ->
                        // The app's own user id (set at login), not an SDK storage read.
                        val appUserId = (activity.application as SampleV2Application).preferencesRepository.getString(USER_ID)
                        billingClient.launchPurchaseFlow(activity, action, productDetails, subscriptionUpdate, appUserId)
                    }
                } else {
                    reportError("Error: ${result.responseCode}")
                    billingClient.safeEndConnection()
                }
            }
        }

    })
}

/**
 * Resolves the [BillingFlowParams.SubscriptionUpdateParams] for an upgrade/downgrade — or null for
 * a plain new purchase — then invokes [onResolved].
 *
 * Mirrors the Purchasely SDK: if the user already owns a Google Play subscription for the *same
 * product* under a *different* SKU (Google handles same-SKU/base-plan changes itself), the new
 * subscription replaces it. The replacement mode is derived from the plans' `level` values, using
 * the SDK's default policies (see `PLYConfiguration`):
 *   - upgrade   (new level > old level) -> WITH_TIME_PRORATION (IMMEDIATE_WITH_TIME_PRORATION)
 *   - downgrade (new level < old level) -> DEFERRED
 *   - same / unknown level              -> WITHOUT_PRORATION   (IMMEDIATE_WITHOUT_PRORATION)
 *
 * The user's current subscriptions come straight from Purchasely (`userSubscriptions`), which
 * already carries the owned plan, its level and the Google purchase token — no extra Google query
 * is needed. On any failure we fall back to a fresh purchase rather than blocking the flow.
 */
private fun resolveSubscriptionUpdate(
    action: PLYPresentationAction.Purchase,
    newSku: String,
    onResolved: (BillingFlowParams.SubscriptionUpdateParams?) -> Unit
) {
    Purchasely.userSubscriptions(
        onSuccess = onSuccess@{ subscriptions ->
            val previous = subscriptions.firstOrNull { sub ->
                sub.data.storeType == StoreType.GOOGLE_PLAY_STORE
                        && sub.plan.getProductId() != newSku
                        && sub.product.plans.any { it.vendorId == action.plan?.vendorId }
            }
            val oldPurchaseToken = previous?.data?.purchaseToken
            if (previous == null || oldPurchaseToken.isNullOrEmpty()) {
                onResolved(null)
                return@onSuccess
            }

            val newLevel = action.plan?.level
            val oldLevel = previous.plan.level
            val replacementMode = when {
                newLevel == null || oldLevel == null -> ReplacementMode.WITHOUT_PRORATION
                newLevel > oldLevel -> ReplacementMode.WITH_TIME_PRORATION // upgrade
                newLevel < oldLevel -> ReplacementMode.DEFERRED            // downgrade
                else -> ReplacementMode.WITHOUT_PRORATION                  // same level, different SKU
            }
            PLYLogger.d(
                "Replacing ${previous.plan.getProductId()} (level $oldLevel) with $newSku " +
                        "(level $newLevel) using replacement mode $replacementMode"
            )

            onResolved(
                BillingFlowParams.SubscriptionUpdateParams.newBuilder()
                    .setOldPurchaseToken(oldPurchaseToken)
                    .setSubscriptionReplacementMode(replacementMode)
                    .build()
            )
        },
        onError = { error ->
            PLYLogger.e("Could not load subscriptions for upgrade/downgrade; doing a fresh purchase: ${error.message}")
            onResolved(null)
        }
    )
}

/**
 * Builds the [BillingFlowParams] for [productDetails] (carrying the offer token, obfuscated
 * account/profile ids and an optional [subscriptionUpdate]) and launches the Google billing flow.
 */
private fun BillingClient.launchPurchaseFlow(
    activity: Activity,
    action: PLYPresentationAction.Purchase,
    productDetails: ProductDetails,
    subscriptionUpdate: BillingFlowParams.SubscriptionUpdateParams?,
    userId: String?
) {
    val productDetailsParamsList = listOf(
        BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(productDetails)
            .apply {
                //should be null for OTP only
                action.subscriptionOffer?.offerToken?.let { setOfferToken(it) }
            }
            .build()
    )

    val billingFlowParams = BillingFlowParams.newBuilder()
        .setProductDetailsParamsList(productDetailsParamsList)
        .setObfuscatedAccountId(Purchasely.anonymousUserId.sha256())

    if (!userId.isNullOrEmpty()) {
        billingFlowParams.setObfuscatedProfileId(userId.sha256())
    }

    subscriptionUpdate?.let { billingFlowParams.setSubscriptionUpdateParams(it) }

    launchBillingFlow(activity, billingFlowParams.build())
}

/**
 * Confirms a successful purchase with a dialog and keeps the paywall open until the user taps the
 * button — [onConfirmed] (which closes the paywall) runs only on that click. The dialog is shown
 * on the activity hosting the paywall so it overlays it, and is not cancelable so the user must
 * use the button. If the activity is already gone we just run [onConfirmed].
 */
private fun showPurchaseSuccessDialog(activity: Activity, onConfirmed: () -> Unit) {
    activity.runOnUiThread {
        if (activity.isFinishing || activity.isDestroyed) {
            onConfirmed()
            return@runOnUiThread
        }
        AlertDialog.Builder(activity)
            .setTitle("Purchase successful")
            .setMessage("Your subscription is now active. Enjoy!")
            .setCancelable(false)
            .setPositiveButton("Continue") { dialog, _ ->
                dialog.dismiss()
                onConfirmed()
            }
            .show()
    }
}

/**
 * Acknowledges (or consumes) every successfully purchased item, then invokes [onComplete].
 *
 * Google revokes and refunds any purchase that is not acknowledged within three days. In
 * Observer mode the SDK does not acknowledge on the app's behalf, so the app must do it on the
 * BillingClient it owns. Consumable products are consumed (which also acknowledges them);
 * everything else (subscriptions and non-consumables) is acknowledged. Only purchases in the
 * PURCHASED state are processed — PENDING purchases must wait until they are completed.
 *
 * [onComplete] runs once every purchase has been processed, regardless of individual outcomes,
 * so the flow still finishes if an acknowledgement fails (a missed acknowledgement is recovered
 * later by `Purchasely.synchronize()`, which re-validates non-acknowledged purchases).
 *
 * https://developer.android.com/google/play/billing/integrate#process
 */
private fun BillingClient.acknowledgePurchases(
    purchases: List<Purchase>,
    isConsumable: Boolean,
    onComplete: () -> Unit
) {
    val pending = purchases.filter {
        it.purchaseState == Purchase.PurchaseState.PURCHASED && !it.isAcknowledged
    }
    if (pending.isEmpty()) {
        onComplete()
        return
    }

    val remaining = AtomicInteger(pending.size)
    fun onItemProcessed() {
        if (remaining.decrementAndGet() == 0) onComplete()
    }

    pending.forEach { purchase ->
        val productId = purchase.products.firstOrNull()
        if (isConsumable) {
            val params = ConsumeParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()
            consumeAsync(params) { result, _ ->
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    PLYLogger.d("Consumed purchase $productId")
                } else {
                    PLYLogger.e("Failed to consume purchase $productId: ${result.responseCode} ${result.debugMessage}")
                }
                onItemProcessed()
            }
        } else {
            val params = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()
            acknowledgePurchase(params) { result ->
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    PLYLogger.d("Acknowledged purchase $productId")
                } else {
                    PLYLogger.e("Failed to acknowledge purchase $productId: ${result.responseCode} ${result.debugMessage}")
                }
                onItemProcessed()
            }
        }
    }
}

private fun BillingClient.safeEndConnection() {
    runCatching { endConnection() }
        .onFailure { PLYLogger.e("Failed to end BillingClient connection: ${it.message}") }
}