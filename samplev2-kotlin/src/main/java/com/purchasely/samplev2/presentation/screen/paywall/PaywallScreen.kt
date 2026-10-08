package com.purchasely.samplev2.presentation.screen.paywall

import android.util.Log
import android.widget.FrameLayout
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.purchasely.samplev2.presentation.util.Constants.Companion.TAG
import com.purchasely.samplev2.presentation.util.info
import io.purchasely.ext.presentation.PLYPresentation
import org.koin.androidx.compose.koinViewModel

/**
 * Embeds the presentation: preload (public API), then [PLYPresentation.buildView]. The info card
 * shows the metadata the SDK exposes about what it resolved (audience, A/B test, campaign, flow).
 */
@Composable
fun PaywallScreen(navController: NavController, viewModel: PaywallViewModel = koinViewModel()) {
    val presentation = viewModel.uiState.value.presentation

    Column(modifier = Modifier.fillMaxSize().background(colorScheme.background)) {
        if (presentation == null) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.CenterHorizontally).statusBarsPadding().padding(32.dp)
            )
        } else {
            PresentationInfoCard(presentation)
            AsyncPresentationView(presentation, onCloseClick = { navController.popBackStack() })
        }
    }
}

@Composable
private fun PresentationInfoCard(presentation: PLYPresentation) {
    Column(modifier = Modifier.statusBarsPadding().padding(horizontal = 16.dp, vertical = 8.dp)) {
        presentation.info().forEach { (label, value) ->
            Text(
                text = "$label: ${value ?: "-"}",
                style = typography.bodySmall,
                color = colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun AsyncPresentationView(
    presentation: PLYPresentation,
    onCloseClick: () -> Unit,
) {
    AndroidView(
        factory = { context ->
            presentation.onCloseRequested = { onCloseClick() }
            presentation.buildView(
                context = context,
            ) { outcome ->
                val plan = outcome.plan
                Log.d(TAG, "result: ${outcome.purchaseResult}, plan: $plan")
                Toast.makeText(context, "${outcome.purchaseResult} ${plan?.name.orEmpty()}", Toast.LENGTH_SHORT).show()
            } ?: FrameLayout(context)
        }
    )
}
