package com.purchasely.samplev2.presentation

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.purchasely.samplev2.presentation.navigation.Navigation
import com.purchasely.samplev2.presentation.theme.PurchaselyTheme
import io.purchasely.ext.Purchasely

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        installSplashScreen()
        handleDeepLink(intent)
        setContent {
            PurchaselyTheme(dynamicColor = false) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Navigation()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        //Purchasely.close()
    }

    private fun handleDeepLink(intent: Intent?) {
        if (intent?.action == Intent.ACTION_VIEW) {
            intent.data?.let {
                Purchasely.isDeeplinkHandled(it)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    PurchaselyTheme {
        Navigation()
    }
}