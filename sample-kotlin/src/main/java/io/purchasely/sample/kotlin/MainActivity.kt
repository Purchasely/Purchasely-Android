package io.purchasely.sample.kotlin

import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import io.purchasely.ext.*
import io.purchasely.models.PLYPlan
import io.purchasely.sample.kotlin.FeatureListActivity
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val adapter by lazy { Adapter() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        recyclerView.layoutManager = LinearLayoutManager(applicationContext)
        recyclerView.adapter = adapter

        //TODO set your api key
        Purchasely.start(applicationContext, /*YOUR_API_KEY*/ "", eventListener = eventListener)

        buttonDisplayFeatureList.setOnClickListener { startActivity(Intent(applicationContext, FeatureListActivity::class.java)) }

        //set your user id to bind the purchase or to restore it
        //Purchasely.userId = "My user id"


        //Implement UI Listener to handle UI event that may appear to user (success and error dialog)
        Purchasely.uiListener = object: UIListener {
            override fun onAlert(alert: PLYAlertMessage) {
                when(alert) {
                    PLYAlertMessage.InAppSuccess -> displaySuccessDialog(alert)
                    PLYAlertMessage.InAppSuccessUnauthentified -> displaySuccessDialog(alert)
                    is PLYAlertMessage.InAppError -> displayErrorDialog(alert)
                }
            }
        }


        //Use LiveData to be notified when a purchase is made
        Purchasely.livePurchase().observe(this, Observer {
            Log.d("Purchasely", "User purchased $it")
            Snackbar.make(recyclerView, "Purchased ${it?.vendorId}", Snackbar.LENGTH_SHORT).show()
        })
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

    private val eventListener = object: EventListener {
        override fun onEvent(event: PLYEvent) {
            when(event) {
                PLYEvent.InAppStarted -> getProducts()
                PLYEvent.LoginTapped -> Toast.makeText(applicationContext, "User asked to login", Toast.LENGTH_LONG).show()
                PLYEvent.InAppPurchasing -> Toast.makeText(applicationContext, "Running...", Toast.LENGTH_SHORT).show()
                PLYEvent.InAppPurchased -> Toast.makeText(applicationContext, "Success !", Toast.LENGTH_SHORT).show()
                is PLYEvent.ReceiptFailed -> Toast.makeText(applicationContext, "Receipt failed : ${event.error.message}", Toast.LENGTH_LONG).show()
                PLYEvent.ReceiptValidated -> Toast.makeText(applicationContext, "Receipt validated", Toast.LENGTH_SHORT).show()
                PLYEvent.RestoreStarted -> Toast.makeText(applicationContext, "Click on restore", Toast.LENGTH_SHORT).show()
                is PLYEvent.InAppPurchaseFailed -> Toast.makeText(applicationContext, "In app failed : ${event.error?.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun getProducts() {
        Purchasely.getProducts(
                onSuccess = { products ->
                    adapter.list.addAll(products.flatMap { it.plans })
                    adapter.notifyDataSetChanged()
                },
                onError = {
                    Toast.makeText(applicationContext, "Error ${it.message}", Toast.LENGTH_SHORT).show()
                }
        )
    }

}

class Adapter(val list: MutableList<PLYPlan> = mutableListOf()) : RecyclerView.Adapter<Holder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(TextView(parent.context).apply {
            setPaddingRelative(60, 30, 60, 0)
        })
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(list[position])
    }

}

class Holder(override val containerView: TextView) : RecyclerView.ViewHolder(containerView), LayoutContainer {

    fun bind(plan: PLYPlan) {
        containerView.text = buildString {
            append(plan.storeInformation?.store_product_id)
            append("\n")
            append(String.format("Full Price: %s", plan.localizedFullPrice()))
            append("\n")
            append(String.format("Price: %s", plan.localizedPrice()))
            append("\n")
            append(String.format("Period: %s", plan.localizedPeriod()))
            append("\n")
            append(String.format("Full introductory price: %s", plan.localizedFullIntroductoryPrice()))
            append("\n")
            append(String.format("Introductory Price: %s", plan.localizedIntroductoryPrice()))
            append("\n")
            append(String.format("Introductory Period: %s", plan.localizedIntroductoryPeriod()))
            append("\n")
            append(String.format("Introductory Duration: %s", plan.localizedIntroductoryDuration()))
            append("\n")
            append(String.format("Trial Period: %s", plan.localizedTrialDuration()))
            append("\n")
            append(String.format("Numeric Price: %s", plan.getPrice()))
            append("\n")
            append(String.format("Price Currency Code: %s", plan.getPriceCurrencyCode()))
            append("\n")
            append(String.format("Daily Equivalent: %s", plan.dailyEquivalentPrice()))
            append("\n")
            append(String.format("Weekly Equivalent: %s", plan.weeklyEquivalentPrice()))
            append("\n")
            append(String.format("Monthly Equivalent: %s", plan.monthlyEquivalentPrice()))
            append("\n")
            append(String.format("Yearly Equivalent: %s", plan.yearlyEquivalentPrice()))
        }
    }

}
