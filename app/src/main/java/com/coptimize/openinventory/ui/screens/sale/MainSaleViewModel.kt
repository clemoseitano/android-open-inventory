package com.coptimize.openinventory.ui.screens.sale

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coptimize.openinventory.data.model.Product
import com.coptimize.openinventory.data.model.SavedCart
import com.coptimize.openinventory.data.repository.CartRepository
import com.coptimize.openinventory.data.repository.CustomerRepository
import com.coptimize.openinventory.data.repository.ProductRepository
import com.coptimize.openinventory.data.repository.SaleRepository
import com.coptimize.openinventory.data.repository.SavedCartRepository
import com.coptimize.openinventory.data.repository.UserSessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Represents the state of the screen that is directly managed by this ViewModel,
 * primarily focusing on product lists and search functionality.
 */
data class MainSaleUiState(
    val searchQuery: String = "",
    val allProducts: List<Product> = emptyList(),
    val searchResults: List<Product> = emptyList()
)

/**
 * Represents the payment details and the customer that made the payment.
 */
data class CheckoutDetails(
    val paidAmount: Double,
    val discount: Double,
    val changeAmount: Double,
    val customerName: String?,
    val customerContact: String?,
    val paymentMethod: String?
)

/**
 * Represents transient, event-like states related to the sale completion process.
 */
sealed class SaleCompletionState {
    object Idle : SaleCompletionState()
    object InProgress : SaleCompletionState()
    data class Success(val saleId: String, val change: Double) : SaleCompletionState()
    data class Error(val message: String) : SaleCompletionState()
}

