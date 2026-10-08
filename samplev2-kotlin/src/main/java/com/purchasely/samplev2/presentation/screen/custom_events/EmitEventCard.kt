package com.purchasely.samplev2.presentation.screen.custom_events

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.purchasely.samplev2.presentation.common.commonFieldColors
import com.purchasely.samplev2.presentation.common.commonFieldTextStyle
import com.purchasely.samplev2.presentation.theme.Brand

private val propertyTypes = listOf("String", "Int", "Double", "Boolean")

private val FieldHeight = 40.dp

/** One property row of the form. Compared by identity, so it can key its row. */
class EventProperty {
    var key by mutableStateOf("")
    var type by mutableStateOf(propertyTypes.first())
    var value by mutableStateOf("")

    /** Null when [value] does not parse as [type], which disables Emit. */
    val typedValue: Any?
        get() = when (type) {
            "Int" -> value.toIntOrNull()
            "Double" -> value.toDoubleOrNull()
            "Boolean" -> value.toBooleanStrictOrNull()
            else -> value
        }
}

/**
 * What the form holds. Kept by the caller, so it survives the card scrolling out of a lazy list
 * and the name can be filled from outside.
 */
class EmitEventState {
    var name by mutableStateOf("")
    val properties = mutableStateListOf<EventProperty>()

    /** Replaces the form with an already emitted event, so it can be tweaked and emitted again. */
    fun fill(event: EmittedEvent) {
        name = event.name
        properties.clear()
        event.properties.forEach { (key, value) ->
            properties += EventProperty().also {
                it.key = key
                // The values are the form's own typed values, so their class name is one of propertyTypes.
                it.type = value?.let { v -> v::class.simpleName }?.takeIf { t -> t in propertyTypes } ?: "String"
                it.value = value?.toString().orEmpty()
            }
        }
    }
}

/** Builds a custom event, a name plus any number of typed properties, and hands it to [onEmit]. */
@Composable
fun EmitEventCard(state: EmitEventState, onEmit: (name: String, properties: Map<String, Any?>) -> Unit) {
    val properties = state.properties

    Card(
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, colorScheme.outline),
        shape = RoundedCornerShape(22.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = "Emit custom event", style = typography.titleSmall, fontWeight = FontWeight.SemiBold)

            CompactField(
                value = state.name,
                onValueChange = { state.name = it },
                placeholder = "event_name",
                modifier = Modifier.fillMaxWidth()
            )

            properties.forEach { property ->
                key(property) {
                    PropertyRow(property, onRemove = { properties.remove(property) })
                }
            }

            // Drops the 48dp touch-target floor so the button hugs its label between the rows.
            CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides Dp.Unspecified) {
                TextButton(
                    onClick = { properties.add(EventProperty()) },
                    contentPadding = PaddingValues(horizontal = 12.dp),
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .height(32.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Property", style = typography.labelLarge)
                }
            }

            Button(
                enabled = state.name.isNotBlank() &&
                    properties.all { it.key.isBlank() || it.typedValue != null },
                onClick = {
                    onEmit(
                        state.name,
                        properties.filter { it.key.isNotBlank() }.associate { it.key to it.typedValue }
                    )
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorScheme.primary,
                    contentColor = colorScheme.onPrimary
                ),
                shape = shapes.small,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(FieldHeight)
            ) {
                Text("Emit")
            }
        }
    }
}

