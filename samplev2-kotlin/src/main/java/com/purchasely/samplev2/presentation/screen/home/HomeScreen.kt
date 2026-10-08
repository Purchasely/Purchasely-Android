package com.purchasely.samplev2.presentation.screen.home

import android.widget.FrameLayout
import android.widget.Toast
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.outlined.ManageAccounts
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavHostController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.purchasely.demo.R
import com.purchasely.samplev2.presentation.MainActivity
import com.purchasely.samplev2.presentation.common.DisplayMethod
import com.purchasely.samplev2.presentation.common.SdkStatus
import com.purchasely.samplev2.presentation.common.navigationBarsContentPadding
import com.purchasely.samplev2.presentation.navigation.Screen
import com.purchasely.samplev2.presentation.theme.Brand
import com.purchasely.samplev2.presentation.theme.StatusError
import com.purchasely.samplev2.presentation.theme.StatusOk
import com.purchasely.samplev2.presentation.theme.StatusPending
import io.purchasely.ext.presentation.PLYPresentation
import io.purchasely.views.presentation.models.PLYDimensionType
import org.koin.androidx.compose.koinViewModel


@Composable
fun HomeScreen(navController: NavHostController, viewModel: HomeViewModel = koinViewModel()) {
    val uiState = viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val systemUiController = rememberSystemUiController()
    systemUiController.isSystemBarsVisible = true


    LaunchedEffect(Unit) {
        viewModel.setPaywallInterceptors(navController = navController)
        // Re-read on every entry so returning from Settings shows the updated configuration.
        viewModel.refreshProperties()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(color = colorScheme.background)
                .graphicsLayer { this.alpha = if (uiState.value.isRestoring) 0.2f else 1f },
        ) {
            // Pinned: identity and SDK state stay in view while the actions scroll under them.
            HomeScreenHeader(
                sdkVersion = uiState.value.sdkVersion,
                sdkStatus = uiState.value.sdkStatus,
                onSettingsClick = { navController.navigate(Screen.Settings.route) }
            ) {
                PropertiesCard(uiState.value) { label, text ->
                    viewModel.saveToClipboard(context, label, text)
                }
            }

            // Pinned like a real inline widget: stays mounted for as long as this screen is.
            uiState.value.inlineScreen?.let {
                InlineScreen(
                    screen = it,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(top = 16.dp)
                )
            }

            LazyColumn(
                horizontalAlignment = Alignment.CenterHorizontally,
                contentPadding = navigationBarsContentPadding,
                modifier = Modifier.weight(1f)
            ) {
                item {
                    PurchaselyActions(
                        uiState = uiState.value,
                        onRestoreClick = { viewModel.restore() },
                        onSynchronizeClick = { viewModel.synchronize() },
                        onProductsClick = { navController.navigate(Screen.Products.route) },
                        onSubscriptionsClick = { navController.navigate(Screen.Subscriptions.route) },
                        onAttributesClick = { navController.navigate(Screen.Attributes.route) },
                        onBuiltInAttributesClick = { navController.navigate(Screen.BuiltInAttributes.route) },
                        onCustomEventsClick = { navController.navigate(Screen.CustomEvents.route) },
                        onOfferingsClick = { navController.navigate(Screen.Offerings.route) },
                        onDisplayScreenClick = {
                            viewModel.onDisplayScreenClick(context) {
                                navController.navigate(Screen.Paywall.route)
                            }
                        },
                        onLogsClick = { navController.navigate(Screen.Logs.route) },
                        onPrivacyClick = { navController.navigate(Screen.Privacy.route) }
                    )
                }
            }
        }
    }

    if (uiState.value.isRestoring) {
        RestoreProgressIndicator()
    }
}

/**
 * Evergreen hero the home screen opens on: identity, SDK state, and the properties card, rounded
 * off at the bottom so the light canvas below reads as a separate surface.
 */
@Composable
fun HomeScreenHeader(
    sdkVersion: String,
    sdkStatus: SdkStatus,
    onSettingsClick: () -> Unit,
    properties: @Composable () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Brand.colors.hero,
                shape = RoundedCornerShape(bottomStart = 30.dp, bottomEnd = 30.dp)
            )
            .statusBarsPadding()
            .padding(horizontal = 16.dp)
            .padding(bottom = 22.dp)
    ) {
        IconButton(
            onClick = { onSettingsClick() },
            modifier = Modifier.align(Alignment.End)
        ) {
            Icon(
                imageVector = Icons.Outlined.ManageAccounts,
                contentDescription = "settings",
                tint = Brand.colors.onHero,
                modifier = Modifier.size(30.dp)
            )
        }
        Image(
            painter = painterResource(R.drawable.logo),
            contentDescription = "logo",
            modifier = Modifier
                .size(64.dp)
                .padding(bottom = 8.dp)
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SdkStatusDot(status = sdkStatus)
            Text(
                text = "Purchasely Demo",
                style = typography.headlineMedium,
                color = Brand.colors.onHero
            )
        }
        Text(
            text = "v.${sdkVersion} ${stringResource(id = R.string.lang)}",
            fontSize = 13.sp,
            color = Brand.colors.onHero.copy(alpha = 0.62f),
            modifier = Modifier.padding(top = 2.dp, bottom = 18.dp)
        )
        properties()
    }
}

