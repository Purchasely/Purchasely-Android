package com.purchasely.samplev2.presentation.screen.built_in_attributes

import android.annotation.SuppressLint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.purchasely.samplev2.presentation.common.CommonScreenHeader
import com.purchasely.samplev2.presentation.common.InformationItem
import com.purchasely.samplev2.presentation.util.toFormattedString
import org.koin.androidx.compose.koinViewModel

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun BuiltInAttributesScreen(
    navController: NavController,
    viewModel: BuiltInAttributesViewModel = koinViewModel()
) {
    val uiState = viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            CommonScreenHeader(
                title = "Built-in Attributes",
                onBackClick = { navController.navigateUp() }
            )

        },
        // Opaque, since the list scrolls underneath it.
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colorScheme.background)
            ) {
                HorizontalDivider(color = colorScheme.outline)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Button(
                        onClick = { viewModel.retrieveBuiltInAttributes() },
                        shape = shapes.medium,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorScheme.primary,
                            contentColor = colorScheme.onPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .weight(1f)
                    ) {
                        Text(text = "Refresh", fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { viewModel.clearAllUserAttributes() },
                        shape = shapes.medium,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorScheme.error,
                            contentColor = colorScheme.onError
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .weight(1f)
                    ) {
                        Text(text = "Clear All", fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        content = {

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                contentPadding = it,
                modifier = Modifier.fillMaxSize()
            ) {
                if (uiState.value.attributes.isNotEmpty()) {
                    items(uiState.value.attributes.toList()) { attribute ->
                        AttributeItem(
                            key = attribute.first,
                            value = attribute.second.toFormattedString(),
                            onAttributeClick = {
                                viewModel.saveToClipboard(
                                    context = context,
                                    label = attribute.first,
                                    text = "${attribute.second}"
                                )
                            }
                        )
                    }
                }
            }
        }
    )
}

/** Tap to copy. The value is the point here, so it is never truncated. */
@Composable
fun AttributeItem(key: String, value: String, onAttributeClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, colorScheme.outline),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(vertical = 5.dp)
            .clickable { onAttributeClick() }
    ) {
        InformationItem(
            label = key,
            value = value,
            maxLines = Int.MAX_VALUE,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp)
        )
    }
}