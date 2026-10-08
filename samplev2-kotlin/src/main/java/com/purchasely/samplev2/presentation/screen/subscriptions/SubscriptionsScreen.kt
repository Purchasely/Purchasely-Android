package com.purchasely.samplev2.presentation.screen.subscriptions

import android.content.Context
import android.text.format.DateFormat
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.purchasely.samplev2.presentation.theme.Brand
import com.purchasely.samplev2.presentation.common.navigationBarsContentPadding
import com.purchasely.samplev2.presentation.common.CommonScreenHeader
import io.purchasely.ext.DistributionType
import io.purchasely.ext.PLYOfferType
import io.purchasely.ext.PLYSubscriptionStatus
import io.purchasely.models.PLYSubscription
import io.purchasely.models.PLYSubscriptionData
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

@Composable
fun SubscriptionsScreen(
    navController: NavController,
    viewModel: SubscriptionsViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .background(colorScheme.background)
            .fillMaxSize()
    ) {
        CommonScreenHeader(
            title = "User Subscriptions",
            onBackClick = { navController.navigateUp() }
        )

        Row(
            horizontalArrangement = Arrangement.End,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            TextButton(
                onClick = { viewModel.load(invalidateCache = true) },
                enabled = !uiState.isLoading
            ) {
                Text(text = "Refresh")
            }
        }

        when {
            uiState.isLoading -> CenteredMessage { CircularProgressIndicator() }

            uiState.error != null -> CenteredMessage {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = uiState.error.orEmpty(), color = colorScheme.error)
                    TextButton(onClick = { viewModel.load(invalidateCache = true) }) {
                        Text(text = "Retry")
                    }
                }
            }

            uiState.subscriptions.isEmpty() -> CenteredMessage {
                Text(text = "No active subscriptions.", color = colorScheme.onSurfaceVariant)
            }

            else -> LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = navigationBarsContentPadding,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                items(uiState.subscriptions) { subscription ->
                    SubscriptionCard(subscription)
                }
            }
        }
    }
}

@Composable
private fun SubscriptionCard(subscription: PLYSubscriptionData) {
    val context = LocalContext.current
    val data = subscription.data
    val plan = subscription.plan
    val product = subscription.product
    var expanded by remember { mutableStateOf(false) }

    // localizedFullPrice() reaches into the store layer; guard it so the demo never crashes.
    val price = remember(subscription) {
        try {
            plan.localizedFullPrice().takeIf { it.isNotBlank() }
        } catch (e: Exception) {
            null
        }
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, colorScheme.outline),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = product.name ?: plan.name ?: "Subscription",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    plan.name?.let { planName ->
                        Text(
                            text = planName,
                            style = MaterialTheme.typography.bodySmall,
                            color = colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
                statusStyle(data.subscriptionStatus)?.let { style ->
                    Spacer(Modifier.width(12.dp))
                    StatusChip(style)
                }
            }

            Divider()

            // Always-visible summary.
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                DetailRow("Store", data.storeType?.displayName)
                DetailRow("Renews", data.getFormattedRenewalDate(context))
                DetailRow("Price", price)
                data.offerType
                    ?.takeIf { it != PLYOfferType.NONE }
                    ?.let { DetailRow("Offer", it.name.humanize()) }
            }

            AnimatedVisibility(visible = expanded) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Section("Subscription")
                    DetailRow("Purchased", formatDate(context, data.purchasedAt))
                    DetailRow("First purchase", formatDate(context, data.originalPurchasedAt))
                    DetailRow("Cancelled", formatDate(context, data.cancelledAt))
                    DetailRow("Duration", durationLabel(data))
                    DetailRow("Environment", data.environment?.name?.humanize())
                    DetailRow("Store country", data.storeCountry)
                    DetailRow("Family shared", data.isFamilyShared?.let { if (it) "Yes" else "No" })
                    DetailRow("Revenue", revenueLabel(data))
                    DetailRow("Offer id", data.offerIdentifier)
                    DetailRow("Content id", data.contentId)
                    DetailRow("Subscription id", data.id)

                    Section("Plan")
                    DetailRow("Name", plan.name)
                    DetailRow("Vendor id", plan.vendorId)
                    DetailRow("Store product id", plan.getProductId())
                    DetailRow("Base plan id", plan.basePlanId)
                    DetailRow("Type", plan.type?.takeIf { it != DistributionType.UNKNOWN }?.name?.humanize())
                    DetailRow("Level", plan.level?.toString())
                    DetailRow("Plan id", plan.id.ifBlank { data.planId })

                    Section("Product")
                    DetailRow("Name", product.name)
                    DetailRow("Vendor id", product.vendorId)
                    DetailRow("Product id", product.id)
                }
            }

            ExpandToggle(expanded = expanded) { expanded = !expanded }
        }
    }
}

