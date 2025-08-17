package com.coptimize.openinventory.ui.screens.sale

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.coptimize.openinventory.data.model.CartItem
import com.coptimize.openinventory.data.model.SavedCart
import com.coptimize.openinventory.ui.screens.AppDrawerContent
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import kotlinx.coroutines.launch

data class CustomerInfo(
    val customerName: String?,
    val customerContact: String?,
    val paymentMethod: String?
)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainSaleScreen(
    windowSizeClass: WindowSizeClass, // Keep for responsive layouts
    navController: NavController,
    viewModel: MainSaleViewModel = hiltViewModel()
) {
    // --- State Collection ---
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val saleCompletionState by viewModel.saleCompletionState.collectAsStateWithLifecycle()
    val lowStockProducts by viewModel.lowStockProducts.collectAsStateWithLifecycle()
    val cartItems by viewModel.cartRepository.items.collectAsStateWithLifecycle()
    val subtotal by viewModel.cartRepository.subtotal.collectAsStateWithLifecycle()
    val tax by viewModel.cartRepository.tax.collectAsStateWithLifecycle()
    val total by viewModel.cartRepository.total.collectAsStateWithLifecycle()
    val savedCarts by viewModel.savedCarts.collectAsStateWithLifecycle()

    var showCheckoutDialog by remember { mutableStateOf(false) }
    var showSaveCartDialog by remember { mutableStateOf(false) }
    var showSavedCartsList by remember { mutableStateOf(false) }

    // --- State and scope for the navigation drawer ---
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // --- Activity Result Launchers ---
    val barcodeLauncher = rememberLauncherForActivityResult(ScanContract()) { result ->
        result.contents?.let { viewModel.onBarcodeScanned(it) }
    }

    // --- Event Handling Side Effects ---
    HandleSaleCompletion(
        saleState = saleCompletionState,
        lowStockProducts = lowStockProducts,
        onDismiss = viewModel::resetSaleState
    )

    // --- UI Structure with Navigation Drawer ---
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawerContent(
                navController = navController,
                closeDrawer = { scope.launch { drawerState.close() } }
            )
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Point of Sale") },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, "Menu")
                        }
                    },
                    actions = {
                        // --- Updated Save Cart Icon with Badge ---
                        BadgedBox(
                            badge = {
                                if (savedCarts.isNotEmpty()) {
                                    Badge { Text("${savedCarts.size}") }
                                }
                            }
                        ) {
                            Row {
                                IconButton(
                                    onClick = { showSaveCartDialog = true },
                                    enabled = cartItems.isNotEmpty()
                                ) {
                                    Icon(
                                        Icons.Default.Save,
                                        contentDescription = "Save Current Cart"
                                    )
                                }
                                IconButton(
                                    onClick = { showSavedCartsList = true },
                                    enabled = savedCarts.isNotEmpty()
                                ) {
                                    Icon(
                                        Icons.Default.FolderOpen,
                                        contentDescription = "Open Saved Carts"
                                    )
                                }
                            }
                        }
                    }
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
            ) {
                // --- Main Content (Cart + Summary), sits at the bottom layer ---
                Column(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxSize()
                ) {
                    // Spacer to leave room for the SearchBar which will be placed on top
                    Spacer(Modifier.height(80.dp))

                    if (cartItems.isEmpty()) {
                        EmptyCartView(modifier = Modifier.weight(1f))
                    } else {
                        CartView(
                            modifier = Modifier.weight(1f),
                            cartItems = cartItems,
                            onRemoveClick = viewModel::onRemoveFromCart,
                            onQuantityChange = viewModel::onUpdateCartQuantity
                        )
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    SaleSummary(
                        subtotal = subtotal,
                        tax = tax,
                        total = total,
                        onCompleteSale = { showCheckoutDialog = true },
                        isCartEmpty = cartItems.isEmpty()
                    )
                }

                // --- Search UI (SearchBar + Results), sits on the top layer ---
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    SearchBar(
                        query = uiState.searchQuery,
                        onQueryChange = viewModel::onSearchQueryChanged,
                        onBarcodeClick = {
                            barcodeLauncher.launch(ScanOptions().apply {
                                setPrompt("Scan a barcode")
                                setBeepEnabled(true)
                            })
                        }
                    )

                    AnimatedVisibility(visible = uiState.searchResults.isNotEmpty() && uiState.searchQuery.isNotBlank()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(8.dp)
                        ) {
                            LazyColumn(
                                modifier = Modifier
                                    .heightIn(max = 240.dp) // Limit the height of the results list
                            ) {
                                items(uiState.searchResults, key = { it.id }) { product ->
                                    ListItem(
                                        headlineContent = { Text(product.name) },
                                        supportingContent = { Text("In Stock: ${product.quantity}") },
                                        modifier = Modifier.clickable {
                                            viewModel.onProductSelectedFromSearch(
                                                product
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCheckoutDialog) {
        CheckoutDialog(
            totalAmount = total,
            activeCustomerInfo = if(viewModel.isRestoredCart()) CustomerInfo(
                customerName = viewModel.getActiveCustomerName(),
                customerContact = viewModel.getActiveCustomerContact(),
                paymentMethod = viewModel.getActiveCustomerPaymentMethod()
            ) else null,
            onDismiss = { showCheckoutDialog = false },
            onConfirm = { checkoutDetails ->
                showCheckoutDialog = false
                viewModel.onCompleteSale(checkoutDetails)
            }
        )
    }
    if (showSaveCartDialog) {
        SaveCartDialog(
            isUpdate = viewModel.isRestoredCart(),
            activeCustomerInfo = if(viewModel.isRestoredCart()) CustomerInfo(
                customerName = viewModel.getActiveCustomerName(),
                customerContact = viewModel.getActiveCustomerContact(),
                paymentMethod = viewModel.getActiveCustomerPaymentMethod()
            ) else null,
            onDismiss = { showSaveCartDialog = false },
            onConfirm = { customerName, customerContact, paymentMethod ->
                showSaveCartDialog = false
                viewModel.onSaveCart(customerName, customerContact, paymentMethod)
            }
        )
    }

    if (showSavedCartsList) {
        SavedCartsBottomSheet(
            savedCarts = savedCarts,
            onDismiss = { showSavedCartsList = false },
            onRestore = { cartId ->
                showSavedCartsList = false
                viewModel.onRestoreCart(cartId)
            },
            onCancel= { cartId ->
                showSavedCartsList = false
                viewModel.onCancelCart(cartId)
            }
        )
    }
}

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onBarcodeClick: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }

    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        label = { Text("Scan or type to search...") },
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
            .focusRequester(focusRequester),
        trailingIcon = {
            IconButton(onClick = onBarcodeClick) {
                Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan Barcode")
            }
        },
        singleLine = true
    )

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}

@Composable
fun QuantityStepper(
    item: CartItem,
    onQuantityChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var quantityText by remember { mutableStateOf(item.quantity.toString()) }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(item.quantity) {
        if (quantityText.toIntOrNull() != item.quantity) {
            quantityText = item.quantity.toString()
        }
    }

    val commitChange = {
        val newQuantity = quantityText.toIntOrNull() ?: 1
        onQuantityChange(newQuantity.coerceIn(0, item.maxStock))
        focusManager.clearFocus()
    }

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(
            onClick = { onQuantityChange(item.quantity + 1) },
            enabled = item.quantity < item.maxStock,
            modifier = Modifier.size(24.dp)
        ) {
            Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Increase quantity")
        }

        BasicTextField(
            value = quantityText,
            onValueChange = { newText ->
                quantityText = newText.filter { it.isDigit() }.let {
                    if (it.length > 1 && it.startsWith('0')) it.drop(1) else it
                }
            },
            modifier = Modifier.width(40.dp),
            textStyle = LocalTextStyle.current.copy(
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = { commitChange() }),
            singleLine = true
        )

        IconButton(
            onClick = { onQuantityChange(item.quantity - 1) },
            modifier = Modifier.size(24.dp)
        ) {
            Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Decrease quantity")
        }
    }
}

@Composable
fun CartView(
    modifier: Modifier = Modifier,
    cartItems: List<CartItem>,
    onRemoveClick: (String) -> Unit,
    onQuantityChange: (String, Int) -> Unit
) {
    LazyColumn(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(cartItems, key = { it.productId }) { item ->
            ListItem(
                headlineContent = { Text(item.name) },
                supportingContent = {
                    val itemTotal = item.price * item.quantity
                    Text("€${"%.2f".format(item.price)}  —  Total: €${"%.2f".format(itemTotal)}")
                },
                trailingContent = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        QuantityStepper(
                            item = item,
                            onQuantityChange = { newQuantity ->
                                onQuantityChange(item.productId, newQuantity)
                            }
                        )
                        IconButton(onClick = { onRemoveClick(item.productId) }) {
                            Icon(Icons.Default.DeleteOutline, "Remove item from cart")
                        }
                    }
                }
            )
            HorizontalDivider()
        }
    }
}

