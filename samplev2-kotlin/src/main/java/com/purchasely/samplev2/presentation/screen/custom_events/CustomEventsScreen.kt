package com.purchasely.samplev2.presentation.screen.custom_events

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Loop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.purchasely.samplev2.presentation.common.CommonScreenHeader
import com.purchasely.samplev2.presentation.screen.settings.SwitchOption
import com.purchasely.samplev2.presentation.theme.Brand
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.Locale

private val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

@Composable
fun CustomEventsScreen(
    navController: NavController,
    viewModel: CustomEventsViewModel = koinViewModel()
) {
    val allowCampaigns by viewModel.allowCampaigns.collectAsState()
    val emittedEvents by viewModel.emittedEvents.collectAsState()
    val emitState = remember { EmitEventState() }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    Column(
        modifier = Modifier
            .background(colorScheme.background)
            .fillMaxSize()
    ) {
        CommonScreenHeader(
            title = "Custom Events",
            onBackClick = { navController.navigateUp() }
        )

        LazyColumn(
            state = listState,
            verticalArrangement = Arrangement.spacedBy(6.dp),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 12.dp + bottomInset)
        ) {
            item {
                EmitEventCard(emitState) { name, properties ->
                    viewModel.emit(name, properties)
                    Toast.makeText(context, "Emitted $name", Toast.LENGTH_SHORT).show()
                }
            }

            item {
                Section("Campaigns", "Off: emitted events cannot trigger a campaign. On: a campaign matching the event opens its screen.")
            }
            item { SwitchOption("ALLOW CAMPAIGNS", allowCampaigns) { viewModel.setAllowCampaigns(it) } }

            item { Section("History", "Events emitted from this screen, kept until the app closes.") }
            if (emittedEvents.isEmpty()) {
                item { EmptyNotice("Nothing emitted yet.") }
            } else {
                items(emittedEvents, key = { it.id }) { event ->
                    EmittedEventRow(event) {
                        emitState.fill(event)
                        scope.launch { listState.animateScrollToItem(0) }
                    }
                }
            }
        }
    }
}

/**
 * An emitted event as a log entry: its name, the time it left and its property count. Expands to
 * its properties when it has any; [onRefill] loads it back into the form.
 */
@Composable
private fun EmittedEventRow(event: EmittedEvent, onRefill: () -> Unit) {
    val count = event.properties.size
    var expanded by rememberSaveable { mutableStateOf(false) }
    val rotation by animateFloatAsState(if (expanded) 180f else 0f, label = "chevron")

    Card(
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, colorScheme.outline),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .clickable(enabled = count > 0) { expanded = !expanded }
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            FilledTonalIconButton(
                onClick = onRefill,
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = colorScheme.surfaceVariant,
                    contentColor = colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Loop,
                    contentDescription = "Refill the form with this event",
                    modifier = Modifier.size(18.dp)
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(3.dp), modifier = Modifier.weight(1f)) {
                EventName(event.name)
                Text(
                    text = timeFormat.format(event.emittedAt),
                    style = typography.bodySmall,
                    color = colorScheme.onSurfaceVariant
                )
            }

            if (count > 0) {
                Pill(if (count == 1) "1 PROPERTY" else "$count PROPERTIES", Brand.colors.ok)
            } else {
                Pill("NO PROPERTIES", colorScheme.onSurfaceVariant)
            }

            // Always reserves the chevron's width so the pills line up across rows.
            Box(modifier = Modifier.size(20.dp)) {
                if (count > 0) {
                    Icon(
                        imageVector = Icons.Default.ExpandMore,
                        contentDescription = if (expanded) "collapse" else "expand",
                        tint = colorScheme.onSurfaceVariant,
                        modifier = Modifier.rotate(rotation)
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(tween(300, easing = FastOutLinearInEasing)),
            exit = shrinkVertically(tween(300, easing = FastOutLinearInEasing))
        ) {
            PropertyPanel(event.properties)
        }
    }
}

/**
 * The properties in an inset panel, read like code: strings quoted and green, numbers in lavender,
 * booleans in amber, with the Kotlin type spelled out beside each value.
 */
@Composable
private fun PropertyPanel(properties: Map<String, Any?>) {
    Column(
        modifier = Modifier
            .padding(start = 14.dp, end = 14.dp, bottom = 14.dp)
            .fillMaxWidth()
            .background(colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        properties.entries.forEachIndexed { index, (key, value) ->
            if (index > 0) HorizontalDivider(color = colorScheme.outline)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                Text(
                    text = key,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    color = colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = if (value is String) "\"$value\"" else value.toString(),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = when (value) {
                        is String -> Brand.colors.ok
                        is Boolean -> colorScheme.onTertiaryContainer
                        else -> colorScheme.onSecondaryContainer
                    },
                    textAlign = TextAlign.End,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1.4f)
                )
                Text(
                    text = value?.let { it::class.simpleName }.orEmpty(),
                    fontSize = 10.sp,
                    color = colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.End,
                    modifier = Modifier.width(44.dp)
                )
            }
        }
    }
}

@Composable
private fun EventName(name: String, modifier: Modifier = Modifier) {
    Text(
        text = name,
        fontFamily = FontFamily.Monospace,
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier
    )
}

@Composable
private fun Pill(text: String, color: Color, modifier: Modifier = Modifier) {
    Text(
        text = text,
        fontSize = 9.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.5.sp,
        color = color,
        textAlign = TextAlign.Center,
        maxLines = 1,
        modifier = modifier
            .background(color.copy(alpha = 0.14f), shape = RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 1.dp)
    )
}

@Composable
private fun Section(title: String, description: String) {
    Column(
        verticalArrangement = Arrangement.spacedBy(2.dp),
        modifier = Modifier.padding(top = 10.dp, start = 4.dp, end = 4.dp, bottom = 2.dp)
    ) {
        Text(
            text = title.uppercase(),
            style = typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = colorScheme.onSurfaceVariant,
            letterSpacing = 0.8.sp
        )
        Text(text = description, style = typography.bodySmall, color = colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun EmptyNotice(text: String) {
    Text(
        text = text,
        style = typography.bodySmall,
        color = colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 4.dp)
    )
}