/**
 * A "label — value" row, aligned as a spec sheet (label left, value right). Renders only when
 * [value] carries something to show, so absent fields leave no empty rows behind.
 */
@Composable
private fun DetailRow(label: String, value: String?) {
    if (value.isNullOrBlank()) return
    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = colorScheme.onSurface,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f)
        )
    }
}

/** Small uppercase group heading for the expanded sections. */
@Composable
private fun Section(title: String) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.SemiBold,
        color = colorScheme.onSurfaceVariant,
        letterSpacing = 0.8.sp,
        modifier = Modifier.padding(top = 6.dp)
    )
}

/** A tinted pill with a status dot: green when renewing, amber for billing states, red when expired. */
@Composable
private fun StatusChip(style: StatusStyle) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(percent = 50))
            .background(style.color.copy(alpha = 0.12f))
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(style.color)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = style.label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = style.color
        )
    }
}

/** The "More/Fewer details" affordance with a chevron that flips as the card expands. */
@Composable
private fun ExpandToggle(expanded: Boolean, onToggle: () -> Unit) {
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = "chevron"
    )
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable { onToggle() }
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = if (expanded) "Fewer details" else "More details",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium,
            color = colorScheme.primary
        )
        Icon(
            imageVector = Icons.Default.KeyboardArrowDown,
            contentDescription = null,
            tint = colorScheme.primary,
            modifier = Modifier
                .size(20.dp)
                .rotate(rotation)
        )
    }
}

@Composable
private fun Divider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
            .height(1.dp)
            .background(colorScheme.outline)
    )
}

private data class StatusStyle(val label: String, val color: Color)

@Composable
@ReadOnlyComposable
private fun statusStyle(status: PLYSubscriptionStatus?): StatusStyle? {
    status ?: return null
    val color = when {
        status == PLYSubscriptionStatus.AUTO_RENEWING -> Brand.colors.ok
        status.isExpired() -> colorScheme.error
        else -> Brand.colors.warning // ON_HOLD, IN_GRACE_PERIOD, AUTO_RENEWING_CANCELED
    }
    return StatusStyle(label = status.name.humanize(), color = color)
}

/** Coarsest non-zero period: "1 month", "2 weeks", "10 days". */
private fun durationLabel(data: PLYSubscription): String? = when {
    data.subscriptionDurationInMonths > 0 -> plural(data.subscriptionDurationInMonths, "month")
    data.subscriptionDurationInWeeks > 0 -> plural(data.subscriptionDurationInWeeks, "week")
    data.subscriptionDurationInDays > 0 -> plural(data.subscriptionDurationInDays, "day")
    else -> null
}

private fun plural(count: Int, unit: String): String = "$count $unit" + if (count > 1) "s" else ""

private fun revenueLabel(data: PLYSubscription): String? =
    data.cumulatedRevenuesInUSD
        .takeIf { it > 0.0 }
        ?.let { "$" + String.format(Locale.US, "%.2f", it) }

/** "IN_GRACE_PERIOD" -> "In grace period"; "FREE_TRIAL" -> "Free trial". */
private fun String.humanize(): String =
    lowercase().replace('_', ' ').replaceFirstChar { it.uppercase() }

/**
 * Formats a backend ISO-8601 timestamp with the device's medium date format, matching the
 * renewal row. Falls back to the date portion of the raw string if it cannot be parsed.
 */
private fun formatDate(context: Context, raw: String?): String? {
    if (raw.isNullOrBlank()) return null
    for (parser in isoDateParsers) {
        val parsed = try {
            parser.parse(raw)
        } catch (e: Exception) {
            null
        }
        if (parsed != null) return DateFormat.getMediumDateFormat(context).format(parsed)
    }
    return raw.substringBefore('T').ifBlank { null }
}

private val isoDateParsers: List<SimpleDateFormat> by lazy {
    listOf(
        "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
        "yyyy-MM-dd'T'HH:mm:ss'Z'",
        "yyyy-MM-dd'T'HH:mm:ss",
        "yyyy-MM-dd"
    ).map { pattern ->
        SimpleDateFormat(pattern, Locale.US).apply { timeZone = TimeZone.getTimeZone("UTC") }
    }
}

@Composable
private fun CenteredMessage(content: @Composable () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        content()
    }
}
