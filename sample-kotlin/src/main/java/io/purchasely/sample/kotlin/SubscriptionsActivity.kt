package io.purchasely.sample.kotlin

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import com.google.android.material.snackbar.Snackbar
import io.purchasely.ext.PLYAlertMessage
import io.purchasely.ext.PLYUIFragmentType
import io.purchasely.ext.Purchasely
import io.purchasely.ext.UIListener
import io.purchasely.sample.R
import io.purchasely.views.subscriptions.PLYSubscriptionsFragment
import kotlinx.android.synthetic.main.activity_feature_list.*

class SubscriptionsActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feature_list)

        supportFragmentManager.beginTransaction()
                .addToBackStack(null)
                .replace(R.id.inappFragment, PLYSubscriptionsFragment(), "SubscriptionsFragment")
                .commitAllowingStateLoss()

        progressBar.isVisible = false


        supportFragmentManager.addOnBackStackChangedListener {
            if(supportFragmentManager.backStackEntryCount == 0) {
                supportFinishAfterTransition()
            }
        }
    }

}