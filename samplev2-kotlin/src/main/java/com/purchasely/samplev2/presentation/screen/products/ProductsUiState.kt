package com.purchasely.samplev2.presentation.screen.products

import io.purchasely.models.PLYPlan
import io.purchasely.models.PLYProduct

/**
 * Data class that defines the ui state of [ProductsScreen]
 */
data class ProductsUiState(

    /**
     * List of available plans corresponding to all products.
     */
    val products: MutableList<Product> = mutableListOf()
)


/**
 * Used instead of [PLYProduct] in order to be able to filter on plans list.
 */
data class Product(
    val name: String?,
    val vendorId: String,
    val plans: MutableList<PLYPlan> = mutableListOf()
)