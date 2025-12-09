package com.coptimize.openinventory.ui.screens.product

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coptimize.openinventory.data.model.Product
import com.coptimize.openinventory.data.model.Stock
import com.coptimize.openinventory.data.repository.ProductAnalysisRepository
import com.coptimize.openinventory.data.repository.ProductDiscoveryRepository
import com.coptimize.openinventory.data.repository.ProductRepository
import com.coptimize.openinventory.data.repository.UserSessionRepository
import com.coptimize.openinventory.ui.formatAsDateForDatabaseQuery
import com.coptimize.openinventory.ui.formatDateForDisplay
import com.coptimize.openinventory.ui.stringToDate
import com.coptimize.openinventory.worker.DiscoveryScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.util.Date
import javax.inject.Inject

data class ProductEditUiState(
    val isLoading: Boolean = true,
    val isExistingProduct: Boolean = false,
    val isSaveSuccessful: Boolean = false,
    val isAnalyzing: Boolean = false,

    // Item Details
    val name: String = "",
    val barcode: String = "",
    val category: String = "",
    val manufacturer: String = "",
    val supplier: String = "",
    val supplierContact: String = "",

    // Location
    val storeSection: String = "",
    val shelfAisle: String = "",

    // Pricing & Stock
    val price: String = "",
    val quantityToAdd: String = "0",
    val quantityInStock: Int = 0,
    val purchasePrice: String = "",
    val unitPrice: String = "",
    val tax: String = "",
    val isTaxFlatRate: Boolean = false,

    // Dates
    val purchaseDate: Date = Date(),
    val expiryDate: Date? = null,

    // Meta
    val imagePath: String = "", // Still used for the "Primary" display image
    val capturedImagePaths: List<String> = emptyList(), // NEW: Holds all captured images
    val isArchived: Boolean = false,

    // Validation
    val nameError: String? = null,
    val priceError: String? = null
)

