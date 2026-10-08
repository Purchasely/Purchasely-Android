package com.purchasely.samplev2.presentation.screen.products

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Badge
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.purchasely.samplev2.presentation.common.navigationBarsContentPadding
import com.purchasely.samplev2.presentation.common.CommonScreenHeader
import com.purchasely.samplev2.presentation.common.CommonSearchBar
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProductsScreen(
    navController: NavController,
    viewModel: ProductsViewModel = koinViewModel()
) {
    var searchQuery by remember { mutableStateOf("") }
    val uiState = viewModel.uiState.collectAsState()

    Column(
        Modifier
            .background(colorScheme.background)
    ) {
        CommonScreenHeader(
            title = "Products and Plans",
            onBackClick = { navController.navigateUp() }
        )

        CommonSearchBar(modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp)) {
            searchQuery = it
            viewModel.filterProductsByQuery(it)
        }

        LazyColumn(
            contentPadding = navigationBarsContentPadding,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            uiState.value.products.forEach { product ->
                stickyHeader {
                    ProductItem(product)
                }

                items(product.plans) { plan ->
                    PlanCard(plan)
                }
            }
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
//fun ProductItem(product: PLYProduct) {
fun ProductItem(product: Product) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(colorScheme.background)
            .fillMaxWidth()
            .padding(horizontal = 18.dp)
            .padding(top = 18.dp, bottom = 12.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "${product.name}",
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = product.vendorId,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                color = colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Text(
            text = "${product.plans.size} " + if (product.plans.size == 1) "plan" else "plans",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = colorScheme.onPrimary,
            modifier = Modifier
                .background(colorScheme.primary, RoundedCornerShape(percent = 50))
                .padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}
