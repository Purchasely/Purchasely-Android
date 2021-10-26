package io.purchasely.sample.kotlin

import android.os.Bundle
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentActivity
import io.purchasely.ext.Purchasely
import io.purchasely.sample.R
import kotlinx.android.synthetic.main.activity_feature_list.*

class SubscriptionsActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feature_list)

        supportFragmentManager.beginTransaction()
            .addToBackStack(null)
            .replace(
                R.id.inappFragment,
                Purchasely.subscriptionsFragment(),
                "SubscriptionsFragment"
            )
            .commitAllowingStateLoss()

        progressBar.isVisible = false


        supportFragmentManager.addOnBackStackChangedListener {
            if(supportFragmentManager.backStackEntryCount == 0) {
                supportFinishAfterTransition()
            }
        }
    }

}