package com.purchasely.samplev2.presentation.screen.subscriptions

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.purchasely.samplev2.R
import com.purchasely.samplev2.presentation.util.Constants.Companion.TAG
import io.purchasely.ext.Purchasely

class SubscriptionsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_subscriptions)

        val subscriptionsFragment = Purchasely.subscriptionsFragment() ?: return

        supportFragmentManager.beginTransaction()
            .addToBackStack(null)
            .replace(R.id.container, subscriptionsFragment, "SubscriptionsFragment")
            .commitAllowingStateLoss()

        supportFragmentManager.addOnBackStackChangedListener {
            if(supportFragmentManager.backStackEntryCount == 0) {
                supportFinishAfterTransition()
            }
        }

        Purchasely.livePurchase().observe(this) {
            Log.d(TAG, "User purchase $it")
            Toast.makeText(this,"Purchased ${it?.vendorId}", Toast.LENGTH_LONG).show()
        }
    }

}