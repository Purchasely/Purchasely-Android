package com.purchasely.sample.kotlin

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.purchasely.ext.*
import io.purchasely.google.GoogleStore
import io.purchasely.models.PLYPlan
import com.purchasely.sample.R
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.launch

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
                .apiKey("fcb39be4-2ba4-4db7-bde3-2a5a1e20745d")
                .eventListener(eventListener)
                .userId(null) //you can set an user id if you have one
                .logLevel(LogLevel.DEBUG) //set to ERROR in production
                .isReadyToPurchase(true) //to open paywalls from deeplinks
                .runningMode(PLYRunningMode.Full) //set to PLYRunningMode.PaywallObserver if you keep your transaction stack
                .stores(listOf(GoogleStore()))
                .build()
                .start { isConfigured, error ->
                    if(isConfigured) Log.d("Purchasely", "You can display paywalls and make purchases")
                    if(error != null) Log.e("Purchasely", "Configuration error", error)
                }

        //make sure no user is saved if none is logged in
        Purchasely.userLogout()

        //Purchasely.playerView = "io.purchasely.player.PLYPlayerView"

        buttonDisplayFeatureList.setOnClickListener { startActivity(Intent(applicationContext, FeatureListActivity::class.java)) }
        buttonDisplayCompose.setOnClickListener { startActivity(Intent(applicationContext, ComposeActivity::class.java)) }
        buttonSubscriptions.setOnClickListener { startActivity(Intent(applicationContext, SubscriptionsActivity::class.java)) }
        buttonDisplayAsync.setOnClickListener { startActivity(Intent(applicationContext, PresentationAsyncActivity::class.java)) }
        buttonDisplayClientPaywall.setOnClickListener { startActivity(Intent(applicationContext, ClientActivity::class.java)) }

        buttonRestore.setOnClickListener {
            Purchasely.restoreAllProducts(
                    success = { plan ->
                        Log.e("Sample", "Restored $plan")
                    },
                    error = { error ->
                        Log.e("Sample", "Restoration failed $error")

                    }
            )

        }

        /*
            If you need the purchase result when a paywall is open directly via a deeplink :
            Purchasely.setDefaultPresentationResultHandler  { result, plan ->
                Log.d("PurchaselyDemo", "Purchased result is $result with plan ${plan?.vendorId}")
            }
         */

    }

    private val eventListener = object: EventListener {
        override fun onEvent(event: PLYEvent) {
            when (event) {
                is PLYEvent.AppStarted -> getProducts()
                is PLYEvent.AppUpdated -> Log.d("Event", "App Updated")
                is PLYEvent.AppInstalled -> Log.d("Event", "App Installed")
                is PLYEvent.LoginTapped -> Toast.makeText(applicationContext, "User asked to login", Toast.LENGTH_LONG).show()
                else -> {}
            }
        }
    }

    private fun getProducts() {
        Purchasely.allProducts(
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
            append(String.format("Type: %s", plan.type?.name?.lowercase()))
            append("\n")
            append(String.format("Full Price: %s", plan.localizedFullPrice()))
            append("\n")
            append(String.format("Price: %s", plan.localizedPrice()))
            append("\n")
            append(String.format("Localized Period: %s", plan.localizedPeriod()))
            append("\n")
            append(String.format("Period: %s", plan.period()?.unit))
            append("\n")
            append(String.format("Duration: %s", plan.duration()))
            append("\n")
            append(
                String.format(
                    "Full introductory price: %s",
                    plan.localizedFullIntroductoryPrice()
                )
            )
            append("\n")
            append(String.format("Introductory Price: %s", plan.localizedIntroductoryPrice()))
            append("\n")
            append(
                String.format(
                    "Introductory Localized Period: %s",
                    plan.localizedIntroductoryPeriod()
                )
            )
            append("\n")
            append(
                String.format(
                    "Introductory Localized Duration: %s",
                    plan.localizedIntroductoryDuration()
                )
            )
            append("\n")
            append(String.format("Introductory Period: %s", plan.introductoryPeriod()?.unit))
            append("\n")
            append(String.format("Introductory Duration: %s", plan.introductoryDuration()))
            append("\n")
            append(String.format("Introductory Cycles: %s", plan.introductoryCycles()))
            append("\n")
            append(String.format("Trial Period: %s", plan.freeTrialPeriod()?.unit))
            append("\n")
            append(String.format("Trial Duration: %s", plan.localizedTrialDuration()))
            append("\n")
            append(String.format("Numeric Price: %s", plan.price()))
            append("\n")
            append(String.format("Price Currency: %s", plan.currencyCode()))
            append("\n")
            append(String.format("Price Currency Code: %s", plan.currencySymbol()))
            append("\n")
            append(String.format("Daily Equivalent: %s", plan.dailyEquivalentPrice()))
            append("\n")
            append(String.format("Weekly Equivalent: %s", plan.weeklyEquivalentPrice()))
            append("\n")
            append(String.format("Monthly Equivalent: %s", plan.monthlyEquivalentPrice()))
            append("\n")
            append(String.format("Yearly Equivalent: %s", plan.yearlyEquivalentPrice()))
            append("\n")
            append(String.format("Daily Duration: %s", plan.durationInDays()))
            append("\n")
            append(String.format("Weekly Duration: %s", plan.durationInWeeks()))
            append("\n")
            append(String.format("Monthly Duration: %s", plan.durationInMonths()))
            append("\n")
            append(String.format("Quaterly Duration: %s", plan.durationInQuarters()))
            append("\n")
            append(String.format("Yearly Duration: %s", plan.durationInYears()))
        }
    }

}