package com.purchasely.samplev2.presentation.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.MaterialTheme.colorScheme
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
import androidx.compose.ui.unit.sp


@Composable
fun CommonSearchBar(modifier: Modifier = Modifier, onValueChanged: ((String) -> Unit)) {
    val focusManager = LocalFocusManager.current
    var state by remember { mutableStateOf(TextFieldValue("")) }

    BasicTextField(
        value = state,
        onValueChange = {
            state = it
            onValueChanged(it.text)
        },
        singleLine = true,
        maxLines = 1,
        modifier = modifier
            .fillMaxWidth()
            .height(46.dp)
            .background(colorScheme.surfaceVariant, RoundedCornerShape(14.dp))
            .border(1.dp, colorScheme.outline, RoundedCornerShape(14.dp)),
        textStyle = TextStyle(color = colorScheme.onSurface, fontSize = 15.sp),
        cursorBrush = SolidColor(colorScheme.onSurface),
        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
        decorationBox = { innerTextField ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 14.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "search icon",
                    tint = colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(17.dp)
                )
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (state.text.isEmpty()) {
                        Text(
                            text = "Search",
                            fontSize = 15.sp,
                            color = colorScheme.onSurfaceVariant
                        )
                    }
                    innerTextField()
                }

                if (state.text.isNotBlank()) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "clear search bar",
                        tint = colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .size(18.dp)
                            .clickable {
                                state = TextFieldValue("")
                                onValueChanged(state.text)
                            }
                    )
                }
            }
        }
    )
}