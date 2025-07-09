package com.coptimize.openinventory.ui.screens.product

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coptimize.openinventory.data.model.Product
import com.coptimize.openinventory.data.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

// Represents the state of all fields on the screen
data class ProductEditUiState(
    val isLoading: Boolean = true,
    val isExistingProduct: Boolean = false,
    val isSaveSuccessful: Boolean = false,

    // Form fields - using String for direct TextField binding
    val name: String = "",
    val category: String = "",
    val manufacturer: String = "",
    val barcode: String = "",
    val price: String = "",
    val quantity: String = "",
    val tax: String = "",
    val isTaxFlatRate: Boolean = false,
    val imagePath: String = "",
    val isArchived: Boolean = false, // For the "deleted_at" status

    // For validation errors
    val nameError: String? = null,
    val priceError: String? = null
)

@HiltViewModel
class ProductEditViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val productId: String? = savedStateHandle["productId"]

    private val _uiState = MutableStateFlow(ProductEditUiState())
    val uiState = _uiState.asStateFlow()

    init {
        // Load the product if a valid ID was passed
        if (productId != null && productId != "-1") {
            loadProduct(productId)
        } else {
            // It's a new product, so we're ready to edit
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    private fun loadProduct(id: String) {
        viewModelScope.launch {
            val product = productRepository.getProduct(id)
            if (product != null) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isExistingProduct = true,
                        name = product.name,
                        category = product.category,
                        manufacturer = product.manufacturer ?: "",
                        barcode = product.barcode ?: "",
                        price = product.price.toString(),
                        quantity = product.quantity.toString(),
                        tax = product.tax?.toString() ?: "0.0",
                        isTaxFlatRate = product.isTaxFlatRate,
                        imagePath = product.imagePath ?: "",
                        isArchived = product.userId == null // A simple way to check if it's archived
                    )
                }
            } else {
                // Handle error case where product ID is invalid
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    // --- Event Handlers for UI ---
    fun onNameChange(value: String) = _uiState.update { it.copy(name = value, nameError = null) }
    fun onCategoryChange(value: String) = _uiState.update { it.copy(category = value) }
    fun onManufacturerChange(value: String) = _uiState.update { it.copy(manufacturer = value) }
    fun onBarcodeChange(value: String) = _uiState.update { it.copy(barcode = value) }
    fun onPriceChange(value: String) = _uiState.update { it.copy(price = value, priceError = null) }
    fun onQuantityChange(value: String) = _uiState.update { it.copy(quantity = value) }
    fun onTaxChange(value: String) = _uiState.update { it.copy(tax = value) }
    fun onTaxTypeChange(isFlat: Boolean) = _uiState.update { it.copy(isTaxFlatRate = isFlat) }
    fun onArchivedChange(isArchived: Boolean) = _uiState.update { it.copy(isArchived = isArchived) }


    fun saveProduct() {
        if (!validateInput()) return

        viewModelScope.launch {
            val currentState = _uiState.value
            val productToSave = Product(
                id = if (currentState.isExistingProduct) productId!! else UUID.randomUUID()
                    .toString(),
                name = currentState.name,
                category = currentState.category,
                manufacturer = currentState.manufacturer.takeIf { it.isNotBlank() },
                barcode = currentState.barcode.takeIf { it.isNotBlank() },
                price = currentState.price.toDouble(),
                quantity = currentState.quantity.toIntOrNull() ?: 0,
                tax = currentState.tax.toDoubleOrNull() ?: 0.0,
                isTaxFlatRate = currentState.isTaxFlatRate,
                imagePath = currentState.imagePath.takeIf { it.isNotBlank() },
                userId = "placeholder-user-id",
            )

            if (currentState.isExistingProduct) {
                productRepository.updateProduct(productToSave)
                // Handle archive/restore logic
                if (currentState.isArchived) {
                    productRepository.deleteProduct(productToSave.id, productToSave.userId)
                } else {
                    productRepository.restoreProduct(productToSave.id, productToSave.userId)
                }
            } else {
                productRepository.addProduct(productToSave)
            }

            _uiState.update { it.copy(isSaveSuccessful = true) }
        }
    }

    private fun validateInput(): Boolean {
        val currentState = _uiState.value
        val nameError = if (currentState.name.isBlank()) "Name cannot be empty" else null
        val priceError = if (currentState.price.toDoubleOrNull() == null) "Invalid price" else null

        _uiState.update { it.copy(nameError = nameError, priceError = priceError) }

        return nameError == null && priceError == null
    }
}