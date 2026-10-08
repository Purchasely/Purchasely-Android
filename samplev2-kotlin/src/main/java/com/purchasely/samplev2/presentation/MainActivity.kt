package com.purchasely.samplev2.presentation

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.purchasely.samplev2.data.repository.PreferencesRepositoryImpl.Companion.HISTORY_PRESENTATION
import com.purchasely.samplev2.data.repository.PreferencesRepositoryImpl.Companion.PRESENTATION_ID
import com.purchasely.samplev2.data.repository.PreferencesRepositoryImpl.Companion.THEME
import com.purchasely.samplev2.domain.preferences.PreferencesRepository
import com.purchasely.samplev2.presentation.navigation.Navigation
import com.purchasely.samplev2.presentation.theme.PurchaselyTheme
import io.purchasely.ext.Purchasely
import io.purchasely.views.presentation.PLYThemeMode
import kotlinx.coroutines.flow.map
import org.koin.android.ext.android.inject

class MainActivity : AppCompatActivity() {

    private val preferencesRepository: PreferencesRepository by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Must come first: enableEdgeToEdge() touches the window, and doing that before the splash
        // theme is swapped out locks in its action bar.
        installSplashScreen()

        // Draw behind the system bars on every API level, not only where the platform forces it.
        // Screens inset themselves, and the hero deliberately paints under the status bar.
        enableEdgeToEdge()

        //adb shell am start -a android.intent.action.VIEW -d "purchasely://ply/placements/android" com.purchasely.demo
        handleDeepLink(intent)

        setContent {
            PurchaselyTheme(darkTheme = isDarkTheme(), dynamicColor = false) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Navigation()
                }
            }
        }
    }

    /**
     * A scan that arrives while this activity is already running reaches [onNewIntent] instead of
     * [onCreate] whenever the caller sets `FLAG_ACTIVITY_SINGLE_TOP`. [setIntent] is what makes the
     * new intent visible to the SDK's automatic deeplink interception, which reads the foreground
     * activity's intent.
     */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleDeepLink(intent)
    }

    /**
     * The app follows the SDK's theme mode, so a paywall and the screen hosting it never disagree.
     * The stored setting is only a change signal: [Purchasely.getThemeMode] stays the source of
     * truth, re-read whenever Settings writes a new value.
     */
    @Composable
    private fun isDarkTheme(): Boolean {
        val themeMode by preferencesRepository.observeKey(THEME)
            .map { Purchasely.getThemeMode() }
            .collectAsState(initial = Purchasely.getThemeMode())

        return when (themeMode) {
            PLYThemeMode.DARK -> true
            PLYThemeMode.LIGHT -> false
            PLYThemeMode.SYSTEM -> isSystemInDarkTheme()
        }
    }

    //url example: https://purchasely.io/ply/presentations/<PRESENTATION_ID>
    private fun handleDeepLink(intent: Intent?) {
        if (intent?.action == Intent.ACTION_VIEW) {
            intent.data?.let {
                // A scanned screen (`.../ply/presentations/{vendor_id}?preview=1&...`) becomes the
                // current presentation: Home reopens it in one tap and Settings lists it in the
                // presentation history.
                it.pathSegments
                    .dropWhile { segment -> segment != "presentations" }
                    .getOrNull(1)
                    ?.takeIf { vendorId -> vendorId.isNotBlank() }
                    ?.let { vendorId ->
                        preferencesRepository.setString(PRESENTATION_ID, vendorId)
                        preferencesRepository.addToHistory(HISTORY_PRESENTATION, vendorId)
                    }

                // No Purchasely.handleDeeplink(it) call needed: the SDK auto-intercepts
                // Purchasely deeplinks from the activity intent (automaticDeeplinkHandling,
                // default true).
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
