package io.purchasely.sample.kotlin

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import com.google.android.material.snackbar.Snackbar
import io.purchasely.ext.PLYAlertMessage
import io.purchasely.ext.Purchasely
import io.purchasely.ext.UIListener
import kotlinx.android.synthetic.main.activity_feature_list.*

class FeatureListActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feature_list)

        //TODO set the product id you want to display
        val fragment = Purchasely.productFragment(
                productId = "YOUR_PRODUCT_ID",
                presentationId = "default", ) { result, plan ->
            Snackbar.make(
                        window.decorView,
                        "Purchased result is $result with plan ${plan?.vendorId}",
                        Snackbar.LENGTH_LONG
                    )
                    .show()
        }

        supportFragmentManager.beginTransaction()
                .addToBackStack(null)
                .replace(R.id.inappFragment, fragment, "InAppFragment")
                .commitAllowingStateLoss()

        progressBar.isVisible = false

        //Implement UI Listener to handle UI event that may appear to user (success and error dialog)
        Purchasely.uiListener = object: UIListener {
            override fun onAlert(alert: PLYAlertMessage) {
                when(alert) {
                    PLYAlertMessage.InAppSuccess -> displaySuccessDialog(alert)
                    PLYAlertMessage.InAppSuccessUnauthentified -> displaySuccessDialog(alert)
                    is PLYAlertMessage.InAppError -> displayErrorDialog(alert)
                    is PLYAlertMessage.InAppRestorationError -> displayErrorDialog(alert)
                }
            }
        }

        //Use LiveData to be notified when a purchase is made
        Purchasely.livePurchase().observe(this, Observer {
            Log.d("Purchasely", "User purchased $it")
            Snackbar.make(window.decorView, "Purchased ${it?.vendorId}", Snackbar.LENGTH_SHORT).show()
        })

        supportFragmentManager.addOnBackStackChangedListener {
            if(supportFragmentManager.backStackEntryCount == 0) {
                supportFinishAfterTransition()
            }
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
                .setTitle("Oups something not right happened")
                .setMessage(alert.getContentMessage())
                .setPositiveButton("Damn it !") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
                .show()
    }

}