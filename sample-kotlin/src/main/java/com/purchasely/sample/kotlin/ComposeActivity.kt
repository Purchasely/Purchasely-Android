package com.purchasely.sample.kotlin

import android.os.Bundle
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import io.purchasely.ext.presentation.PLYPresentation
import io.purchasely.ext.presentation.preload
import com.purchasely.sample.kotlin.ui.theme.AndroidPublicTheme

class ComposeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AndroidPublicTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    Greeting("Android") {
                        this@ComposeActivity.finish()
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, close: (() -> Unit)? = null) {
    val displayPaywall = remember { mutableStateOf(true) }

    Column {
        Text(
            modifier = Modifier.padding(20.dp, 0.dp),
            text = "Hello Compose"
        )

        if(displayPaywall.value)
            paywall(displayPaywall)
        else
            close?.invoke()
    }
}

@Composable
fun paywall(displayPaywall: MutableState<Boolean>) {
    // v6: preload the presentation, then embed the view returned by buildView() in an AndroidView
    var loaded by remember { mutableStateOf<PLYPresentation?>(null) }

    LaunchedEffect(Unit) {
        loaded = try {
            PLYPresentation {
                placementId("ACCOUNT")
                onCloseRequested { displayPaywall.value = false }
            }.preload()
        } catch (e: Exception) {
            displayPaywall.value = false
            null
        }
    }

    loaded?.let { presentation ->
        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .padding(0.dp, 5.dp), // Occupy the max size in the Compose UI tree
            factory = { context ->
                // Creates the custom paywall view from the preloaded presentation
                presentation.buildView(context) ?: FrameLayout(context)
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    AndroidPublicTheme {
        Greeting("Android")
    }
}