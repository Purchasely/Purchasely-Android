package com.purchasely.sample.kotlin

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import io.purchasely.ext.*
import io.purchasely.models.PLYPlan
import com.purchasely.sample.R
import kotlinx.coroutines.launch

class PresentationAsyncActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_presentations_async)

        lifecycleScope.launch {
            val presentation = try {
                Purchasely.fetchPresentation(
                    this@PresentationAsyncActivity,
                    PLYPresentationViewProperties(
                        placementId = "ONBOARDING",
                        onLoaded = { isLoaded ->
                            if(isLoaded) findViewById<FrameLayout>(R.id.paywallFrame).isVisible = true
                            else supportFinishAfterTransition()
                        }
                    )) { result: PLYProductViewResult, plan: PLYPlan? ->
                    //called when paywall is closed
                    //Purchased and Restored are returned only if purchase was validated by Purchasely, Google and your backend (if webhook configured)
                    when(result) {
                        PLYProductViewResult.PURCHASED -> Log.d("Purchasely", "User purchased $plan, you can call your backend to refresh user information and grant his entitlements")
                        PLYProductViewResult.RESTORED -> Log.d("Purchasely", "User restored $plan, you can call your backend to refresh user information and grant his entitlements")
                        PLYProductViewResult.CANCELLED -> Log.d("Purchasely", "User closed paywall without purchasing")
                    }
                    supportFinishAfterTransition()
                }
            } catch (e: Exception) {
                Log.e("Purchasely", "Error fetching presentation", e)
                null
            } ?: return@launch

            when(presentation.type) {
                PLYPresentationType.NORMAL,
                PLYPresentationType.FALLBACK -> {
                    if(presentation.view == null) Log.d("Purchasely", "Error with view")
                    findViewById<FrameLayout>(R.id.paywallFrame).addView(presentation.view)
                }
                PLYPresentationType.DEACTIVATED -> supportFinishAfterTransition()
                PLYPresentationType.CLIENT -> startActivity(Intent(applicationContext, ClientActivity::class.java))
            }
        }
    }

}