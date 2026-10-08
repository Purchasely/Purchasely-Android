package com.purchasely.samplev2.presentation.screen.attributes

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.purchasely.samplev2.presentation.common.CommonDropdownMenu
import com.purchasely.samplev2.presentation.common.CommonScreenHeader
import com.purchasely.samplev2.presentation.common.CommonTextField
import com.purchasely.samplev2.presentation.common.commonFieldColors
import com.purchasely.samplev2.presentation.common.commonFieldTextStyle
import com.purchasely.samplev2.presentation.common.navigationBarsContentPadding
import com.purchasely.samplev2.presentation.theme.Brand
import io.purchasely.ext.PLYDataProcessingLegalBasis
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun AttributesScreen(navController: NavController, viewModel: AttributesViewModel = koinViewModel()) {
    val attributes = viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .background(color = colorScheme.background)
            .fillMaxSize() // ensure we can apply inset padding
            //.statusBarsPadding() // add top padding for status bar
    ) {
        CommonScreenHeader(
            title = "User Attributes",
            onBackClick = { navController.navigateUp() }
        )
        AttributeInputCard(
            onAddAttributeClick = { key, value, type, legal -> viewModel.addAttribute(key, value, type, legal) },
            onStepAttribute = { key, amount, legal, increment -> viewModel.stepAttribute(key, amount, legal, increment) },
            onClearAllClick = { viewModel.clearAttributes() }
        )

        AttributesList(attributes.value.attributes) {
            viewModel.removeAttribute(it)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttributeInputCard(
    onAddAttributeClick: (String, String, String, String) -> Unit,
    onStepAttribute: (String, String, String, Boolean) -> Unit,
    onClearAllClick: () -> Unit
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
    val typeList = listOf("String", "Int", "Float", "Boolean", "Date", "IntArray", "FloatArray", "StringArray", "BooleanArray")

    var attributeKey by remember { mutableStateOf("") }
    var attributeValue by remember { mutableStateOf("") }
    var attributeType by remember { mutableStateOf(typeList.first()) }
    var attributeLegalBasis by remember { mutableStateOf(PLYDataProcessingLegalBasis.OPTIONAL.name) }

    Card(
        colors = CardDefaults.cardColors(colorScheme.surface),
        elevation = CardDefaults.cardElevation(4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = shapes.small)
            .padding(horizontal = 16.dp, vertical = 20.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 16.dp)
        ) {
            CommonDropdownMenu(
                modifier = Modifier.fillMaxWidth(),
                label = "Legal Basis",
                items = PLYDataProcessingLegalBasis.entries.map { it.name },
                selected = attributeLegalBasis,
                onItemChange = {
                    attributeLegalBasis = it
                }
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CommonTextField(
                    modifier = Modifier.weight(1f),
                    label = "Key",
                    value = attributeKey,
                    onValueChange = { attributeKey = it })

                CommonDropdownMenu(
                    modifier = Modifier.weight(1f),
                    label = "Type",
                    items = typeList,
                    selected = attributeType,
                    onItemChange = {
                        attributeType = it
                        attributeValue = ""
                    }
                )

                when (attributeType) {
                    "String" -> {
                        CommonTextField(
                            modifier = Modifier.weight(1f),
                            label = "Value",
                            value = attributeValue,
                            onValueChange = { attributeValue = it })
                    }

                    "Float" -> {
                        CommonTextField(
                            Modifier.weight(1f),
                            label = "Value",
                            value = attributeValue,
                            keyboardType = KeyboardType.Decimal,
                            onValueChange = { attributeValue = it })
                    }

                    "Int" -> {
                        CommonTextField(
                            Modifier.weight(1f),
                            label = "Value",
                            value = attributeValue,
                            KeyboardType.Number,
                            onValueChange = { attributeValue = it })
                    }

                    "Boolean" -> {
                        CommonDropdownMenu(
                            Modifier.weight(1f),
                            label = "Value",
                            items = listOf("true", "false"),
                            selected = attributeValue
                        ) {
                            attributeValue = it
                        }
                    }

                    "Date" -> {
                        val calendar = Calendar.getInstance()
                        val datePicker = DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                calendar.set(year, month, dayOfMonth)

                                val timePicker = TimePickerDialog(
                                    context,
                                    { _, hourOfDay, minute ->
                                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                                        calendar.set(Calendar.MINUTE, minute)

                                        attributeValue = dateFormat.format(calendar.time)
                                    },
                                    calendar.get(Calendar.HOUR_OF_DAY),
                                    calendar.get(Calendar.MINUTE),
                                    true // Use true if you want to display 24-hour format
                                )
                                timePicker.show()
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                        )

                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedTextField(
                                value = attributeValue,
                                onValueChange = { attributeValue = it },
                                shape = shapes.medium,
                                textStyle = commonFieldTextStyle,
                                colors = commonFieldColors(),
                                singleLine = true,
                                label = {
                                    Text(
                                        text = "Value",
                                        color = colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                            )
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .alpha(0f)
                                    .clickable(onClick = { datePicker.show() }),
                            )
                        }
                    }

                    "IntArray", "FloatArray", "StringArray", "BooleanArray" -> {
                        CommonTextField(
                            Modifier.weight(1f),
                            label = "Value",
                            value = attributeValue,
                            onValueChange = { attributeValue = it }
                        )
                    }
                }
            }
            Button(
                enabled = attributeKey.isNotBlank().and(attributeValue.isNotBlank()),
                onClick = {
                    onAddAttributeClick(
                        attributeKey,
                        attributeValue,
                        attributeType,
                        attributeLegalBasis
                    )
                    focusManager.clearFocus()
                    attributeKey = ""
                    attributeValue = ""
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorScheme.primary,
                    contentColor = colorScheme.onPrimary
                ),
                shape = shapes.small,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Add")
            }
            // Counters: the value field is the amount (blank = 1), the key must hold an Int.
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    enabled = attributeKey.isNotBlank(),
                    onClick = { onStepAttribute(attributeKey, attributeValue, attributeLegalBasis, true) },
                    shape = shapes.small,
                    modifier = Modifier.weight(1f)
                ) { Text("Increment") }
                OutlinedButton(
                    enabled = attributeKey.isNotBlank(),
                    onClick = { onStepAttribute(attributeKey, attributeValue, attributeLegalBasis, false) },
                    shape = shapes.small,
                    modifier = Modifier.weight(1f)
                ) { Text("Decrement") }
                OutlinedButton(
                    onClick = onClearAllClick,
                    shape = shapes.small,
                    modifier = Modifier.weight(1f)
                ) { Text("Clear all") }
            }
        }
    }

    if(attributeType == "IntArray" || attributeType == "FloatArray" || attributeType == "StringArray" || attributeType == "BooleanArray") {
        InformationNotice()
    }
}

