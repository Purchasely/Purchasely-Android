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
import com.purchasely.sample.R
import kotlinx.android.synthetic.main.activity_feature_list.*

class FeatureListActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feature_list)

        //You can use a fragment (deprecated) or use a view
        val paywallFragment = Purchasely.presentationFragmentForPlacement("ACCOUNT", null, null) { result, plan ->
            Log.d("PurchaselyDemo", "Purchased result is $result with plan ${plan?.vendorId}")
        } ?: return

        //You can also display the presentation for a specific product or plan
        //Purchasely.productFragment("productId", "presentationId or null for default")
        //Purchasely.planFragment("planId", "presentationId or null for default")

        //You can display the fragment as usual
        /*supportFragmentManager.beginTransaction()
            .replace(R.id.inappFragment, paywallFragment, "InAppFragment")
            .commitAllowingStateLoss()*/

        //you can setup the paywall in an invisible container to load it until ready
        findViewById<FrameLayout>(R.id.paywall).visibility = View.INVISIBLE

        val paywallView = Purchasely.presentationView(
            this,
            PLYPresentationViewProperties(
                placementId = "ACCOUNT",
                contentId = "my_content_id which is optional",
                displayCloseButton = true, //true by default, you can set to false if you never want it displayed
                onLoaded = { isLoaded ->
                    //paywall is ready to be shown
                    if(isLoaded) {
                        progressBar.isVisible = false
                        findViewById<FrameLayout>(R.id.paywall).visibility = View.VISIBLE
                    }
                },
                onClose = {
                    //TODO this is mandatory to handle with a View to remove it when user click on close button
                    findViewById<FrameLayout>(R.id.paywall).removeAllViews()

                    //this activity has nothing to display so close it
                    supportFinishAfterTransition()
                }
            )
        ) { result, plan ->
            //called when paywall is closed
            //Purchased and Restored are returned only if purchase was validated by Purchasely, Google and your backend (if webhook configured)
            when(result) {
                PLYProductViewResult.PURCHASED -> Log.d("Purchasely", "User purchased $plan, you can call your backend to refresh user information and grant his entitlements")
                PLYProductViewResult.RESTORED -> Log.d("Purchasely", "User restored $plan, you can call your backend to refresh user information and grant his entitlements")
                PLYProductViewResult.CANCELLED -> Log.d("Purchasely", "User closed paywall without purchasing")
            }
        }

        findViewById<FrameLayout>(R.id.paywall).addView(paywallView)

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
         * Before displaying purchase view, you can display you own content
         * and send back a boolean to result callback
         * true if you allow the user to continue with his purchase
         * false otherwise
         */
        Purchasely.setPaywallActionsInterceptor { info, action, parameters, processAction ->
            when(action) {
                PLYPresentationAction.PURCHASE -> {
                    //display an alert dialog
                    AlertDialog.Builder(this@FeatureListActivity)
                        .setTitle("Do you agree with our terms and conditions ?")
                        .setPositiveButton("I agree") { dialog, _ ->
                            processAction(true)
                            dialog.dismiss()
                        }
                        .setNegativeButton("Cancel") { dialog, _ ->
                            processAction(false)
                            dialog.dismiss()
                        }
                        .create()
                        .show()

                    //or display a fragment
                    /*
                    supportFragmentManager.beginTransaction()
                            .addToBackStack(null)
                            .add(R.id.inappFragment, LegalFragment(result), "InAppFragment")
                            .commitAllowingStateLoss()
                     */
                }
                PLYPresentationAction.CLOSE -> {
                    AlertDialog.Builder(this@FeatureListActivity)
                        .setTitle("Close paywall ?")
                        .setPositiveButton("Close") { dialog, _ ->
                            processAction(true)
                            dialog.dismiss()
                        }
                        .setNegativeButton("Cancel") { dialog, _ ->
                            processAction(false)
                            dialog.dismiss()
                        }
                        .create()
                        .show()
                }
                PLYPresentationAction.LOGIN -> {
                    /*
                        Display your own view to log in the user
                        DO NOT FORGET to call Purchasely.userLogin(userId) after successful login
                     */

                    //Once done call with true if user logged in or false if he did not
                    processAction(true)
                }
                else -> processAction(true)
            }
        }

        /*supportFragmentManager.addOnBackStackChangedListener {
            if(supportFragmentManager.backStackEntryCount == 0) {
                supportFinishAfterTransition()
            }
        }*/
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