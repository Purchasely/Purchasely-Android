package io.purchasely.sample.kotlin

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.android.billingclient.api.Purchase
import com.google.android.material.snackbar.Snackbar
import io.purchasely.ext.PLYAlertMessage
import io.purchasely.ext.PLYPaywallActionHandler
import io.purchasely.ext.PLYPresentationAction
import io.purchasely.ext.Purchasely
import io.purchasely.sample.R
import kotlinx.android.synthetic.main.activity_feature_list.*

class FeatureListActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feature_list)

        val fragment = Purchasely.presentationFragment("CAROUSEL", null, null) { result, plan ->
            Log.d("PurchaselyDemo", "Purchased result is $result with plan ${plan?.vendorId}")
        } ?: return

        //You may want to display a presentation with a placement for different groups of users
        //Purchasely.presentationFragmentForPlacement("my_placement_id", "my content id", null, null)

        //You can also display the presentation for a specific product or plan
        //Purchasely.productFragment("productId", "presentationId or null for default")
        //Purchasely.planFragment("planId", "presentationId or null for default")

        supportFragmentManager.beginTransaction()
                .addToBackStack(null)
                .replace(R.id.inappFragment, fragment, "InAppFragment")
                .commitAllowingStateLoss()

        progressBar.isVisible = false

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

        Purchasely.setPaywallActionsInterceptor(paywallActionInterceptor)

        supportFragmentManager.addOnBackStackChangedListener {
            if(supportFragmentManager.backStackEntryCount == 0) {
                supportFinishAfterTransition()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Purchasely.uiListener = null
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

    /**
     * Before displaying purchase view, you can display you own content
     * and send back a boolean to result callback
     * true if you allow the user to continue with his purchase
     * false otherwise
     */
    private val paywallActionInterceptor: PLYPaywallActionHandler = {
            info, action, parameters, processAction ->

        when(action) {
            PLYPresentationAction.PURCHASE -> {
                //display an alert dialog
                AlertDialog.Builder(this)
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
            else -> processAction(true)
        }
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