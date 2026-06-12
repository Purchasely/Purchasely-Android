package com.purchasely.sample.kotlin

import android.os.Bundle
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentActivity
import io.purchasely.ext.Purchasely
import com.purchasely.sample.R
import kotlinx.android.synthetic.main.activity_feature_list.*

class SubscriptionsActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feature_list)

        // v6: the built-in subscriptions/cancellation UI has been removed from the SDK.
        // Build your own UI from the data APIs that remain available:
        //   Purchasely.userSubscriptions { ... }          // active subscriptions
        //   Purchasely.userSubscriptionsHistory { ... }    // expired subscriptions
        Purchasely.userSubscriptions(
            onSuccess = { subscriptions ->
                progressBar.isVisible = false

                val list = LinearLayout(this@SubscriptionsActivity).apply {
                    orientation = LinearLayout.VERTICAL
                    setPadding(60, 60, 60, 60)
                }

                if (subscriptions.isEmpty()) {
                    list.addView(TextView(this@SubscriptionsActivity).apply { text = "No active subscription" })
                } else {
                    subscriptions.forEach { subscription ->
                        list.addView(TextView(this@SubscriptionsActivity).apply {
                            setPaddingRelative(0, 0, 0, 30)
                            text = "${subscription.plan.name ?: subscription.plan.vendorId}\n" +
                                    "Product: ${subscription.product.name}"
                        })
                    }
                }

                findViewById<FrameLayout>(R.id.paywall).apply {
                    removeAllViews()
                    addView(list)
                }
            },
            onError = { error ->
                progressBar.isVisible = false
                Toast.makeText(this@SubscriptionsActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        )
    }

}