@Composable
fun AttributesList(attributes: Map<String, Any>, onRemoveClick: (String) -> Unit) {
    val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
    LazyColumn(
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = navigationBarsContentPadding,
        modifier = Modifier.fillMaxSize()
    ) {
        if (attributes.isNotEmpty()) {
            items(attributes.toList()) { attribute ->
                val value = when(attribute.second) {
                    is Date -> formatter.format(attribute.second)
                    is Array<*> -> "[${(attribute.second as? Array<*>)?.joinToString("; ")}]"
                    else -> attribute.second
                }

                AttributeItem(
                    key = attribute.first,
                    value = "$value",
                    type = attribute.second::class.simpleName ?: "",
                    onRemoveClick = {
                        onRemoveClick(attribute.first)
                    }
                )
            }
        } else {
            item {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .fillMaxWidth()
                ) {
                    Text(
                        "No attributes found.",
                        modifier = Modifier.padding(8.dp),
                        fontWeight = FontWeight.SemiBold,
                        color = colorScheme.onSurface
                    )

                }
            }

        }
    }
}

@Composable
fun AttributeItem(key: String, value: String, type: String, onRemoveClick: (String) -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, colorScheme.outline),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(vertical = 5.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = key,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        // Shrinks before the pill does, so the type stays readable on long keys.
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    TypePill(type)
                }
                Text(
                    text = value,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp,
                    color = colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            IconButton(
                onClick = { onRemoveClick(key) },
                modifier = Modifier.size(38.dp)
            ) {
                Icon(
                    tint = colorScheme.error,
                    imageVector = Icons.Rounded.Delete,
                    contentDescription = "delete",
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

/** The attribute type, tinted so it reads as metadata on the key rather than as a value. */
@Composable
private fun TypePill(type: String) {
    Text(
        text = type.uppercase(),
        fontSize = 8.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.5.sp,
        color = Brand.colors.ok,
        maxLines = 1,
        modifier = Modifier
            .background(
                color = Brand.colors.ok.copy(alpha = 0.16f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 6.dp)
    )
}

@Composable
fun InformationNotice() {
    Card(
        colors = CardDefaults.cardColors(colorScheme.error.copy(alpha = 0.8f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 16.dp)
    ) {
        Text(
            text = "Notice",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = colorScheme.onError,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(top = 16.dp, bottom = 6.dp)
        )
        Text(
            text = "For arrays, values should be separated with semicolons \";\"\nFor example: value1;value2;value3",
            style = MaterialTheme.typography.bodySmall,
            color = colorScheme.onError,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp, top = 6.dp)
        )
    }
}