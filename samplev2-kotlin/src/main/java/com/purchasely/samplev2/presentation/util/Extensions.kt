package com.purchasely.samplev2.presentation.util

import io.purchasely.ext.presentation.PLYPresentation
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


fun Date.formatDate(): String {
    val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    return formatter.format(this)
}

fun Any?.toFormattedString(indent: String = ""): String {
    return when (this) {
        is List<*> -> this.joinToString(separator = "\n") { it.toFormattedString(indent) }
        is Map<*, *> -> this.entries.joinToString(separator = "\n") {
            "$indent${it.key}: ${it.value.toFormattedString(indent)}"
        }
        null -> "null"
        else -> this.toString()
    }
}

/** What the SDK resolved for a presentation: type, placement and the ids behind the choice. */
fun PLYPresentation.info(): List<Pair<String, String?>> = listOf(
    "type" to type.name,
    "placementId" to placementId,
    "audienceId" to audienceId,
    "abTestId" to abTestId,
    "campaignId" to campaignId,
    "flowId" to flowId,
)
