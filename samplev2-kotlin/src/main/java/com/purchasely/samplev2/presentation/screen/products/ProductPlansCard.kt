package com.purchasely.samplev2.presentation.screen.products

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toLowerCase
import androidx.compose.ui.unit.dp
import com.purchasely.samplev2.presentation.theme.Gray500
import io.purchasely.models.PLYPlan


@Composable
fun PlanCard(plan: PLYPlan) {
    var expanded by remember { mutableStateOf(false) }
    Card(
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.secondaryContainer),
        elevation = CardDefaults.cardElevation(4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(vertical = 5.dp)
            .clip(shape = MaterialTheme.shapes.medium)
            .clickable(onClick = { expanded = !expanded })

    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 20.dp)
                    .fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = "${plan.name}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )

                    Text(
                        text = "${plan.vendorId}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Gray500,
                    )
                    Text(
                        text = "${plan.store_product_id}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Gray500,
                    )
                }

                Icon(
                    imageVector = Icons.Default.ExpandMore,
                    contentDescription = "expand",
                    modifier = Modifier
                        .align(CenterVertically)
                        .graphicsLayer(
                            rotationZ = animateFloatAsState(
                                if (expanded) 180f else 0f
                            ).value,
                        )
                )
            }
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(tween(durationMillis = 300, easing = FastOutLinearInEasing)),
                exit = shrinkVertically(tween(durationMillis = 300, easing = FastOutLinearInEasing))
            ) {
                PlanInformation(plan = plan)
            }
        }
    }
}

@Composable
fun PlanInformation(plan: PLYPlan) {
    Column(modifier = Modifier.padding(vertical = 6.dp, horizontal = 16.dp)) {
        Text(style = MaterialTheme.typography.bodyMedium,
            text = "localized price: ${plan.localizedPrice()}"
        )
        Text(
            style = MaterialTheme.typography.bodyMedium,
            text = "localized full price: ${plan.localizedFullPrice()}"
        )
        Text(
            style = MaterialTheme.typography.bodyMedium,
            text = "localized intro. price: ${plan.localizedOfferPrice()}"
        )
        Text(
            style = MaterialTheme.typography.bodyMedium,
            text = "localized full intro. price: ${plan.localizedFullOfferPrice()}"
        )
        Divider(Modifier.padding(vertical = 10.dp))

        Text(
            style = MaterialTheme.typography.bodyMedium,
            text = "intro. period: ${plan.offerPeriod()?.unit?.name}"
        )
        Text(
            style = MaterialTheme.typography.bodyMedium,
            text = "localized period: ${plan.localizedPeriod()}"
        )
        Text(
            style = MaterialTheme.typography.bodyMedium,
            text = "localized intro. period: ${plan.localizedOfferPeriod()}"
        )
        Text(
            style = MaterialTheme.typography.bodyMedium,
            text = "period unit: ${plan.period()?.unit?.name?.toLowerCase(Locale.current)}"
        )
        Text(
            style = MaterialTheme.typography.bodyMedium,
            text = "free trial period unit: ${plan.freeTrialPeriod()?.unit?.name}"
        )
        Divider(Modifier.padding(vertical = 10.dp))

        Text(
            style = MaterialTheme.typography.bodyMedium,
            text = "duration: ${plan.duration()}"
        )
        Text(
            style = MaterialTheme.typography.bodyMedium,
            text = "intro. duration: ${plan.offerDuration()}"
        )
        Text(
            style = MaterialTheme.typography.bodyMedium,
            text = "localized intro. duration: ${plan.localizedOfferDuration()}"
        )
        Text(
            style = MaterialTheme.typography.bodyMedium,
            text = "localized trial duration: ${plan.localizedTrialDuration()}"
        )
        Divider(Modifier.padding(vertical = 10.dp))

        Text(
            style = MaterialTheme.typography.bodyMedium,
            text = "intro. cycles: ${plan.offerCycles()}"
        )
        Text(style = MaterialTheme.typography.bodyMedium, text = "amount: ${plan.amount()}")
        Text(style = MaterialTheme.typography.bodyMedium, text = "currency code: ${plan.currencyCode()}")
        Text(style = MaterialTheme.typography.bodyMedium, text = "currency symbol: ${plan.currencySymbol()}")
        Divider(Modifier.padding(vertical = 10.dp))

        Text(
            style = MaterialTheme.typography.bodyMedium,
            text = "daily price: ${plan.dailyEquivalentPrice()}"
        )
        Text(
            style = MaterialTheme.typography.bodyMedium,
            text = "weekly price: ${plan.weeklyEquivalentPrice()}"
        )
        Text(
            style = MaterialTheme.typography.bodyMedium,
            text = "monthly price: ${plan.monthlyEquivalentPrice()}"
        )
        Text(
            style = MaterialTheme.typography.bodyMedium,
            text = "yearly price: ${plan.yearlyEquivalentPrice()}"
        )
        Divider(Modifier.padding(vertical = 10.dp))

        Text(style = MaterialTheme.typography.bodyMedium, text = "duration in days: ${plan.durationInDays()}")
        Text(
            style = MaterialTheme.typography.bodyMedium,
            text = "duration in weeks: ${plan.durationInWeeks()}"
        )
        Text(
            style = MaterialTheme.typography.bodyMedium,
            text = "duration in months: ${plan.durationInMonths()}"
        )
        Text(
            style = MaterialTheme.typography.bodyMedium,
            text = "duration in quarters: ${plan.durationInQuarters()}"
        )
        Text(
            style = MaterialTheme.typography.bodyMedium,
            text = "duration in years: ${plan.durationInYears()}"
        )
    }
}