/** Key, type and value on a single line; the value input follows the type. */
@Composable
private fun PropertyRow(property: EventProperty, onRemove: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        CompactField(
            value = property.key,
            onValueChange = { property.key = it },
            placeholder = "key",
            modifier = Modifier.weight(1f)
        )
        TypePicker(
            type = property.type,
            onTypeChange = {
                property.type = it
                property.value = if (it == "Boolean") "false" else ""
            }
        )
        Box(modifier = Modifier.weight(1f)) {
            if (property.type == "Boolean") {
                BooleanPicker(value = property.value, onValueChange = { property.value = it })
            } else {
                CompactField(
                    value = property.value,
                    onValueChange = { property.value = it },
                    placeholder = "value",
                    isError = property.value.isNotEmpty() && property.typedValue == null,
                    keyboardType = when (property.type) {
                        "Int" -> KeyboardType.Number
                        "Double" -> KeyboardType.Decimal
                        else -> KeyboardType.Text
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        IconButton(onClick = onRemove, modifier = Modifier.size(32.dp)) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Remove property",
                tint = colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

/** The type as a tinted pill, read as metadata on the key like the attribute type pills. */
@Composable
private fun TypePicker(type: String, onTypeChange: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .width(76.dp)
                .height(FieldHeight)
                .clip(shapes.extraSmall)
                .background(Brand.colors.ok.copy(alpha = 0.16f))
                .clickable { expanded = true }
        ) {
            Text(
                text = type.uppercase(),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp,
                color = Brand.colors.ok,
                maxLines = 1
            )
            Icon(
                Icons.Default.ExpandMore,
                contentDescription = "Property type",
                tint = Brand.colors.ok,
                modifier = Modifier.size(14.dp)
            )
        }
        OptionsMenu(expanded, propertyTypes, onDismiss = { expanded = false }, onSelect = onTypeChange)
    }
}

/**
 * A true / false menu styled like [CompactField]. The shared
 * [com.purchasely.samplev2.presentation.common.CommonDropdownMenu] is a 56dp labelled field and
 * would make a Boolean row taller than the others.
 */
@Composable
private fun BooleanPicker(value: String, onValueChange: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .height(FieldHeight)
                .clip(shapes.extraSmall)
                .background(Brand.colors.fieldBackground)
                .border(1.dp, Brand.colors.fieldBorder, shapes.extraSmall)
                .clickable { expanded = true }
                .padding(horizontal = 12.dp)
        ) {
            Text(text = value, style = compactTextStyle, modifier = Modifier.weight(1f))
            Icon(
                Icons.Default.ExpandMore,
                contentDescription = "Boolean value",
                tint = colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
        }
        OptionsMenu(expanded, listOf("true", "false"), onDismiss = { expanded = false }, onSelect = onValueChange)
    }
}

@Composable
private fun OptionsMenu(
    expanded: Boolean,
    options: List<String>,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        modifier = Modifier.background(Brand.colors.raised)
    ) {
        options.forEach {
            DropdownMenuItem(
                text = { Text(it, style = typography.labelMedium) },
                onClick = {
                    onDismiss()
                    onSelect(it)
                }
            )
        }
    }
}

private val compactTextStyle: TextStyle
    @Composable get() = commonFieldTextStyle.copy(fontSize = 13.sp)

/**
 * The app's field look at [FieldHeight] instead of the 56dp Material minimum, with a placeholder
 * in place of the floating label, so a property fits on one line.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CompactField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    val interactionSource = remember { MutableInteractionSource() }
    val colors = commonFieldColors()
    val textStyle = compactTextStyle

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        textStyle = textStyle,
        cursorBrush = SolidColor(colorScheme.primary),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = ImeAction.Next),
        interactionSource = interactionSource,
        modifier = modifier.height(FieldHeight),
        decorationBox = { innerTextField ->
            OutlinedTextFieldDefaults.DecorationBox(
                value = value,
                innerTextField = innerTextField,
                enabled = true,
                singleLine = true,
                visualTransformation = VisualTransformation.None,
                interactionSource = interactionSource,
                isError = isError,
                placeholder = { Text(placeholder, style = textStyle, color = colorScheme.onSurfaceVariant) },
                colors = colors,
                contentPadding = PaddingValues(horizontal = 12.dp),
                container = {
                    OutlinedTextFieldDefaults.Container(
                        enabled = true,
                        isError = isError,
                        interactionSource = interactionSource,
                        colors = colors,
                        shape = shapes.extraSmall
                    )
                }
            )
        }
    )
}
