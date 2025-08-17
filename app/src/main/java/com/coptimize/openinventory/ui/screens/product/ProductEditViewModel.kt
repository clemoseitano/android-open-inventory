package com.coptimize.openinventory.ui.screens.product

import android.content.Context
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coptimize.openinventory.data.model.Product
import com.coptimize.openinventory.data.model.Stock
import com.coptimize.openinventory.data.repository.ProductRepository
import com.coptimize.openinventory.ui.formatAsDateForDatabaseQuery
import com.coptimize.openinventory.ui.formatDateForDisplay
import com.coptimize.openinventory.ui.stringToDate
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.util.Date
import java.util.UUID
import javax.inject.Inject

// Represents the state of all fields on the screen
data class ProductEditUiState(
    val isLoading: Boolean = true,
    val isExistingProduct: Boolean = false,
    val isSaveSuccessful: Boolean = false,

    // Item Details
    val name: String = "",
    val barcode: String = "",
    val category: String = "", // itemClassLineEdit
    val manufacturer: String = "",
    val supplier: String = "",
    val supplierContact: String = "",

    // Location
    val storeSection: String = "",
    val shelfAisle: String = "",

    // Pricing & Stock
    val price: String = "",
    val quantityToAdd: String = "0", // itemQtyLineEdit, represents the new stock being added
    val quantityInStock: Int = 0, // quantityInStockLabel
    val purchasePrice: String = "",
    val unitPrice: String = "",
    val tax: String = "",
    val isTaxFlatRate: Boolean = false,

    // Dates
    val purchaseDate: Date = Date(), // dateTimeEdit
    val expiryDate: Date? = null,    // expiryDateEdit

    // Meta
    val imagePath: String = "",
    val isArchived: Boolean = false,

    // Validation
    val nameError: String? = null,
    val priceError: String? = null
)

