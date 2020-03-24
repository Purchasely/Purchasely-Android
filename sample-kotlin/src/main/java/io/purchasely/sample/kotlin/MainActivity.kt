package io.purchasely.sample.kotlin

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.purchasely.models.PLYPlan
import io.purchasely.public.EventListener
import io.purchasely.public.PLYEvent
import io.purchasely.public.PLYUI
import io.purchasely.public.Purchasely
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

        /*
        Implement UI Listener to handle UI event that may appear to user (success and error dialog)
        Purchasely.uiListener = object: UIListener {
            override fun onAlert(alert: PLYUI) {
                when(alert) {
                    PLYUI.InAppSuccess -> //TODO do something
                    PLYUI.InAppSuccessUnauthentified -> //TODO do something
                    PLYUI.InAppError -> //TODO do something
                }
            }
        }
        */
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
            append(String.format("Trial Period: %s", plan.localizedTrialPeriod()))
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
