package io.purchasely.sample.kotlin

import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.purchasely.ext.EventListener
import io.purchasely.ext.LogLevel
import io.purchasely.ext.PLYEvent
import io.purchasely.ext.Purchasely
import io.purchasely.google.GoogleStore
import io.purchasely.models.PLYPlan
import io.purchasely.sample.R
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

        Purchasely.Builder(applicationContext)
                //TODO set your api key
                .apiKey("afa96c76-1d8e-4e3c-a48f-204a3cd93a15")
                .eventListener(eventListener)
                .logLevel(LogLevel.DEBUG)
                .isReadyToPurchase(true)
                .stores(listOf(GoogleStore()))
                .build()
                .start()

        buttonDisplayFeatureList.setOnClickListener { startActivity(Intent(applicationContext, FeatureListActivity::class.java)) }
        buttonSubscriptions.setOnClickListener { startActivity(Intent(applicationContext, SubscriptionsActivity::class.java)) }

        getProducts()

        Purchasely.userLogin("DEMO_USER") { refresh ->
            if (refresh) {
                //Purchases were transferred to the user, you may need to refresh your user information
            }
        }

        Purchasely.setLoginTappedHandler { activity, isLoggedIn ->
            if (activity == null) return@setLoginTappedHandler

            //display your login view and send result back
            isLoggedIn(true) //true if user is logged in
        }

    }

    private val eventListener = object: EventListener {
        override fun onEvent(event: PLYEvent) {
            when(event) {
                PLYEvent.AppStarted -> getProducts()
                PLYEvent.LoginTapped -> Toast.makeText(applicationContext, "User asked to login", Toast.LENGTH_LONG).show()
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

    override fun onDestroy() {
        super.onDestroy()
        Purchasely.close()
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
        containerView.text = plan.toString()
        containerView.text = buildString {
            append(plan.store_product_id)
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
            append(String.format("Price Currency Symbol: %s", plan.getPriceCurrencySymbol()))
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