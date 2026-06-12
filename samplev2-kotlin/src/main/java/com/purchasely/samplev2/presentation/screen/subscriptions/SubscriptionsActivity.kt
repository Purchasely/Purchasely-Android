package com.purchasely.samplev2.presentation.screen.subscriptions

import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.purchasely.demo.R
import com.purchasely.samplev2.presentation.util.Constants.Companion.TAG
import io.purchasely.ext.Purchasely

class SubscriptionsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_subscriptions)

        // v6: the built-in subscriptions/cancellation UI has been removed from the SDK.
        // Build your own UI from the data APIs that remain available:
        //   Purchasely.userSubscriptions { ... }          // active subscriptions
        //   Purchasely.userSubscriptionsHistory { ... }    // expired subscriptions
        Purchasely.userSubscriptions(
            onSuccess = { subscriptions ->
                val list = LinearLayout(this@SubscriptionsActivity).apply {
                    orientation = LinearLayout.VERTICAL
                    setPadding(48, 48, 48, 48)
                }

                if (subscriptions.isEmpty()) {
                    list.addView(TextView(this@SubscriptionsActivity).apply { text = "No active subscription" })
                } else {
                    subscriptions.forEach { subscription ->
                        list.addView(TextView(this@SubscriptionsActivity).apply {
                            setPaddingRelative(0, 0, 0, 24)
                            text = "${subscription.plan.name ?: subscription.plan.vendorId}\n" +
                                    "Product: ${subscription.product.name}"
                        })
                    }
                }

                findViewById<ScrollView>(R.id.container).apply {
                    removeAllViews()
                    addView(list)
                }
            },
            onError = { error ->
                Log.e(TAG, "Unable to load subscriptions", error)
                Toast.makeText(this@SubscriptionsActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        )

        Purchasely.livePurchase().observe(this) {
            Log.d(TAG, "User purchase $it")
            Toast.makeText(this, "Purchased ${it?.vendorId}", Toast.LENGTH_LONG).show()
        }
    }

}
