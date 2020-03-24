package io.purchasely.sample.kotlin

import android.os.Bundle
import android.util.Log
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentActivity
import com.google.android.material.snackbar.Snackbar
import io.purchasely.public.Purchasely
import io.purchasely.sample.kotlin.R
import kotlinx.android.synthetic.main.activity_feature_list.*

class FeatureListActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feature_list)

        //TODO set the product id you want to display
        Purchasely.displayProduct(/*Your Product id*/ "",
            success = { fragment ->
                supportFragmentManager.beginTransaction()
                    .addToBackStack(null)
                    .replace(R.id.inappFragment, fragment, "InAppFragment")
                    .commitAllowingStateLoss()

                progressBar.isVisible = false
            },
            failure = { error ->
                Log.e("SinglePlan", "Error", error)
                Snackbar.make(window.decorView, error.message ?: "error", Snackbar.LENGTH_SHORT).show()
            }
        )

        supportFragmentManager.addOnBackStackChangedListener {
            if(supportFragmentManager.backStackEntryCount == 0) {
                supportFinishAfterTransition()
            }
        }
    }

}