/**
 * SDK start state as a pulsing dot: green once started, orange while starting, red when start
 * failed. The halo keeps pulsing so the dot reads as live state rather than a static bullet, and
 * pulses faster while the outcome is still pending.
 */
@Composable
private fun SdkStatusDot(status: SdkStatus) {
    val color = when (status) {
        SdkStatus.STARTED -> StatusOk
        SdkStatus.STARTING -> StatusPending
        SdkStatus.ERROR -> StatusError
    }
    val pulse by rememberInfiniteTransition(label = "sdkStatus").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (status == SdkStatus.STARTING) 800 else 1800,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse"
    )
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(16.dp)
            .semantics { contentDescription = "SDK ${status.name.lowercase()}" }
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .graphicsLayer {
                    val scale = 0.5f + pulse * 0.5f
                    scaleX = scale
                    scaleY = scale
                    alpha = 1f - pulse
                }
                .background(color, CircleShape)
        )
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, CircleShape)
        )
    }
}

@Composable
fun PurchaselyActions(
    uiState: HomeUiState,
    onDisplayScreenClick: () -> Unit,
    onProductsClick: () -> Unit,
    onSubscriptionsClick: () -> Unit,
    onAttributesClick: () -> Unit,
    onBuiltInAttributesClick: () -> Unit,
    onCustomEventsClick: () -> Unit,
    onOfferingsClick: () -> Unit,
    onRestoreClick: () -> Unit,
    onSynchronizeClick: () -> Unit,
    onLogsClick: () -> Unit,
    onPrivacyClick: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 18.dp, bottom = 28.dp)
    ) {
        HomeActionButton(text = "Display Screen", onClick = onDisplayScreenClick)
        HomeActionButton(text = "Products", onClick = onProductsClick)
        HomeActionButton(text = "Subscriptions", onClick = onSubscriptionsClick)

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            HomeActionButton("Attributes", onAttributesClick, Modifier.weight(1f))
            HomeActionButton("Built-in Attributes", onBuiltInAttributesClick, Modifier.weight(1f))
        }

        HomeActionButton(text = "Custom Events", onClick = onCustomEventsClick)

        HomeActionButton(text = "Offerings", onClick = onOfferingsClick)

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            HomeActionButton("Restore", onRestoreClick, Modifier.weight(1f))
            HomeActionButton(
                text = if (uiState.isSynchronizing) "Synchronizing…" else "Synchronize",
                onClick = onSynchronizeClick,
                modifier = Modifier.weight(1f)
            )
        }

        HomeActionButton(text = "Logs", onClick = onLogsClick)
        HomeActionButton(text = "Privacy", onClick = onPrivacyClick)
    }
}

/** White tile on the light canvas: hairline outline plus a low shadow to lift it off the page. */
@Composable
private fun HomeActionButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = colorScheme.surface,
            contentColor = colorScheme.onSurface
        ),
        border = BorderStroke(1.dp, colorScheme.outline),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 1.dp,
            pressedElevation = 0.dp
        ),
        contentPadding = PaddingValues(horizontal = 8.dp),
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
    ) {
        Text(text = text, style = typography.labelLarge)
    }
}


/**
 * Summary of what the SDK is configured to do: which backend and mode it runs in, who the user is,
 * what the next screen request will target, and how it will be displayed. Every value is
 * tap-to-copy so it can be pasted into the dashboard.
 *
 * Collapsed by default to the handful of properties worth checking constantly; expanding reveals
 * the rest rather than making the home screen scroll.
 */
@Composable
fun PropertiesCard(state: HomeUiState, onPropertyClick: (String, String) -> Unit) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val properties = state.properties()

    Card(
        colors = CardDefaults.cardColors(colorScheme.primaryContainer),
        modifier = Modifier.fillMaxWidth()
    ) {
        // Width only: the header is no longer inside a scrolling item, so filling the height here
        // would take the whole screen and leave the actions no room.
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(vertical = 14.dp, horizontal = 24.dp)
            ) {
                properties
                    .filter { expanded || it.essential }
                    .forEach { property ->
                        PropertyData(label = property.label, value = property.value) {
                            onPropertyClick(property.label, property.value)
                        }
                    }
            }
            // Overlaid rather than placed in a header row, so collapsing costs no extra height.
            IconButton(
                onClick = { expanded = !expanded },
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Icon(
                    imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = if (expanded) "Collapse properties" else "Expand properties",
                    tint = colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
        }
    }
}

