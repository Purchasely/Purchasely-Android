package com.purchasely.samplev2.presentation.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.unit.sp
import com.purchasely.samplev2.presentation.theme.Brand

/**
 * The design's input: filled a shade under the card it sits on, hairline outlined, and picking up
 * the primary colour on focus. Shared by every field in the app so they resolve identically.
 */
@Composable
fun commonFieldColors(): TextFieldColors = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = Brand.colors.fieldBackground,
    unfocusedContainerColor = Brand.colors.fieldBackground,
    disabledContainerColor = Brand.colors.fieldBackground,
    unfocusedBorderColor = Brand.colors.fieldBorder,
    focusedBorderColor = colorScheme.primary,
    cursorColor = colorScheme.primary,
    focusedTextColor = colorScheme.onSurface,
    unfocusedTextColor = colorScheme.onSurface,
    focusedLabelColor = colorScheme.onSurfaceVariant,
    unfocusedLabelColor = colorScheme.onSurfaceVariant,
    focusedTrailingIconColor = colorScheme.onSurfaceVariant,
    unfocusedTrailingIconColor = colorScheme.onSurfaceVariant
)

/** Fields hold identifiers, urls and raw values, so they are set in the mono face. */
val commonFieldTextStyle: TextStyle
    @Composable get() = TextStyle(
        color = colorScheme.onSurface,
        fontFamily = FontFamily.Monospace,
        fontSize = 14.sp
    )

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommonTextField(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    onValueChange: (String) -> Unit
) {
    val focusManager = LocalFocusManager.current
    var textState by remember { mutableStateOf(value) }

    LaunchedEffect(value) {
        textState = value
    }

    OutlinedTextField(
        value = textState,
        onValueChange = {
            textState = it
            onValueChange(textState)
        },
        modifier = modifier,
        singleLine = true,

        shape = MaterialTheme.shapes.small,
        textStyle = commonFieldTextStyle,
        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = ImeAction.Done),
        label = {
            Text(
                text = label,
                color = colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        colors = commonFieldColors(),
        placeholder = {
            Text(
                label,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.alpha(0.5f)
            )
        },
        trailingIcon = {
            if (textState.isNotEmpty()) {
                IconButton(
                    onClick = {
                        textState = ""
                        onValueChange(textState)
                    }
                ) {
                    Icon(Icons.Default.Close, contentDescription = "clear text")
                }
            }
        }
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropDownCommonTextField(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    onValueChange: (String) -> Unit,
    historyKeywords: List<String>
) {
    val focusManager = LocalFocusManager.current
    var textState by remember { mutableStateOf(value) }
    var expanded by remember { mutableStateOf(false) }

//    val matchingKeywords = remember(textState, historyKeywords) {
//        historyKeywords.filter { it.startsWith(textState, ignoreCase = true) }
//    }

    LaunchedEffect(value) {
        textState = value
    }

    Box(modifier = modifier) {
        OutlinedTextField(
            value = textState,
            onValueChange = {
                textState = it
                onValueChange(textState)
                expanded = true
            },
            modifier = modifier.onFocusChanged { expanded = it.isFocused },
            singleLine = true,
            shape = MaterialTheme.shapes.small,
            textStyle = commonFieldTextStyle,
            keyboardActions = KeyboardActions(onDone = {
                focusManager.clearFocus()
                expanded = false
            }),
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                imeAction = ImeAction.Done
            ),
            label = {
                Text(
                    text = label,
                    color = colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            colors = commonFieldColors(),
            placeholder = {
                Text(
                    label,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.alpha(0.5f)
                )
            },
            trailingIcon = {
                if (textState.isNotEmpty()) {
                    IconButton(
                        onClick = {
                            textState = ""
                            onValueChange(textState)
                            expanded = false
                        }
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "clear text")
                    }
                }
            }
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = {},
            properties = PopupProperties(focusable = false),
        ) {
            historyKeywords.forEach { keyword ->
                DropdownMenuItem(
                    text = { Text(text = keyword) },
                    onClick = {
                        expanded = false
                        textState = keyword
                        onValueChange(textState)
                        focusManager.clearFocus()
                    }
                )
            }
        }
    }
}
