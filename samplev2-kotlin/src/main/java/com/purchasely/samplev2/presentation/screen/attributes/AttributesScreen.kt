package com.purchasely.samplev2.presentation.screen.attributes

import android.app.DatePickerDialog
import android.widget.DatePicker
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.purchasely.samplev2.presentation.common.CommonDropdownMenu
import com.purchasely.samplev2.presentation.common.CommonScreenHeader
import com.purchasely.samplev2.presentation.common.CommonTextField
import com.purchasely.samplev2.presentation.theme.Gray100
import com.purchasely.samplev2.presentation.theme.Gray200
import com.purchasely.samplev2.presentation.theme.Gray500
import com.purchasely.samplev2.presentation.theme.Gray700
import com.purchasely.samplev2.presentation.theme.Red500
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun AttributesScreen(navController: NavController, viewModel: AttributesViewModel = hiltViewModel()) {
    val attributes = viewModel.uiState.collectAsState()

    Column(modifier = Modifier.background(color = Gray100)) {
        CommonScreenHeader(
            title = "User Attributes",
            onBackClick = { navController.navigateUp() }
        )
        AttributeInputCard { key, value ->
            viewModel.addAttribute(key, value)
        }

        AttributesList(attributes.value.attributes) {
            viewModel.removeAttribute(it)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttributeInputCard(onAddAttributeClick: (String, Any) -> Unit) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val typeList = listOf("String", "Int", "Float", "Boolean", "Date")

    var attributeKey by remember { mutableStateOf("") }
    var attributeValue by remember { mutableStateOf("") }
    var attributeType by remember { mutableStateOf(typeList.first()) }

    Card(
        colors = CardDefaults.cardColors(Color.White),
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
                        attributeValue = "True"
                        CommonDropdownMenu(
                            Modifier.weight(1f),
                            label = "Value",
                            items = listOf("True", "False"),
                            selected = attributeValue
                        ) {
                            attributeValue = it
                        }
                    }

                    "Date" -> {
                        val calendar = Calendar.getInstance()
                        val picker = DatePickerDialog(
                            context,
                            { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
                                calendar.set(year, month, dayOfMonth)
                                attributeValue = dateFormat.format(calendar.time)
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
                                textStyle = TextStyle(color = Gray700),
                                colors = TextFieldDefaults.outlinedTextFieldColors(unfocusedBorderColor = Gray200),
                                singleLine = true,
                                label = {
                                    Text(
                                        text = "Value",
                                        color = Gray500,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                            )
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .alpha(0f)
                                    .clickable(onClick = { picker.show() }),
                            )
                        }
                    }
                }
            }
            Button(
                enabled = attributeKey.isNotBlank().and(attributeValue.isNotBlank()),
                onClick = {
                    onAddAttributeClick(attributeKey, attributeValue)
                    focusManager.clearFocus()
                    attributeKey = ""
                    attributeValue = ""
                },
                colors = ButtonDefaults.buttonColors(colorScheme.primary),
                shape = shapes.small,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Add", color = Color.White)
            }
        }
    }
}

@Composable
fun AttributesList(attributes: Map<String, Any>, onRemoveClick: (String) -> Unit) {
    LazyColumn(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {
        if (attributes.isNotEmpty()) {
            items(attributes.toList()) { attribute ->
                AttributeItem(
                    key = attribute.first,
                    value = "${attribute.second}",
                    onRemoveClick = {
                        onRemoveClick(attribute.first)
                    }
                )
            }
        } else {
            item {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(
                        "No attributes found.",
                        modifier = Modifier.padding(8.dp),
                        fontWeight = FontWeight.SemiBold,
                        color = Gray700
                    )

                }
            }

        }
    }
}

@Composable
fun AttributeItem(key: String, value: String, onRemoveClick: (String) -> Unit) {
    Card(
        colors = CardDefaults.cardColors(Color.White),
        elevation = CardDefaults.cardElevation(4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = shapes.medium)
            .padding(horizontal = 16.dp)
            .padding(vertical = 5.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 8.dp, bottom = 8.dp)
        ) {
            Text(
                text = key,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = Gray700,
                textAlign = TextAlign.Center,
                modifier = Modifier
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = Gray700,
                textAlign = TextAlign.Center,
                modifier = Modifier
            )
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = { onRemoveClick(key) }) {
                Icon(
                    tint = Red500,
                    imageVector = Icons.Rounded.Delete,
                    contentDescription = "delete",
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}