@Composable
fun SaleSummary(
    subtotal: Double,
    tax: Double,
    total: Double,
    onCompleteSale: () -> Unit,
    isCartEmpty: Boolean
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Subtotal")
            Text("€${"%.2f".format(subtotal)}")
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Tax")
            Text("€${"%.2f".format(tax)}")
        }
        HorizontalDivider()
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                "Total",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                "€${"%.2f".format(total)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = onCompleteSale,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isCartEmpty
        ) {
            Text("Complete Sale")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutDialog(
    totalAmount: Double,
    activeCustomerInfo: CustomerInfo?,
    onDismiss: () -> Unit,
    onConfirm: (CheckoutDetails) -> Unit
) {
    val paymentMethods = listOf("Cash", "Mobile Money", "Bank Card")
    // Use the active cart's data as the initial state if we are updating
    var customerName by remember { mutableStateOf(activeCustomerInfo?.customerName ?: "") }
    var customerContact by remember { mutableStateOf(activeCustomerInfo?.customerContact ?: "") }
    var paymentMethod by remember { mutableStateOf(activeCustomerInfo?.paymentMethod ?: paymentMethods.first()) }
    // This effect ensures that if the dialog recomposes for any reason,
    // the state isn't reset. It only sets the initial values once.
    LaunchedEffect(activeCustomerInfo) {
        if (activeCustomerInfo != null) {
            customerName = activeCustomerInfo.customerName ?: ""
            customerContact = activeCustomerInfo.customerContact ?: ""
            paymentMethod = activeCustomerInfo.paymentMethod ?: paymentMethods.first()
        }
    }
    var isPaymentDropdownExpanded by remember { mutableStateOf(false) }
    var discountString by remember { mutableStateOf("0.0") }
    var amountTenderedString by remember { mutableStateOf(String.format("%.2f", totalAmount)) }

    val discount = discountString.toDoubleOrNull() ?: 0.0
    val displayTotal = (totalAmount - discount).coerceAtLeast(0.0)
    val amountTendered = amountTenderedString.toDoubleOrNull() ?: 0.0
    val changeDue = (amountTendered - displayTotal).coerceAtLeast(0.0)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Checkout") },
        text = {
            LazyColumn {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            "Final Amount Due: €${"%.2f".format(displayTotal)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        HorizontalDivider()
                        Text(
                            "Customer Details (Optional)",
                            style = MaterialTheme.typography.titleSmall
                        )
                        OutlinedTextField(
                            value = customerName,
                            onValueChange = { customerName = it },
                            label = { Text("Customer Name") })
                        OutlinedTextField(
                            value = customerContact,
                            onValueChange = { customerContact = it },
                            label = { Text("Customer Contact") })
                        Text("Payment Details", style = MaterialTheme.typography.titleSmall)
                        ExposedDropdownMenuBox(
                            expanded = isPaymentDropdownExpanded,
                            onExpandedChange = {
                                isPaymentDropdownExpanded = !isPaymentDropdownExpanded
                            }) {
                            OutlinedTextField(
                                value = paymentMethod,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Payment Method") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isPaymentDropdownExpanded) },
                                modifier = Modifier.menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = isPaymentDropdownExpanded,
                                onDismissRequest = { isPaymentDropdownExpanded = false }) {
                                paymentMethods.forEach { method ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                method
                                            )
                                        },
                                        onClick = {
                                            paymentMethod = method; isPaymentDropdownExpanded =
                                            false
                                        })
                                }
                            }
                        }
                        OutlinedTextField(
                            value = discountString,
                            onValueChange = { discountString = it },
                            label = { Text("Discount") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        AnimatedVisibility(visible = paymentMethod == "Cash") {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                OutlinedTextField(
                                    value = amountTenderedString,
                                    onValueChange = { amountTenderedString = it },
                                    label = { Text("Amount Tendered") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                )
                                Text(
                                    "Change Due: €${"%.2f".format(changeDue)}",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalPaidAmount =
                        if (paymentMethod == "Cash") amountTendered else displayTotal
                    val finalChangeAmount = if (paymentMethod == "Cash") changeDue else 0.0
                    val details = CheckoutDetails(
                        paidAmount = finalPaidAmount,
                        changeAmount = finalChangeAmount,
                        discount = discount,
                        customerName = customerName.takeIf { it.isNotBlank() },
                        customerContact = customerContact.takeIf { it.isNotBlank() },
                        paymentMethod = paymentMethod
                    )
                    onConfirm(details)
                },
                enabled = !(paymentMethod == "Cash" && amountTendered < displayTotal)
            ) { Text("Confirm Sale") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun HandleSaleCompletion(
    saleState: SaleCompletionState,
    lowStockProducts: List<String>,
    onDismiss: () -> Unit
) {
    var showLowStockAlert by remember { mutableStateOf(false) }
    LaunchedEffect(saleState, lowStockProducts) {
        if (saleState is SaleCompletionState.Success && lowStockProducts.isNotEmpty()) {
            showLowStockAlert = true
        } else if (saleState is SaleCompletionState.Idle) {
            showLowStockAlert = false
        }
    }
    when (saleState) {
        is SaleCompletionState.Success -> {
            AlertDialog(
                onDismissRequest = onDismiss,
                icon = { Icon(Icons.Default.CheckCircle, null) },
                title = { Text("Sale Completed") },
                text = {
                    Text(
                        "Sale ID: ${saleState.saleId.takeLast(6)}\nChange Due: €${
                            "%.2f".format(
                                saleState.change
                            )
                        }"
                    )
                },
                confirmButton = { TextButton(onClick = { /* TODO */ }) { Text("Print Receipt") } },
                dismissButton = { TextButton(onClick = onDismiss) { Text("Done") } }
            )
        }

        is SaleCompletionState.Error -> {
            AlertDialog(
                onDismissRequest = onDismiss,
                icon = { Icon(Icons.Default.Error, null) },
                title = { Text("Sale Failed") },
                text = { Text(saleState.message) },
                confirmButton = { TextButton(onClick = onDismiss) { Text("OK") } }
            )
        }

        else -> {}
    }
    if (showLowStockAlert) {
        AlertDialog(
            onDismissRequest = { showLowStockAlert = false }, title = { Text("Low Stock Warning") },
            text = {
                Text(
                    "The following products are now low on stock:\n\n${
                        lowStockProducts.joinToString(
                            "\n"
                        )
                    }"
                )
            },
            confirmButton = { TextButton(onClick = { showLowStockAlert = false }) { Text("OK") } }
        )
    }
}

@Composable
fun EmptyCartView(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            "Scan an item or search to begin a new sale.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaveCartDialog(
    isUpdate: Boolean,
    activeCustomerInfo: CustomerInfo?,
    onDismiss: () -> Unit,
    onConfirm: (customerName: String, customerContact: String?, paymentMethod: String) -> Unit
) {
    val paymentMethods = listOf("Cash", "Mobile Money", "Bank Card")
    // Use the active cart's data as the initial state if we are updating
    var customerName by remember { mutableStateOf(if (isUpdate) activeCustomerInfo?.customerName ?: "" else "") }
    var customerContact by remember { mutableStateOf(if (isUpdate) activeCustomerInfo?.customerContact ?: "" else "") }
    var paymentMethod by remember { mutableStateOf(if (isUpdate) activeCustomerInfo?.paymentMethod ?: paymentMethods.first() else paymentMethods.first()) }
    // This effect ensures that if the dialog recomposes for any reason,
    // the state isn't reset. It only sets the initial values once.
    LaunchedEffect(activeCustomerInfo) {
        if (isUpdate && activeCustomerInfo != null) {
            customerName = activeCustomerInfo.customerName ?: ""
            customerContact = activeCustomerInfo.customerContact ?: ""
            paymentMethod = activeCustomerInfo.paymentMethod ?: paymentMethods.first()
        }
    }
    var isDropdownExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isUpdate) "Update Cart" else "Save Cart") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Enter customer details to identify this cart later.")
                OutlinedTextField(
                    value = customerName,
                    onValueChange = { customerName = it; },
                    label = { Text("Customer Name") },
                )
                OutlinedTextField(
                    value = customerContact,
                    onValueChange = { customerContact = it },
                    label = { Text("Customer Contact (Optional)") }
                )

                // --- ADDED PAYMENT METHOD DROPDOWN ---
                ExposedDropdownMenuBox(
                    expanded = isDropdownExpanded,
                    onExpandedChange = { isDropdownExpanded = !isDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = paymentMethod,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Expected Payment Method") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDropdownExpanded) },
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = isDropdownExpanded,
                        onDismissRequest = { isDropdownExpanded = false }
                    ) {
                        paymentMethods.forEach { method ->
                            DropdownMenuItem(
                                text = { Text(method) },
                                onClick = {
                                    paymentMethod = method
                                    isDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                onConfirm(
                    customerName,
                    customerContact.takeIf { it.isNotBlank() },
                    paymentMethod
                )
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedCartsBottomSheet(
    savedCarts: List<SavedCart>,
    onDismiss: () -> Unit,
    onRestore: (String) -> Unit,
    onCancel: (String) -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Pending Carts",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(savedCarts, key = { it.id }) { cart ->
                    ListItem(
                        headlineContent = { Text(cart.customerId ?: "Unknown") },
                        supportingContent = { Text("Saved on: ${cart.createdAt}") },
                        trailingContent = {
                            Row { Button(onClick = { onRestore(cart.id) }) {
                                Text("Restore")
                            }
                                IconButton(onClick = { onCancel(cart.id) }) {
                                Icon(Icons.Default.DeleteOutline, "Remove item from cart")
                            }
                            }
                        }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}