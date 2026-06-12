package com.purchasely.sample.kotlin

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import io.purchasely.ext.*
import io.purchasely.ext.presentation.*
import com.purchasely.sample.R
import kotlinx.coroutines.launch

class PresentationAsyncActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_presentations_async)

        lifecycleScope.launch {
            //v6: build the presentation with the DSL and preload() it to get the Loaded instance
            val presentation = try {
                PLYPresentation {
                    placementId("ONBOARDING")
                    onCloseRequested { supportFinishAfterTransition() }
                }.preload()
            } catch (e: Exception) {
                Log.e("Purchasely", "Error fetching presentation", e)
                supportFinishAfterTransition()
                return@launch
            }

            when(presentation.type) {
                PLYPresentationType.NORMAL,
                PLYPresentationType.FALLBACK -> {
                    val paywallView = presentation.buildView(this@PresentationAsyncActivity) ?: return@launch
                    findViewById<FrameLayout>(R.id.paywallFrame).apply {
                        addView(paywallView)
                        isVisible = true
                    }
                }
                PLYPresentationType.DEACTIVATED -> supportFinishAfterTransition()
                PLYPresentationType.CLIENT -> startActivity(Intent(applicationContext, ClientActivity::class.java))
            }
        }
    }

}