@HiltViewModel
class ProductEditViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val analysisRepository: ProductAnalysisRepository,
    private val discoveryRepository: ProductDiscoveryRepository,
    private val userSessionRepository: UserSessionRepository,
    savedStateHandle: SavedStateHandle,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val productId: String? = savedStateHandle["productId"]

    private val _uiState = MutableStateFlow(ProductEditUiState())
    val uiState = _uiState.asStateFlow()

    init {
        if (productId != null && productId != "-1") {
            loadProduct(productId)
        } else {
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
                if (stock != null) {
                    _uiState.update {
                        it.copy(
                            supplier = stock.supplier ?: "",
                            supplierContact = stock.supplierContact ?: "",
                        )
                    }
                    if (!stock.purchaseDate.isNullOrEmpty()) {
                        _uiState.update { it.copy(purchaseDate = stringToDate(stock.purchaseDate)) }
                    }
                    if (!stock.expiryDate.isNullOrEmpty()) {
                        _uiState.update { it.copy(expiryDate = stringToDate(stock.expiryDate)) }
                    }
                    if (stock.purchasePrice != null) {
                        _uiState.update { it.copy(purchasePrice = stock.purchasePrice.toString()) }
                    }
                }
            } else {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    // ... (Keep simple field handlers: onNameChange, onBarcodeChange, etc.) ...
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

    fun onImageSelected(uri: Uri?) {
        if (uri == null) return
        viewModelScope.launch {
            val imagePath = saveImageToInternalStorage(uri)
            if (imagePath != null) {
                // If selecting from gallery, we set it as primary AND add it to the list
                _uiState.update {
                    it.copy(
                        imagePath = imagePath,
                        capturedImagePaths = it.capturedImagePaths + imagePath
                    )
                }
            }
        }
    }

    // Updated to handle list of images
    fun onEnrollmentComplete(barcode: String?, imageUris: List<Uri>) {
        _uiState.update {
            it.copy(
                barcode = barcode ?: it.barcode,
                // Set the first image as the primary one for UI display
                imagePath = imageUris.firstOrNull()?.path ?: it.imagePath,
                // Store ALL paths for the upload logic
                capturedImagePaths = imageUris.mapNotNull { uri -> uri.path }
            )
        }
    }

    fun saveProduct() {
        if (!validateInput()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val currentState = _uiState.value
            val quantityChange = currentState.quantityToAdd.toIntOrNull() ?: 0
            val isNewProduct = !currentState.isExistingProduct
            val finalProductId = if (isNewProduct) "" else productId!!
            val currentUserId = userSessionRepository.getCurrentUserId()

            // 1. Save Product to Local DB
            val productToSave = Product(
                id = finalProductId,
                name = currentState.name,
                barcode = currentState.barcode.takeIf { it.isNotBlank() },
                category = currentState.category,
                manufacturer = currentState.manufacturer.takeIf { it.isNotBlank() },
                price = currentState.price.toDoubleOrNull() ?: 0.0,
                quantity = currentState.quantityInStock + quantityChange,
                tax = currentState.tax.toDoubleOrNull() ?: 0.0,
                isTaxFlatRate = currentState.isTaxFlatRate,
                imagePath = currentState.imagePath.takeIf { it.isNotBlank() },
                deletedAt = if (currentState.isArchived) "archived" else null,
                shelf = currentState.shelfAisle.takeIf { it.isNotBlank() },
                section = currentState.storeSection.takeIf { it.isNotBlank() },
                userId = currentUserId
            )

            val stockEvent = Stock(
                id = "",
                productId = finalProductId,
                supplier = currentState.supplier.takeIf { it.isNotBlank() },
                supplierContact = currentState.supplierContact.takeIf { it.isNotBlank() },
                purchasePrice = currentState.purchasePrice.toDoubleOrNull() ?: 0.0,
                purchaseDate = currentState.purchaseDate.time.formatAsDateForDatabaseQuery(),
                expiryDate = if (currentState.expiryDate != null) formatDateForDisplay(currentState.expiryDate, format = "yyyy-MM-dd HH:mm:ss") else "",
                quantity = quantityChange,
                userId = currentUserId,
                unitPrice = currentState.unitPrice.toDoubleOrNull() ?: 0.0,
            )

            var savedProductId = finalProductId
            if (isNewProduct) {
                savedProductId = productRepository.addProduct(productToSave)
                if (savedProductId.isNotBlank()) {
                    val newStock: Stock = stockEvent.copy(productId = savedProductId)
                    productRepository.addStock(newStock)
                }
            } else {
                productRepository.updateProductAndStock(product = productToSave, stock = stockEvent)
                if (currentState.isArchived && productToSave.deletedAt == null) {
                    productRepository.deleteProduct(productToSave.id, currentUserId)
                } else if (!currentState.isArchived && productToSave.deletedAt != null) {
                    productRepository.restoreProduct(productToSave.id, currentUserId)
                }
            }

            // 2. Trigger Discovery Logic (Using all captured images)

            // Determine which images to send.
            // If we captured multiple in this session, use those.
            // If we didn't capture new ones but there's a primary image (e.g. existing product), send that.
            val imagesToSend = if (currentState.capturedImagePaths.isNotEmpty()) {
                currentState.capturedImagePaths
            } else if (!currentState.imagePath.isNullOrBlank()) {
                listOf(currentState.imagePath)
            } else {
                emptyList()
            }

            if (imagesToSend.isNotEmpty()) {
                triggerDiscoveryProcess(savedProductId, imagesToSend)
            }

            _uiState.update { it.copy(isSaveSuccessful = true, isLoading = false) }
        }
    }

    private suspend fun triggerDiscoveryProcess(productId: String, imagePaths: List<String>) {
        try {
            // Convert strings paths to URIs
            val uris = imagePaths.map { path ->
                val file = File(path)
                if (file.exists()) Uri.fromFile(file) else Uri.parse(path)
            }

            // A. Upload Images & Get Task ID
            val taskId = analysisRepository.performRemoteOcrAndInference(uris)

            // B. Save Task locally
            discoveryRepository.saveTask(
                productId = productId,
                taskId = taskId,
                stockId = null
            )

            // C. Schedule Worker
            DiscoveryScheduler.scheduleTaskMonitoring(
                context = context,
                taskId = taskId,
                productId = productId,
                stockId = null
            )

            Log.i("CLEMENT", "Discovery started for Product $productId with Task $taskId using ${uris.size} images")

        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("CLEMENT", "Failed to initiate discovery: ${e.message}")
        }
    }

    private fun validateInput(): Boolean {
        val currentState = _uiState.value
        val nameError = if (currentState.name.isBlank()) "Name cannot be empty" else null
        val priceError = if (currentState.price.toDoubleOrNull() == null) "Invalid price" else null
        _uiState.update { it.copy(nameError = nameError, priceError = priceError) }
        return nameError == null && priceError == null
    }

    private fun saveImageToInternalStorage(uri: Uri): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val imagesDir = File(context.filesDir, "images")
            if (!imagesDir.exists()) imagesDir.mkdir()
            val file = File(imagesDir, "${System.currentTimeMillis()}.jpg")
            val outputStream = FileOutputStream(file)
            inputStream.copyTo(outputStream)
            inputStream.close()
            outputStream.close()
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}