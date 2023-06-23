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

        val properties = PLYPresentationViewProperties(
            onLoaded = { isLoaded ->
                if(isLoaded) findViewById<FrameLayout>(R.id.paywallFrame).isVisible = true
                else supportFinishAfterTransition()
            })

        lifecycleScope.launch {
            val presentation = try {
                Purchasely.fetchPresentationForPlacement(this@PresentationAsyncActivity, placementId = "ONBOARDING") {
                    presentation, error ->
                }
            } catch (e: Exception) {
                Log.e("Purchasely", "Error fetching presentation", e)
                null
            } ?: return@launch

            /*when(presentation.type) {
                PLYPresentationType.NORMAL,
                PLYPresentationType.FALLBACK -> {
                    if(presentation.view == null) Log.d("Purchasely", "Error with view")
                    val paywallView = presentation.view ?: return@launch
                    findViewById<FrameLayout>(R.id.paywallFrame).addView(paywallView)
                }
                PLYPresentationType.DEACTIVATED -> supportFinishAfterTransition()
                PLYPresentationType.CLIENT -> startActivity(Intent(applicationContext, ClientActivity::class.java))
            }*/
        }
    }

}