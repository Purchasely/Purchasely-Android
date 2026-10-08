package com.purchasely.samplev2.presentation.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Two columns of label/value pairs, filling left to right. */
@Composable
fun DetailGrid(items: List<Pair<String, String>>, modifier: Modifier = Modifier) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp), modifier = modifier) {
        items.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                row.forEach { (label, value) ->
                    InformationItem(label, value, Modifier.weight(1f))
                }
                if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

/**
 * An overline label above its value. Values are machine-written, hence monospace.
 * [maxLines] defaults to what fits a grid cell; raise it where the value is the point.
 */
@Composable
fun InformationItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    maxLines: Int = 2
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp), modifier = modifier) {
        Text(
            text = label.uppercase(),
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.6.sp,
            color = colorScheme.onSurfaceVariant
        )
        Text(
            // A blank leaves the label orphaned, which reads as a rendering fault rather than
            // an absent value.
            text = value.ifBlank { "N/A" },
            fontFamily = FontFamily.Monospace,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            overflow = TextOverflow.Ellipsis,
            maxLines = maxLines
        )
    }
}
