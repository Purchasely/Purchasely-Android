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
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import io.purchasely.ext.Purchasely
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
    AndroidView(
        modifier = Modifier
            .fillMaxSize()
            .padding(0.dp, 5.dp), // Occupy the max size in the Compose UI tree
        factory = { context ->
            // Creates custom view
            val paywall = Purchasely.presentationViewForPlacement(
                context,
                "ACCOUNT",
                onClose = {
                    displayPaywall.value = false
                })

            paywall ?: FrameLayout(context)
        },
        update = { view ->
            // View's been inflated or state read in this block has been updated
            // Add logic here if necessary

            // As selectedItem is read here, AndroidView will recompose
            // whenever the state changes
            // Example of Compose -> View communication
        }
    )
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    AndroidPublicTheme {
        Greeting("Android")
    }
}