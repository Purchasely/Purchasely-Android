package com.purchasely.sample.kotlin

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentActivity
import com.google.android.material.snackbar.Snackbar
import io.purchasely.ext.*
import io.purchasely.ext.presentation.*
import com.purchasely.sample.R
import kotlinx.android.synthetic.main.activity_feature_list.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

class FeatureListActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feature_list)

        //You can also display the presentation for a specific product or plan
        //Purchasely.productFragment("productId", "presentationId or null for default")
        //Purchasely.planFragment("planId", "presentationId or null for default")

        //You can display the fragment as usual
        /*supportFragmentManager.beginTransaction()
            .replace(R.id.inappFragment, paywallFragment, "InAppFragment")
            .commitAllowingStateLoss()*/

        //you can setup the paywall in an invisible container to load it until ready
        findViewById<FrameLayout>(R.id.paywall).visibility = View.INVISIBLE

        //v6: build a presentation with the PLYPresentation { } DSL, preload it, then build its view
        PLYPresentation {
            placementId("abtest")
            contentId("my_content_id which is optional")
            displayCloseButton(true) //true by default, you can set to false if you never want it displayed
            onCloseRequested {
                //TODO this is mandatory to handle with a View to remove it when user click on close button
                findViewById<FrameLayout>(R.id.paywall).removeAllViews()

                //this activity has nothing to display so close it
                supportFinishAfterTransition()
            }
        }.preload { loaded, error ->
            runOnUiThread {
                if (error != null || loaded == null) {
                    Log.e("Purchasely", "Unable to load paywall", error)
                    supportFinishAfterTransition()
                    return@runOnUiThread
                }

                val paywallView = loaded.buildView(this) { outcome ->
                    //called when paywall is closed
                    //Purchased and Restored are returned only if purchase was validated by Purchasely, Google and your backend (if webhook configured)
                    when (outcome.purchaseResult) {
                        PLYPurchaseResult.PURCHASED -> Log.d("Purchasely", "User purchased ${outcome.plan}, you can call your backend to refresh user information and grant his entitlements")
                        PLYPurchaseResult.RESTORED -> Log.d("Purchasely", "User restored ${outcome.plan}, you can call your backend to refresh user information and grant his entitlements")
                        PLYPurchaseResult.CANCELLED -> Log.d("Purchasely", "User closed paywall without purchasing")
                        null -> Log.d("Purchasely", "Paywall dismissed without a purchase action")
                    }
                } ?: return@runOnUiThread

                progressBar.isVisible = false
                findViewById<FrameLayout>(R.id.paywall).apply {
                    addView(paywallView)
                    visibility = View.VISIBLE
                }
            }
        }

        //Implement UI Listener to handle UI event that may appear to user (success and error dialog)
        /*Purchasely.uiListener = object: UIListener {
            override fun onAlert(alert: PLYAlertMessage) {
                when(alert) {
                    PLYAlertMessage.InAppSuccess -> displaySuccessDialog(alert)
                    PLYAlertMessage.InAppSuccessUnauthentified -> displaySuccessDialog(alert)
                    is PLYAlertMessage.InAppError -> displayErrorDialog(alert)
                    is PLYAlertMessage.InAppRestorationError -> displayErrorDialog(alert)
                }
            }

            override fun onFragment(fragment: Fragment, type: PLYUIFragmentType) {
                //display fragment coming from deeplink
            }
        }*/

        //You can use LiveData to be notified when a purchase is made
        Purchasely.livePurchase().observe(this) {
            Log.d("Purchasely", "User purchased product $it")
            Snackbar.make(window.decorView, "Purchased ${it?.vendorId}", Snackbar.LENGTH_SHORT).show()
        }

        /**
         * Before displaying purchase view, you can display your own content
         * and return a [PLYInterceptResult]:
         *   NOT_HANDLED -> let the SDK perform the action (old processAction(true))
         *   SUCCESS     -> you handled it, the SDK skips its default behavior (old processAction(false))
         *   FAILED      -> you tried but failed, breaking the action chain
         *
         * v6 replaces the monolithic setPaywallActionsInterceptor with granular,
         * type-safe per-action interceptors. The lambda is a suspend function, so you can
         * await your own UI (e.g. a confirmation dialog) before returning a result.
         */
        Purchasely.interceptAction<PLYPresentationAction.Purchase> { info, _ ->
            if (info.activity != this@FeatureListActivity) return@interceptAction PLYInterceptResult.NOT_HANDLED

            //display an alert dialog and wait for the user's choice
            if (confirmDialog("Do you agree with our terms and conditions ?", "I agree", "Cancel"))
                PLYInterceptResult.NOT_HANDLED // let the SDK continue with the purchase
            else
                PLYInterceptResult.SUCCESS     // you handled it -> the SDK skips the purchase
        }

        Purchasely.interceptAction<PLYPresentationAction.Close> { info, _ ->
            if (info.activity != this@FeatureListActivity) return@interceptAction PLYInterceptResult.NOT_HANDLED

            if (confirmDialog("Close paywall ?", "Close", "Cancel"))
                PLYInterceptResult.NOT_HANDLED // let the SDK close the paywall
            else
                PLYInterceptResult.SUCCESS     // keep the paywall open
        }

        Purchasely.interceptAction<PLYPresentationAction.Login> { _, _ ->
            /*
                Display your own view to log in the user
                DO NOT FORGET to call Purchasely.userLogin(userId) after successful login
             */
            PLYInterceptResult.NOT_HANDLED
        }

        /*supportFragmentManager.addOnBackStackChangedListener {
            if(supportFragmentManager.backStackEntryCount == 0) {
                supportFinishAfterTransition()
            }
        }*/
    }

    /**
     * Shows a confirmation dialog and suspends until the user makes a choice.
     * Returns true if the positive button was tapped, false otherwise.
     */
    private suspend fun confirmDialog(title: String, positive: String, negative: String): Boolean =
        withContext(Dispatchers.Main) {
            suspendCancellableCoroutine { continuation ->
                val dialog = AlertDialog.Builder(this@FeatureListActivity)
                    .setTitle(title)
                    .setPositiveButton(positive) { d, _ -> d.dismiss(); if (continuation.isActive) continuation.resume(true) }
                    .setNegativeButton(negative) { d, _ -> d.dismiss(); if (continuation.isActive) continuation.resume(false) }
                    .setOnCancelListener { if (continuation.isActive) continuation.resume(false) }
                    .create()
                continuation.invokeOnCancellation { dialog.dismiss() }
                dialog.show()
            }
        }

    /**
     * You can display your own view like a dialog to inform the user
     * PLYAlertMessage contains some predefined string key translated in multiples languages that you can use if you want to
     * As an example, the success title key is : ply_modal_alert_in_app_success_title
     * All keys are in a values-{language}.xml file like values.xml for english or values-fr.xml for french
     */
    private fun displaySuccessDialog(alert: PLYAlertMessage) {
        AlertDialog.Builder(this)
                .setTitle(getString(alert.getTitleKey()))
                .setMessage("Thank you for your purchase, enjoy our great content !!")
                .setPositiveButton(getString(alert.getButtonKey())) { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
                .show()
    }

    /**
     * For an error, PLYAlertMessage contains a dynamic content accessible with getContentMessage()
     * You can display directly the error to the user if you want to
     */
    private fun displayErrorDialog(alert: PLYAlertMessage) {
        AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage(alert.getContentMessage())
                .setPositiveButton("Ok") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
                .show()
    }

}

/*class LegalFragment(val callback: PLYProcessToPaymentHandler) : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_legal, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<Button>(R.id.buttonAgree).setOnClickListener {
            callback(true)
            parentFragmentManager.popBackStack()
        }

        view.findViewById<Button>(R.id.buttonCancel).setOnClickListener {
            callback(false)
            parentFragmentManager.popBackStack()
        }
    }

}*/