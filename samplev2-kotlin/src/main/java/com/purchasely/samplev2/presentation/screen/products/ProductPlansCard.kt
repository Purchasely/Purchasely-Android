package com.purchasely.samplev2.presentation.screen.products

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import android.app.Activity
import android.widget.Toast
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.ui.platform.LocalContext
import io.purchasely.ext.PLYRunningMode
import io.purchasely.ext.Purchasely
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.purchasely.samplev2.presentation.common.DetailGrid
import io.purchasely.models.PLYPlan


@Composable
fun PlanCard(plan: PLYPlan) {
    var expanded by remember { mutableStateOf(false) }
    Card(
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, colorScheme.outline),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 12.dp)
            .clickable(onClick = { expanded = !expanded })
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${plan.name}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${plan.vendorId}",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${plan.store_product_id}",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Icon(
                    imageVector = Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "collapse" else "expand",
                    tint = colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .size(18.dp)
                        .graphicsLayer(rotationZ = animateFloatAsState(if (expanded) 180f else 0f).value)
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


/**
 * Everything the SDK exposes about a plan, grouped the way it is read: what it costs, how long it
 * runs, what the introductory offer changes, and the equivalent prices per period.
 */
@Composable
fun PlanInformation(plan: PLYPlan) {
    Column(
        verticalArrangement = Arrangement.spacedBy(18.dp),
        modifier = Modifier.padding(top = 14.dp)
    ) {
        HorizontalDivider(color = colorScheme.outline)

        DetailGrid(
            listOf(
                "Price" to plan.localizedPrice(),
                "Full price" to plan.localizedFullPrice(),
                "Amount" to "${plan.amount()}",
                "Currency" to "${plan.currencyCode()} ${plan.currencySymbol()}".trim()
            )
        )

        DetailGrid(
            listOf(
                "Period" to plan.localizedPeriod(),
                "Period unit" to (plan.period()?.unit?.name?.lowercase() ?: "N/A"),
                "Duration" to "${plan.duration()}",
                "Free trial" to "${plan.localizedTrialDuration()}"
            )
        )

        DetailGrid(
            listOf(
                "Intro price" to plan.localizedOfferPrice(),
                "Full intro price" to plan.localizedFullOfferPrice(),
                "Intro period" to plan.localizedOfferPeriod(),
                "Intro period unit" to (plan.offerPeriod()?.unit?.name?.lowercase() ?: "N/A"),
                "Intro duration" to plan.localizedOfferDuration(),
                "Intro cycles" to "${plan.offerCycles(null)}",
                "Free trial unit" to (plan.freeTrialPeriod()?.unit?.name?.lowercase() ?: "N/A")
            )
        )

        DetailGrid(
            listOf(
                "Daily price" to "${plan.dailyEquivalentPrice()}",
                "Weekly price" to "${plan.weeklyEquivalentPrice()}",
                "Monthly price" to "${plan.monthlyEquivalentPrice()}",
                "Yearly price" to "${plan.yearlyEquivalentPrice()}"
            )
        )

        DetailGrid(
            listOf(
                "Days" to "${plan.durationInDays()}",
                "Weeks" to "${plan.durationInWeeks()}",
                "Months" to "${plan.durationInMonths()}",
                "Quarters" to "${plan.durationInQuarters()}",
                "Years" to "${plan.durationInYears()}"
            )
        )

        // Purchasely.purchase() only validates in Full mode; Observer apps buy through their own billing code.
        val activity = LocalContext.current as? Activity
        val isFull = Purchasely.runningMode == PLYRunningMode.Full
        Button(
            enabled = isFull && activity != null,
            onClick = {
                activity?.let {
                    Purchasely.purchase(
                        activity = it,
                        plan = plan,
                        onSuccess = { purchased -> Toast.makeText(it, "Purchased ${purchased?.name}", Toast.LENGTH_SHORT).show() },
                        onError = { error -> Toast.makeText(it, "Error: ${error?.message}", Toast.LENGTH_SHORT).show() }
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isFull) "Buy" else "Full mode only")
        }

        Text(text = plan.dump(), fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = colorScheme.onSurfaceVariant)
    }
}
