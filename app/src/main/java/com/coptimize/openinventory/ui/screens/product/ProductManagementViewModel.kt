package com.coptimize.openinventory.ui.screens.product

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coptimize.openinventory.data.model.Product
import com.coptimize.openinventory.data.repository.ProductRepository
import com.coptimize.openinventory.data.repository.UserSessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductManagementViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val userSessionRepository: UserSessionRepository
) : ViewModel() {

    // This converts the Flow from the repository into a StateFlow that the UI can collect.
    // It's efficient because it only runs when the UI is visible.
    val activeProducts: StateFlow<List<Product>> = productRepository.getAllActiveProducts()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    val archivedProducts: StateFlow<List<Product>> = productRepository.getArchivedProducts()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    /**
     * Restores a product from the archived list to the active list.
     * This function should be called from the UI.
     */
    fun restoreProduct(product: Product) {
        viewModelScope.launch {
            val currentUserId = userSessionRepository.getCurrentUserId()
            productRepository.restoreProduct(
                productId = product.id,
                userId = currentUserId
            )
        }
    }

    /**
     * Deletes (archives) a product from the active list.
     */
    fun deleteProduct(product: Product) {
        viewModelScope.launch {
            val currentUserId = userSessionRepository.getCurrentUserId()

            productRepository.deleteProduct(
                productId = product.id,
                userId = currentUserId
            )
        }
    }
}