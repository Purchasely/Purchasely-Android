package io.purchasely.sample.kotlin

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.purchasely.ext.*
import io.purchasely.google.GoogleStore
import io.purchasely.models.PLYPlan
import io.purchasely.sample.R
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
                .apiKey("afa96c76-1d8e-4e3c-a48f-204a3cd93a15")
                .eventListener(eventListener)
                .logLevel(LogLevel.DEBUG)
                .isReadyToPurchase(true)
                .runningMode(PLYRunningMode.Full)
                .stores(listOf(GoogleStore()))
                .build()

        //Purchasely.playerView = "io.purchasely.player.PLYPlayerView"

        buttonDisplayFeatureList.setOnClickListener { startActivity(Intent(applicationContext, FeatureListActivity::class.java)) }
        buttonSubscriptions.setOnClickListener { startActivity(Intent(applicationContext, SubscriptionsActivity::class.java)) }

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

        Purchasely.userLogin("DEMO_USER") { refresh ->
            if (refresh) {
                //Purchases were transferred to the user, you may need to refresh your user information
            }
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
                PLYEvent.AppStarted -> getProducts()
                PLYEvent.AppUpdated -> Log.d("Event", "App Updated")
                PLYEvent.AppInstalled -> Log.d("Event", "App Installed")
                PLYEvent.LoginTapped -> Toast.makeText(applicationContext, "User asked to login", Toast.LENGTH_LONG).show()
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