@HiltViewModel
class MainSaleViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val saleRepository: SaleRepository,
    private val savedCartRepository: SavedCartRepository,
    private val customerRepository: CustomerRepository,
    private val userSessionRepository: UserSessionRepository,
    val cartRepository: CartRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _activeSavedCartId = MutableStateFlow<String?>(null)
    private val _activeCustomerId = MutableStateFlow<String?>(null)
    private val _activeCustomerName = MutableStateFlow<String?>(null)
    private val _activeCustomerContact = MutableStateFlow<String?>(null)
    private val _activeCustomerPaymentMethod = MutableStateFlow<String?>(null)

    private val _navigateToProductManagement = MutableSharedFlow<Boolean>()
    val navigateToProductManagement = _navigateToProductManagement.asSharedFlow()

    val uiState: StateFlow<MainSaleUiState>

    init {
        val productsFlow = productRepository.getAllActiveProducts()

        uiState = combine(_searchQuery, productsFlow) { query, allProducts ->
            val searchResults = if (query.isBlank()) {
                allProducts
            } else {
                allProducts.filter {
                    it.name.contains(query, ignoreCase = true) ||
                            it.barcode?.contains(query, ignoreCase = true) == true
                }
            }
            MainSaleUiState(
                searchQuery = query,
                allProducts = allProducts,
                searchResults = searchResults
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = MainSaleUiState()
        )

        productsFlow
            .onEach { products ->
                if (products.isEmpty()) {
                    _navigateToProductManagement.emit(true)
                }
            }
            .launchIn(viewModelScope)
    }

    private val _saleCompletionState =
        MutableStateFlow<SaleCompletionState>(SaleCompletionState.Idle)
    val saleCompletionState = _saleCompletionState.asStateFlow()

    private val _lowStockProducts = MutableStateFlow<List<String>>(emptyList())
    val lowStockProducts = _lowStockProducts.asStateFlow()

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onBarcodeScanned(barcode: String) {
        val product = uiState.value.allProducts.find { it.barcode == barcode }
        if (product != null) {
            cartRepository.addProductToCart(product)
            _searchQuery.value = ""
        } else {
            _searchQuery.value = barcode
        }
    }

    fun onProductSelectedFromSearch(product: Product) {
        cartRepository.addProductToCart(product)
        _searchQuery.value = ""
    }

    fun onUpdateCartQuantity(productId: String, quantity: Int) {
        cartRepository.updateItemQuantity(productId, quantity)
    }

    fun onRemoveFromCart(productId: String) {
        cartRepository.removeItem(productId)
    }

    val savedCarts: StateFlow<List<SavedCart>> = savedCartRepository.getActiveCarts()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun onSaveCart(customerName: String, customerContact: String?, paymentMethod: String?) {
        viewModelScope.launch {
            val cartSnapshot = cartRepository.getCartSnapshot(0.0, 0.0)
            if (cartSnapshot.items.isEmpty()) return@launch
            _activeCustomerName.value = customerName
            _activeCustomerContact.value = customerContact
            _activeCustomerPaymentMethod.value = paymentMethod
            var customerId: String? = _activeCustomerId.value
            if (customerId == null) {
                customerId = customerRepository.addCustomer(customerName, customerContact, paymentMethod)
                _activeCustomerId.value = customerId
            } else {
                customerRepository.updateCustomer(customerName, customerContact, paymentMethod, customerId)
            }
            val currentCartId = _activeSavedCartId.value
            val userId = userSessionRepository.getCurrentUserId()

            if (currentCartId != null) {
                savedCartRepository.updateCart(currentCartId, cartSnapshot, customerId, userId)
            } else {
                savedCartRepository.saveCart(cartSnapshot, customerId, userId)
            }
            clearCartAndCustomerInfo()
        }
    }

    fun isRestoredCart(): Boolean {
        return _activeSavedCartId.value != null
    }

    private fun clearCartAndCustomerInfo() {
        cartRepository.clearCart()
        _activeCustomerName.value = null
        _activeCustomerContact.value = null
        _activeCustomerPaymentMethod.value = null
        _activeSavedCartId.value = null
        _activeCustomerId.value = null
    }

    fun onRestoreCart(cartId: String) {
        viewModelScope.launch {
            val result = savedCartRepository.restoreCartItems(cartId)
            if (result != null) {
                val (items, customer) = result
                cartRepository.clearCart()
                items?.forEach { cartItem ->
                    val product = uiState.value.allProducts.find { it.id == cartItem.productId }
                    if (product != null) {
                        cartRepository.addProductToCart(product, cartItem.quantity)
                    }
                }
                if (customer != null) {
                    _activeCustomerId.value = customer.id
                    _activeCustomerName.value = customer.name
                    _activeCustomerContact.value = customer.contact
                    _activeCustomerPaymentMethod.value = customer.paymentMethod
                }
                _activeSavedCartId.value = cartId
            }
        }
    }

    fun onCancelCart(cartId: String) {
        viewModelScope.launch {
            savedCartRepository.updateCartStatus(cartId, "cancelled", userSessionRepository.getCurrentUserId())
        }
    }

    fun onCompleteSale(details: CheckoutDetails) {
        viewModelScope.launch {
            _saleCompletionState.value = SaleCompletionState.InProgress
            val customerNameForDb = details.customerName
            val customerId = _activeCustomerId.value ?: customerRepository.addCustomer(
                name = customerNameForDb,
                contact = details.customerContact,
                paymentMethod = details.paymentMethod
            )

            val cartSnapshot = cartRepository.getCartSnapshot(details.paidAmount, details.discount)

            if (cartSnapshot.items.isEmpty()) {
                _saleCompletionState.value = SaleCompletionState.Error("Cannot complete sale with an empty cart.")
                return@launch
            }

            val lowStock = uiState.value.allProducts
                .filter { product ->
                    cartSnapshot.items.any { cartItem ->
                        product.id == cartItem.productId && (product.quantity - cartItem.quantity) <= 5
                    }
                }
                .map { it.name }
            _lowStockProducts.value = lowStock

            val result = saleRepository.recordSale(cartSnapshot, customerId, userSessionRepository.getCurrentUserId())
            result.fold(
                onSuccess = { saleId ->
                    _saleCompletionState.value = SaleCompletionState.Success(saleId, cartSnapshot.changeAmount)
                    val cartId = _activeSavedCartId.value
                    if (cartId != null) {
                        savedCartRepository.updateCartStatus(
                            cartId,
                            "completed",
                            userSessionRepository.getCurrentUserId()
                        )
                    }
                    clearCartAndCustomerInfo()
                },
                onFailure = { ex ->
                    _saleCompletionState.value = SaleCompletionState.Error(
                        ex.message ?: "An unknown error occurred during the sale."
                    )
                }
            )
        }
    }

    fun resetSaleState() {
        _saleCompletionState.value = SaleCompletionState.Idle
        _lowStockProducts.value = emptyList()
    }

    fun getActiveCustomerName(): String? = _activeCustomerName.value
    fun getActiveCustomerContact(): String? = _activeCustomerContact.value
    fun getActiveCustomerPaymentMethod(): String? = _activeCustomerPaymentMethod.value
}