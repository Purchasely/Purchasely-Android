package com.purchasely.sample.kotlin

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.Snackbar
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import io.purchasely.ext.*
import io.purchasely.models.PLYPlan
import com.purchasely.sample.R
import kotlinx.android.extensions.LayoutContainer
import kotlinx.coroutines.launch

class ClientActivity : AppCompatActivity() {

    private var presentation: PLYPresentation? = null

    private val adapter by lazy {
        Adapter { plan, observer ->
            //Purchase with Purchasely or your own service
            Purchasely.purchase(this, plan, null, "content_id_client",
                onSuccess = {
                    Toast.makeText(applicationContext, "Bought ${it?.name}", Toast.LENGTH_SHORT).show()

                    //If you do not use Purchasely to purchase, call synchronize() method
                    Purchasely.synchronize()

                    supportFinishAfterTransition()
                },
                onError = {
                    PLYLogger.log("Error buying ${plan.name}")
                    Toast.makeText(applicationContext, "Error $it", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_client_paywall)

        findViewById<RecyclerView>(R.id.recyclerView2).layoutManager = LinearLayoutManager(applicationContext)
        findViewById<RecyclerView>(R.id.recyclerView2).adapter = adapter

        /* You can also use coroutines
        lifecycleScope.launch {
            val presentation = Purchasely.fetchPresentation(
                this@ClientActivity,
                PLYPresentationViewProperties(placementId = "client_placement")) { result, plan ->

            }
        }*/

        Purchasely.fetchPresentationForPlacement( "client") { presentation, error ->
            this.presentation = presentation
            if(presentation?.type == PLYPresentationType.CLIENT) {
                Purchasely.clientPresentationDisplayed(presentation)

                lifecycleScope.launch {
                    adapter.list.addAll(presentation.plans.mapNotNull { Purchasely.plan(it.planVendorId!!) })
                    adapter.notifyDataSetChanged()
                }
            } else {
                if(presentation?.type == PLYPresentationType.DEACTIVATED) {
                    Snackbar.make(findViewById<RecyclerView>(R.id.recyclerView2), "Paywall is deactivated", Snackbar.LENGTH_SHORT).show()
                } else {
                    startActivity(Intent(applicationContext, PresentationAsyncActivity::class.java))
                    supportFinishAfterTransition()
                }
            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()

        presentation?.let { Purchasely.clientPresentationClosed(it) }
    }

    internal class Adapter(val list: MutableList<PLYPlan> = mutableListOf(), val click : (PLYPlan, Boolean) -> Unit) : RecyclerView.Adapter<Holder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_client_paywall_button, parent, false)
            return Holder(view as ViewGroup)
        }

        override fun getItemCount() = list.size

        override fun onBindViewHolder(holder: Holder, position: Int) {
            holder.bind(list[position])

            holder.containerView.findViewById<Button>(R.id.button).setOnClickListener {
                val observer = holder.bindingAdapterPosition % 2 == 1
                click(list[holder.bindingAdapterPosition], observer)
            }
        }

    }

    internal class Holder(override val containerView: ViewGroup) : RecyclerView.ViewHolder(containerView),
        LayoutContainer {

        @SuppressLint("SetTextI18n")
        fun bind(plan: PLYPlan) {
            containerView.findViewById<TextView>(R.id.text).text =
                "ID: ${plan.vendorId} \n" +
                "NAME: ${plan.name} \n" +
                "PERIOD: ${plan.period()?.toLocale()} \n" +
                "TYPE: ${plan.type} \n" +
                "PRICE: ${plan.localizedFullPrice()} \n" +
                "INTRO_PRICE: ${plan.localizedFullIntroductoryPrice()} \n" +
                "FREE_TRIAL: ${plan.hasFreeTrial()} \n"
        }

    }

}