@HiltViewModel
class ProductEditViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    savedStateHandle: SavedStateHandle,
    @ApplicationContext private val context: Context,
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
            val stock = productRepository.getLastStockForProduct(id)
            if (product != null) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isExistingProduct = true,
                        name = product.name,
                        barcode = product.barcode ?: "",
                        category = product.category,
                        manufacturer = product.manufacturer ?: "",
                        price = product.price.toString(),
                        quantityInStock = product.quantity,
                        tax = product.tax?.toString() ?: "0.0",
                        isTaxFlatRate = product.isTaxFlatRate,
                        imagePath = product.imagePath ?: "",
                        isArchived = product.deletedAt != null
                    )
                }
                if (stock != null){
                    _uiState.update{
                        it.copy(
                            supplier = stock.supplier?:"",
                            supplierContact = stock.supplierContact?:"",
                        )
                    }
                    if (stock.purchaseDate !=null){
                        _uiState.update { it.copy(purchaseDate = stringToDate(stock.purchaseDate)) }
                    }
                    if (stock.expiryDate !=null){
                        _uiState.update { it.copy(expiryDate = stringToDate(stock.expiryDate)) }
                    }
                    if (stock.purchasePrice != null){
                        _uiState.update { it.copy(purchasePrice = stock.purchasePrice.toString()) }
                    }
                }
            } else {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    // --- Event Handlers for ALL UI fields ---
    fun onNameChange(value: String) = _uiState.update { it.copy(name = value, nameError = null) }
    fun onBarcodeChange(value: String) = _uiState.update { it.copy(barcode = value) }
    fun onCategoryChange(value: String) = _uiState.update { it.copy(category = value) }
    fun onManufacturerChange(value: String) = _uiState.update { it.copy(manufacturer = value) }
    fun onSupplierChange(value: String) = _uiState.update { it.copy(supplier = value) }
    fun onSupplierContactChange(value: String) = _uiState.update { it.copy(supplierContact = value) }
    fun onStoreSectionChange(value: String) = _uiState.update { it.copy(storeSection = value) }
    fun onShelfAisleChange(value: String) = _uiState.update { it.copy(shelfAisle = value) }
    fun onPriceChange(value: String) = _uiState.update { it.copy(price = value, priceError = null) }
    fun onQuantityToAddChange(value: String) = _uiState.update { it.copy(quantityToAdd = value) }
    fun onPurchasePriceChange(value: String) = _uiState.update { it.copy(purchasePrice = value) }
    fun onTaxChange(value: String) = _uiState.update { it.copy(tax = value) }
    fun onTaxTypeChange(isFlat: Boolean) = _uiState.update { it.copy(isTaxFlatRate = isFlat) }
    fun onPurchaseDateChange(date: Date) = _uiState.update { it.copy(purchaseDate = date) }
    fun onExpiryDateChange(date: Date?) = _uiState.update { it.copy(expiryDate = date) }
    fun onArchivedChange(isArchived: Boolean) = _uiState.update { it.copy(isArchived = isArchived) }

    fun saveProduct() {
        if (!validateInput()) return

        viewModelScope.launch {
            val currentState = _uiState.value
            val quantityChange = currentState.quantityToAdd.toIntOrNull() ?: 0
            val isNewProduct = !currentState.isExistingProduct

            // Generate a new ID for the product only if it's new
            var finalProductId = if (isNewProduct) "" else productId!!

            // In a real app, get this from a UserSession repository
            val currentUserId = "placeholder-user-id"

            // 1. Create the Product and Stock objects to save/update
            val productToSave = Product(
                id = finalProductId,
                name = currentState.name,
                barcode = currentState.barcode.takeIf { it.isNotBlank() },
                category = currentState.category,
                manufacturer = currentState.manufacturer.takeIf { it.isNotBlank() },
                price = currentState.price.toDoubleOrNull() ?: 0.0,
                quantity = currentState.quantityInStock + quantityChange, // The NEW total quantity
                tax = currentState.tax.toDoubleOrNull() ?: 0.0,
                isTaxFlatRate = currentState.isTaxFlatRate,
                imagePath = currentState.imagePath.takeIf { it.isNotBlank() },
                deletedAt = if (currentState.isArchived) "archived" else null, // Example
                shelf = currentState.shelfAisle.takeIf { it.isNotBlank() },
                section = currentState.storeSection.takeIf { it.isNotBlank() },
                userId = currentUserId
            )

            // 3. Create a new Stock log entry IF a positive quantity was added.
            // This logs the specific purchase event.
//            if (quantityChange > 0) { We allow updates to other fields
            val stockEvent = Stock(
                id = "",
                productId = finalProductId,
                supplier = currentState.supplier.takeIf { it.isNotBlank() },
                supplierContact = currentState.supplierContact.takeIf { it.isNotBlank() },
                purchasePrice = currentState.purchasePrice.toDoubleOrNull() ?: 0.0,
                purchaseDate = currentState.purchaseDate.time.formatAsDateForDatabaseQuery(),
                expiryDate = if (currentState.expiryDate!=null) formatDateForDisplay(
                    currentState.expiryDate,
                    format = "yyyy-MM-dd HH:mm:ss",
                ) else "",
                quantity = quantityChange,
                userId = currentUserId,
                unitPrice = currentState.unitPrice.toDoubleOrNull()?:0.0,
            )
//            }

            // 2. Perform the Product table operation (either create or update)
            if (isNewProduct) {
                // For a new product, we simply add it with its initial quantity.
                finalProductId = productRepository.addProduct(productToSave)
                if (finalProductId.isNotBlank()){
                    val newStock: Stock =  stockEvent.copy(productId=finalProductId)
                    productRepository.addStock(newStock)
                }
            } else {
                // For an existing product, we call the specialized update query
                // that handles incrementing the stock quantity atomically.
                productRepository.updateProductAndStock(product=productToSave, stock=stockEvent)

                // Also handle archive/restore logic if needed
                if (currentState.isArchived && productToSave.deletedAt == null) {
                    productRepository.deleteProduct(productToSave.id, currentUserId)
                } else if (!currentState.isArchived && productToSave.deletedAt != null) {
                    productRepository.restoreProduct(productToSave.id, currentUserId)
                }
            }

            // 4. Signal to the UI that the save was successful.
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

    fun onImageSelected(uri: Uri?) {
        if (uri == null) return // User cancelled the picker

        viewModelScope.launch {
            // Copy the image from the content URI to our app's private storage
            val imagePath = saveImageToInternalStorage(uri)
            if (imagePath != null) {
                // Update the UI state with the path to our new, private copy
                _uiState.update { it.copy(imagePath = imagePath) }
            }
        }
    }

    /**
     * Copies a file from a given content URI to the app's internal files directory.
     * This ensures the app has permanent access to the image.
     * @return The absolute path to the newly created file, or null on failure.
     */
    private fun saveImageToInternalStorage(uri: Uri): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null

            // Create a file in the app's private "images" directory
            val imagesDir = File(context.filesDir, "images")
            if (!imagesDir.exists()) {
                imagesDir.mkdir()
            }
            // Create a unique file name using the current time
            val file = File(imagesDir, "${System.currentTimeMillis()}.jpg")

            val outputStream = FileOutputStream(file)
            inputStream.copyTo(outputStream)

            inputStream.close()
            outputStream.close()

            // Return the permanent path to our copy of the image
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}