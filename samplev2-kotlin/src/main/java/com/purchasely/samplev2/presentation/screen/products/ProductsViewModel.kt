package com.purchasely.samplev2.presentation.screen.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.purchasely.ext.Purchasely
import io.purchasely.models.PLYPlan
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

/**
 * Home viewModel, providing [ProductsUiState] and methods for [ProductsScreen].
 */
class ProductsViewModel() : ViewModel() {

    /**
     * State flow of [ProductsUiState].
     */
    var uiState = MutableStateFlow(ProductsUiState())

    /**
     * All retrieved purchasely products.
     */
    private var allProducts = mutableListOf<Product>()

    init {
        viewModelScope.launch {
            getProducts()
        }
    }

    /**
     * Retrieve all products and plans.
     */
    private fun getProducts() {
        Purchasely.allProducts(
            onSuccess = {
                val products = it.map { product -> Product(product.name,product.vendorId,product.plans.toMutableList()) }
                allProducts.clear()
                allProducts.addAll(products)
                uiState.value = uiState.value.copy(products = allProducts)
            },
            onError = {
                allProducts.clear()
                uiState.value = uiState.value.copy(products = allProducts)
            }
        )
    }

    /**
     * Filter products matching [query].
     */
    fun filterProductsByQuery(query: String) {
        if (query.isEmpty()) {
            uiState.value = uiState.value.copy(products = allProducts.toMutableList())
        } else {
            uiState.value = uiState.value.copy(products = allProducts.flatMap { product ->
                val filteredPlans = product.plans.filter(query).toMutableList()
                if (filteredPlans.isNotEmpty()) listOf(product.copy(plans = filteredPlans)) else emptyList()
            }.toMutableList())
        }
    }
}

/**
 * Extension of [PLYPlan].
 * Filter a list of plans matching [query].
 */
private fun List<PLYPlan>.filter(query: String): List<PLYPlan> =
    this.filter { plan ->
        plan.name?.contains(query, ignoreCase = true) == true ||
                plan.vendorId?.contains(query, ignoreCase = true) == true ||
                plan.store_product_id?.contains(query, ignoreCase = true) == true
    }

