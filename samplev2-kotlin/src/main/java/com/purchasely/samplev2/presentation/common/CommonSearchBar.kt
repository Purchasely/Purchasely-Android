package com.purchasely.samplev2.presentation.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.purchasely.samplev2.presentation.theme.Gray500
import com.purchasely.samplev2.presentation.theme.Gray700


@Composable
fun CommonSearchBar(modifier: Modifier = Modifier, onValueChanged: ((String) -> Unit)) {
    val focusManager = LocalFocusManager.current
    var state by remember { mutableStateOf(TextFieldValue("")) }

    Surface(
        shadowElevation = 3.dp,
        shape = RoundedCornerShape(30.dp),
        modifier = modifier
    ) {
        BasicTextField(
            value = state,
            onValueChange = {
                state = it
                onValueChanged(it.text)
            },
            singleLine = true,
            maxLines = 1,
            modifier = Modifier
                .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(8.dp))
                .height(47.dp)
                .fillMaxWidth(),
            textStyle = TextStyle(
                MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
            decorationBox = { innerTextField ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "search icon",
                        tint = Gray700,
                    )
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (state.text.isEmpty())
                            Text(
                                text = "Search",
                                color = Gray500,
                            )
                        innerTextField()
                    }

                    if (state.text.isNotBlank()) {
                        IconButton(onClick = {
                            state = TextFieldValue("")
                            onValueChanged(state.text)
                        }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "clear search bar",
                                tint = Gray700
                            )
                        }
                    }
                }
            }
        )
    }
}