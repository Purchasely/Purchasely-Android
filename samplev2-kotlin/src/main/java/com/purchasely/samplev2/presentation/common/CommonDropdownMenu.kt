package com.purchasely.samplev2.presentation.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import com.purchasely.samplev2.presentation.theme.Gray200
import com.purchasely.samplev2.presentation.theme.Gray700
import com.purchasely.samplev2.presentation.theme.Purple100


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommonDropdownMenu(modifier: Modifier = Modifier, label: String, items: List<String>, selected: String, onItemChange: (String) -> Unit) {
    var expansionState by remember { mutableStateOf(false) }
    var selectedState by remember { mutableStateOf(selected.ifBlank { "None" }) }

    Box(
        modifier = modifier
    ) {
        ExposedDropdownMenuBox(
            expanded = expansionState,
            onExpandedChange = { expansionState = !expansionState },
        ) {
            OutlinedTextField(
                value = selectedState,
                readOnly = true,
                onValueChange = {},
                shape = MaterialTheme.shapes.medium,
                textStyle = TextStyle(color = Gray700),
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expansionState) },
                colors = TextFieldDefaults.outlinedTextFieldColors(unfocusedBorderColor = Gray200),
                label = { Text(text = label, color = Gray700) },
                maxLines = 1,
                singleLine = true,
                modifier = modifier.menuAnchor()
            )

            ExposedDropdownMenu(
                expanded = expansionState,
                onDismissRequest = { expansionState = false },
                modifier = Modifier.background(color = Purple100)
            ) {
                items.forEach { item ->
                    DropdownMenuItem(
                        text = { Text(text = item) },
                        onClick = {
                            selectedState = item
                            expansionState = false
                            onItemChange(item)
                        }
                    )
                }
            }
        }
    }
}