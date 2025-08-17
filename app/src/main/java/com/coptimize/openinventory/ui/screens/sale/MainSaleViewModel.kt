package com.coptimize.openinventory.ui.screens.sale

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coptimize.openinventory.data.model.Customer
import com.coptimize.openinventory.data.model.Product
import com.coptimize.openinventory.data.model.SavedCart
import com.coptimize.openinventory.data.repository.CartRepository
import com.coptimize.openinventory.data.repository.CustomerRepository
import com.coptimize.openinventory.data.repository.ProductRepository
import com.coptimize.openinventory.data.repository.SaleRepository
import com.coptimize.openinventory.data.repository.SavedCartRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Represents the state of the screen that is directly managed by this ViewModel,
 * primarily focusing on product lists and search functionality.
 * Cart details are handled by the CartRepository and collected directly in the UI.
 */
data class MainSaleUiState(
    val searchQuery: String = "",
    val allProducts: List<Product> = emptyList(),
    val searchResults: List<Product> = emptyList()
)

/**
 * Represents the payment details and the customer that made the payment.
 * Even when there is no customer information, we still save
 * it to represent a unique walk-in customer.
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
 * This is managed separately from the main UI state.
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
    // The CartRepository is made public so the UI can collect its state flows directly.
    val cartRepository: CartRepository
) : ViewModel() {

    // Internal state for the search query, controlled only by this ViewModel.
    private val _searchQuery = MutableStateFlow("")
    private val _activeSavedCartId = MutableStateFlow<String?>(null)
    private val _activeCustomerId = MutableStateFlow<String?>(null)
    private val _activeCustomerName = MutableStateFlow<String?>(null)
    private val _activeCustomerContact = MutableStateFlow<String?>(null)
    private val _activeCustomerPaymentMethod = MutableStateFlow<String?>(null)

    // This is the main state flow for this ViewModel. It reactively combines
    // the search query and the full product list to produce filtered search results.
    val uiState: StateFlow<MainSaleUiState> = combine(
        _searchQuery,
        productRepository.getAllActiveProducts()
    ) { query, allProducts ->
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

    // These flows handle the transient state of the sale completion event.
    private val _saleCompletionState =
        MutableStateFlow<SaleCompletionState>(SaleCompletionState.Idle)
    val saleCompletionState = _saleCompletionState.asStateFlow()

    private val _lowStockProducts = MutableStateFlow<List<String>>(emptyList())
    val lowStockProducts = _lowStockProducts.asStateFlow()

    // --- UI Event Handlers ---

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onBarcodeScanned(barcode: String) {
        // Use the product list from the current uiState
        val product = uiState.value.allProducts.find { it.barcode == barcode }
        if (product != null) {
            cartRepository.addProductToCart(product)
            _searchQuery.value = "" // Clear search on success
        } else {
            _searchQuery.value = barcode // Show scanned code if not found
        }
    }

    fun onProductSelectedFromSearch(product: Product) {
        cartRepository.addProductToCart(product)
        _searchQuery.value = "" // Clear search after adding to cart
    }

    /**
     * Updates the quantity of an item already in the cart.
     * Delegates the call directly to the CartRepository.
     */
    fun onUpdateCartQuantity(productId: String, quantity: Int) {
        cartRepository.updateItemQuantity(productId, quantity)
    }

    /**
     * Removes an item completely from the cart.
     * Delegates the call directly to the CartRepository.
     */
    fun onRemoveFromCart(productId: String) {
        cartRepository.removeItem(productId)
    }

    // --- New State for Saved Carts ---
    val savedCarts: StateFlow<List<SavedCart>> = savedCartRepository.getActiveCarts()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // --- Updated onSaveCart ---
    fun onSaveCart(customerName: String, customerContact: String?, paymentMethod: String?) {
        viewModelScope.launch {
            val cartSnapshot = cartRepository.getCartSnapshot(0.0, 0.0)
            if (cartSnapshot.items.isEmpty()) return@launch
            _activeCustomerName.value = customerName
            _activeCustomerContact.value = customerContact
            _activeCustomerPaymentMethod.value = paymentMethod
            var customerId: String? = _activeCustomerId.value
            if (customerId == null) {
                customerId =
                    customerRepository.addCustomer(customerName, customerContact, paymentMethod)
                _activeCustomerId.value = customerId
            } else {
                customerRepository.updateCustomer(customerName, customerContact, paymentMethod, customerId)
            }
            val currentCartId = _activeSavedCartId.value
            val userId = "placeholder-user-id"

            if (currentCartId != null) {
                // UPDATE existing saved cart
                savedCartRepository.updateCart(currentCartId, cartSnapshot, customerId, userId)
            } else {
                // INSERT new saved cart
                savedCartRepository.saveCart(cartSnapshot, customerId, userId)
            }
            clearCartAndCustomerInfo()
        }
    }

    fun isRestoredCart(): Boolean {
        val currentCartId = _activeSavedCartId.value
        return (currentCartId != null)
    }

    private fun clearCartAndCustomerInfo() {
        // Clear everything after saving
        cartRepository.clearCart()
        _activeCustomerName.value = null
        _activeCustomerContact.value = null
        _activeCustomerPaymentMethod.value = null
        _activeSavedCartId.value = null
        _activeCustomerId.value = null
    }

    fun onRestoreCart(cartId: String) {
        viewModelScope.launch {
            val items = savedCartRepository.restoreCartItems(cartId)
            if (items != null) {
                cartRepository.clearCart()
                items.first?.forEach { cartItem ->
                    val product = uiState.value.allProducts.find { it.id == cartItem.productId }
                    if (product != null) {
                        cartRepository.addProductToCart(product, cartItem.quantity)
                    }
                }
                // Set the active cart ID
                val customer: Customer? = items.second
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
            savedCartRepository.updateCartStatus(cartId, "cancelled", "placeholder-user-id")
        }
    }

    fun onCompleteSale(details: CheckoutDetails) {
        viewModelScope.launch {
            _saleCompletionState.value = SaleCompletionState.InProgress
            // Step 1: Always create a customer record to capture sale-specific info.
            val customerNameForDb = details.customerName

            val customerId =
                if (_activeCustomerId.value != null) _activeCustomerId.value else customerRepository.addCustomer(
                    name = customerNameForDb,
                    contact = details.customerContact,
                    paymentMethod = details.paymentMethod
                )


            // Step 2: Get the cart snapshot with the final financial details
            val cartSnapshot = cartRepository.getCartSnapshot(details.paidAmount, details.discount)

            if (cartSnapshot.items.isEmpty()) {
                _saleCompletionState.value =
                    SaleCompletionState.Error("Cannot complete sale with an empty cart.")
                return@launch
            }

            // Check for products that will be low on stock *after* this sale.
            val lowStock = uiState.value.allProducts
                .filter { product ->
                    cartSnapshot.items.any { cartItem ->
                        product.id == cartItem.productId && (product.quantity - cartItem.quantity) <= 5
                    }
                }
                .map { it.name }
            _lowStockProducts.value = lowStock

            // Record the sale
            val result = saleRepository.recordSale(cartSnapshot, customerId, "placeholder-user-id")
            result.fold(
                onSuccess = { saleId ->
                    _saleCompletionState.value =
                        SaleCompletionState.Success(saleId, cartSnapshot.changeAmount)
                    val cartId = _activeSavedCartId.value
                    if (cartId != null) {
                        savedCartRepository.updateCartStatus(
                            cartId,
                            "completed",
                            "placeholder-user-id"
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

    /**
     * Resets the sale completion and low stock states, typically after the user
     * has acknowledged any dialogs or notifications.
     */
    fun resetSaleState() {
        _saleCompletionState.value = SaleCompletionState.Idle
        _lowStockProducts.value = emptyList()
    }

    fun getActiveCustomerName(): String? {
        return _activeCustomerName.value
    }

    fun getActiveCustomerContact(): String? {
        return _activeCustomerContact.value
    }

    fun getActiveCustomerPaymentMethod(): String? {
        return _activeCustomerPaymentMethod.value
    }
}