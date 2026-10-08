package com.purchasely.samplev2.presentation.screen.offerings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.purchasely.samplev2.presentation.theme.Brand
import com.purchasely.samplev2.presentation.common.navigationBarsContentPadding
import com.purchasely.samplev2.presentation.common.CommonDropdownMenu
import com.purchasely.samplev2.presentation.common.CommonScreenHeader
import com.purchasely.samplev2.presentation.common.CommonTextField
import com.purchasely.samplev2.presentation.common.commonFieldColors
import com.purchasely.samplev2.presentation.common.commonFieldTextStyle
import io.purchasely.models.PLYProduct
import io.purchasely.storage.userData.PLYDynamicOffering
import org.koin.androidx.compose.koinViewModel

@Composable
fun OfferingsScreen(navController: NavController, viewModel: OfferingsViewModel = koinViewModel()) {
    val uiState = viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .background(color = colorScheme.background)
    ) {
        CommonScreenHeader(
            title = "Dynamic Offerings",
            onBackClick = { navController.navigateUp() }
        )

        if(uiState.value.loading) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                CircularProgressIndicator(
                    color = colorScheme.primary,
                    strokeWidth = 10.dp,
                    modifier = Modifier.size(100.dp)
                )
            }
        }
        else {
            OfferingsInputCard(products = uiState.value.products) { reference, planId, offerId ->
                viewModel.addDynamicOffering(reference, planId, offerId)
            }

            if (uiState.value.error != null) {
                ErrorNotice(uiState.value.error!!) {
                    viewModel.hideError()
                }
            }

            OfferingsList(uiState.value.offerings) {
                viewModel.removeDynamicOffering(it)
            }
        }
    }
}

@Composable
fun OfferingsInputCard(products: List<PLYProduct>, onAddOfferingClick: (String, String, String?) -> Unit) {
    val focusManager = LocalFocusManager.current

    var reference by remember { mutableStateOf("") }
    var planId by remember { mutableStateOf("") }
    var offerId by remember { mutableStateOf<String?>(null) }

    val offerList = remember(planId) {
        products.map { it.plans }
            .flatten()
            .firstOrNull { it.vendorId == planId }
            ?.promoOffers
            ?.map { it.vendorId } ?: emptyList()
    }

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
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CommonTextField(
                    modifier = Modifier.weight(1f),
                    label = "Reference",
                    value = reference,
                    onValueChange = { reference = it })
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                GroupedPlansDropdownMenu(
                    modifier = Modifier.weight(1f),
                    label = "Plan",
                    products = products,
                    selected = planId,
                    onItemChange = {
                        planId = it
                        offerId = null
                    }
                )

                CommonDropdownMenu(
                    modifier = Modifier.weight(1f),
                    label = "Offer",
                    items = offerList.mapNotNull { it },
                    selected = offerId ?: "None",
                    onItemChange = { offerId = it }
                )
            }
            Button(
                enabled = reference.isNotBlank().and(planId.isNotBlank()),
                onClick = {
                    onAddOfferingClick(reference, planId, offerId)
                    focusManager.clearFocus()
                    reference = ""
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
        }
    }
}

@Composable
fun OfferingsList(offerings: List<PLYDynamicOffering>, onRemoveClick: (String) -> Unit) {
    LazyColumn(
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = navigationBarsContentPadding,
        modifier = Modifier.fillMaxSize()
    ) {
        if (offerings.isNotEmpty()) {
            items(offerings.size) {
                val offering = offerings[it]

                OfferingItem(
                    reference = offering.reference,
                    planId = offering.planId,
                    offerId = offering.offerId,
                    onRemoveClick = {
                        onRemoveClick(offering.reference)
                    }
                )
            }
        } else {
            item {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .fillParentMaxSize()
                ) {
                    Text(
                        "No dynamic offerings found.",
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
fun OfferingItem(reference: String, planId: String, offerId: String?, onRemoveClick: (String) -> Unit) {
    Card(
        colors = CardDefaults.cardColors(colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = shapes.medium)
            .padding(horizontal = 16.dp)
            .padding(vertical = 5.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 8.dp)
        ) {
            Text(
                text = reference,
                style = MaterialTheme.typography.labelMedium,
                color = colorScheme.onSurface,
                textAlign = TextAlign.Center,
                overflow = TextOverflow.Visible,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            )
            Text(
                text = planId,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = colorScheme.onSurface,
                textAlign = TextAlign.Center,
                overflow = TextOverflow.Visible,
                modifier = Modifier
                    .weight(2f)
                    .fillMaxWidth()
            )
            Text(
                text = offerId ?: "-",
                style = MaterialTheme.typography.labelMedium,
                color = colorScheme.onSurface,
                textAlign = TextAlign.Center,
                overflow = TextOverflow.Visible,
                modifier = Modifier
                    .weight(2f)
                    .fillMaxWidth()
            )

            IconButton(
                modifier = Modifier,
                onClick = { onRemoveClick(reference) }
            ) {
                Icon(
                    tint = colorScheme.error.copy(alpha = 0.9f),
                    imageVector = Icons.Rounded.Delete,
                    contentDescription = "delete",
                    modifier = Modifier
                        .size(15.dp)
                        .weight(1f)
                )
            }
        }
    }
}


@Composable
fun ErrorNotice(message: String, onRemoveClick: () -> Unit) {
    Card(
        elevation = CardDefaults.cardElevation(2.dp),
        border = BorderStroke(2.dp, colorScheme.error.copy(alpha = 0.8f)),
        colors = CardDefaults.cardColors(colorScheme.error),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Error",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = colorScheme.onError,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp, bottom = 6.dp)
            )

            IconButton(
                modifier = Modifier,
                onClick = { onRemoveClick() }
            ) {
                Icon(
                    tint = colorScheme.onError,
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "remove",
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Text(
            text = message,
            style = MaterialTheme.typography.labelLarge,
            color = colorScheme.onError,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp, top = 6.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupedPlansDropdownMenu(modifier: Modifier = Modifier, label: String, products: List<PLYProduct>, selected: String, onItemChange: (String) -> Unit) {
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
                shape = shapes.medium,
                textStyle = commonFieldTextStyle,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expansionState) },
                colors = commonFieldColors(),
                label = { Text(text = label, color = colorScheme.onSurface) },
                maxLines = 1,
                singleLine = true,
                modifier = modifier.menuAnchor()
            )

            ExposedDropdownMenu(
                expanded = expansionState,
                onDismissRequest = { expansionState = false },
                modifier = Modifier
                    .background(color = Brand.colors.raised)
                    .fillMaxWidth()
            ) {
                products.forEach {
                    Text(
                        text = it.vendorId,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    it.plans.forEach { plan ->
                        plan.vendorId?.let { vendorId ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = vendorId,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.padding(start = 10.dp),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = colorScheme.onSurface
                                    )
                                },
                                onClick = {
                                    selectedState = vendorId
                                    expansionState = false
                                    onItemChange(vendorId)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}