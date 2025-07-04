package com.coptimize.openinventory.ui.screens.sale

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coptimize.openinventory.data.model.Product
import com.coptimize.openinventory.data.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

// This annotation tells Hilt how to create this ViewModel
@HiltViewModel
class MainSaleViewModel @Inject constructor(
    private val productRepository: ProductRepository // Hilt provides this automatically
    // Inject other repositories like CartRepository here later
) : ViewModel() {

    // This converts the Flow from the repository into a StateFlow that the UI can collect.
    // It's efficient because it only runs when the UI is visible.
    val products: StateFlow<List<Product>> = productRepository.getAllActiveProducts()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    // TODO: Add functions for addToCart, removeFromCart, completeSale etc.
    fun onAddToCart(product: Product) {
        // Business logic will go here
        println("Added to cart: ${product.name}")
    }
}