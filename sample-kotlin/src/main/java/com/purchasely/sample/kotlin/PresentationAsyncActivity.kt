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
            placementId = "ONBOARDING",
            onLoaded = { isLoaded ->
                if(isLoaded) findViewById<FrameLayout>(R.id.paywallFrame).isVisible = true
                else supportFinishAfterTransition()
            },
            onClose = { supportFinishAfterTransition() }
        )

        lifecycleScope.launch {
            val presentation = try {
                Purchasely.fetchPresentation(properties = properties)
            } catch (e: Exception) {
                Log.e("Purchasely", "Error fetching presentation", e)
                null
            } ?: return@launch

            when(presentation.type) {
                PLYPresentationType.NORMAL,
                PLYPresentationType.FALLBACK -> {
                    val paywallView = presentation.buildView(this@PresentationAsyncActivity) ?: return@launch
                    findViewById<FrameLayout>(R.id.paywallFrame).addView(paywallView)
                }
                PLYPresentationType.DEACTIVATED -> supportFinishAfterTransition()
                PLYPresentationType.CLIENT -> startActivity(Intent(applicationContext, ClientActivity::class.java))
            }
        }
    }

}