/** A single row of the properties card. */
private data class HomeProperty(
    val label: String,
    val value: String,
    /** Shown while the card is collapsed; everything else needs expanding. */
    val essential: Boolean = false,
)

/**
 * Properties to display, in order. Blank values are omitted entirely so the card never shows an
 * empty row, and the identifier that actually drives the next screen request is the one marked
 * essential — a placement wins over a screen id.
 */
private fun HomeUiState.properties(): List<HomeProperty> = buildList {
    add(HomeProperty("Running mode", if (isObserverMode) "Observer" else "Full", essential = true))
    if (userId.isNotBlank()) add(HomeProperty("User ID", userId, essential = true))
    // Backend lookups key off the anonymous id even for a logged-in user, so it is always listed.
    if (anonymousUserId.isNotBlank()) {
        add(HomeProperty("Anonymous ID", anonymousUserId, essential = userId.isBlank()))
    }
    if (placementId.isNotBlank()) {
        add(HomeProperty("Placement ID", placementId, essential = true))
    }
    if (presentationId.isNotBlank()) {
        add(HomeProperty("Screen ID", presentationId, essential = placementId.isBlank()))
    }
    if (contentId.isNotBlank()) add(HomeProperty("Content ID", contentId))
    if (planId.isNotBlank()) add(HomeProperty("Plan ID", planId))
    displayMethod?.let { add(HomeProperty("Display method", it.name, essential = true)) }
    if (themeMode.isNotBlank()) add(HomeProperty("Theme", themeMode))
    // The transition only applies to DISPLAY, the one path that passes a PLYTransition.
    if (displayMethod == DisplayMethod.DISPLAY) add(HomeProperty("Transition", transitionSummary()))
    add(HomeProperty("Deeplinks", if (allowDeeplink) "Allowed" else "Blocked"))
    add(HomeProperty("Campaigns", if (allowCampaigns) "Allowed" else "Blocked"))
}

/** `DRAWER 60%h 80%w`, omitting dimensions the transition leaves at its default. */
private fun HomeUiState.transitionSummary(): String = buildString {
    append(transitionType.name)
    transitionHeightValue.takeIf { it.isNotBlank() }?.let {
        append(" $it${transitionHeightType.symbol()}h")
    }
    transitionWidthValue.takeIf { it.isNotBlank() }?.let {
        append(" $it${transitionWidthType.symbol()}w")
    }
}

private fun PLYDimensionType.symbol(): String = when (this) {
    PLYDimensionType.PERCENTAGE -> "%"
    PLYDimensionType.PIXEL -> "px"
}

@Composable
fun PropertyData(label: String, value: String, onPropertyClick: () -> Unit) {
    Column {
        // lineHeight is pinned close to fontSize on both texts: the default leading adds a few dp
        // of dead space between a label and its value that padding cannot remove.
        Text(
            text = label,
            fontSize = 9.sp,
            lineHeight = 10.sp,
            color = colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.clickable { onPropertyClick() },
        ) {
            Text(
                text = value,
                fontSize = 13.sp,
                lineHeight = 15.sp,
                color = colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Bold,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
            )
            Image(
                painter = painterResource(id = R.drawable.ic_copy),
                contentDescription = "save to clipboard",
                modifier = Modifier.size(10.dp)
            )
        }
    }
}

@Composable
fun RestoreProgressIndicator() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(
            color = colorScheme.primary,
            strokeWidth = 12.dp,
            modifier = Modifier.size(100.dp)
        )
        Spacer(modifier = Modifier.size(20.dp))
        Text(
            text = "Restoring...",
            color = colorScheme.primary,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 20.sp
        )
    }
}

/**
 * Capped so a tall paywall cannot fill the viewport and push the actions below it out of reach,
 * and so its own scrolling never swallows the home list's.
 */
@Composable
fun InlineScreen(screen: PLYPresentation, modifier: Modifier = Modifier) {
    AndroidView(
        modifier = modifier.heightIn(max = 100.dp),
        factory = { context ->
            screen.buildView(
                context = context,
            ) { outcome ->
                val planName = outcome.plan?.name.orEmpty()
                Toast.makeText(context, "${outcome.purchaseResult} $planName", Toast.LENGTH_SHORT).show()
            } ?: FrameLayout(context)
        }
    )
}
