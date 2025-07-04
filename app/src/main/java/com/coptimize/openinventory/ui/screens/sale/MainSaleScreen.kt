package com.coptimize.openinventory.ui.screens.sale

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.coptimize.openinventory.data.CartItem
import com.coptimize.openinventory.data.model.Product
import com.coptimize.openinventory.data.SampleData
import com.coptimize.openinventory.navigation.Screen
import kotlinx.coroutines.launch
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainSaleScreen(windowSizeClass: WindowSizeClass,
                   navController: NavController,
                   viewModel: MainSaleViewModel = hiltViewModel()
) {
    val isHandset = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // State for the screen
    val products by viewModel.products.collectAsStateWithLifecycle()
    // This will be a placeholder for now until we build the CartRepository
    var cartItems by remember { mutableStateOf(SampleData.cartItems) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = isHandset, // Only allow gestures on handset
        drawerContent = {
            ModalDrawerSheet {
                AppDrawerContent(navController) {
                    scope.launch { drawerState.close() }
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Point of Sale") },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    },
                    actions = {
                        if (isHandset) {
                            // On handset, show a cart icon to navigate to a dedicated cart screen
                            BadgedBox(
                                badge = { Badge { Text("${cartItems.size}") } }
                            ) {
                                IconButton(onClick = { /* TODO: Navigate to a dedicated CartScreen */ }) {
                                    Icon(Icons.Default.ShoppingCart, "Cart")
                                }
                            }
                        }
                    }
                )
            }
        ) { paddingValues ->
            if (isHandset) {
                MainSaleHandsetLayout(
                    modifier = Modifier.padding(paddingValues),
                    products = products, // Use the real products list
                    onAddToCart = { product ->
                        // Call the ViewModel function
                        viewModel.onAddToCart(product)
                    }
                )
            } else {
                MainSaleTabletLayout(
                    modifier = Modifier.padding(paddingValues),
                    products = products, // Use the real products list
                    cartItems = cartItems,
                    onAddToCart = { product ->
                        // Call the ViewModel function
                        viewModel.onAddToCart(product)
                    },
                    onRemoveFromCart = { /* TODO */ },
                    onCompleteSale = { /* TODO */ }
                )
            }
        }
    }
}

@Composable
fun MainSaleHandsetLayout(modifier: Modifier = Modifier, products: List<Product>, onAddToCart: (Product) -> Unit) {
    Column(modifier = modifier) {
        // Handset just shows the product list. Cart is a separate screen.
        ProductList(products = products, onProductClick = onAddToCart)
    }
}

@Composable
fun MainSaleTabletLayout(
    modifier: Modifier = Modifier,
    products: List<Product>,
    cartItems: List<CartItem>,
    onAddToCart: (Product) -> Unit,
    onRemoveFromCart: (CartItem) -> Unit,
    onCompleteSale: () -> Unit,
) {
    Row(modifier = modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        // Left Pane: Product List
        Box(modifier = Modifier.weight(0.4f)) {
            ProductList(products = products, onProductClick = onAddToCart)
        }

        // Right Pane: Cart and Summary
        Column(modifier = Modifier.weight(0.6f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            CartView(
                modifier = Modifier.weight(1f),
                cartItems = cartItems,
                onRemoveClick = onRemoveFromCart
            )
            SaleSummary(onCompleteSale = onCompleteSale)
        }
    }
}

// --- Reusable Components ---

@Composable
fun AppDrawerContent(navController: NavController, closeDrawer: () -> Unit) {
    Column(Modifier.padding(16.dp)) {
        Text("OpenInventory", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 16.dp))
        Divider()
        NavigationDrawerItem(
            icon = { Icon(Icons.Default.PointOfSale, null) },
            label = { Text("Point of Sale") },
            selected = true,
            onClick = { closeDrawer() }
        )
        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Inventory, null) },
            label = { Text("Product Management") },
            selected = false,
            onClick = {
                navController.navigate(Screen.ProductManagement.route)
                closeDrawer()
            }
        )
        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Assessment, null) },
            label = { Text("Sales Report") },
            selected = false,
            onClick = {
                navController.navigate(Screen.SalesReport.route)
                closeDrawer()
            }
        )
        NavigationDrawerItem(
            icon = { Icon(Icons.Default.People, null) },
            label = { Text("Manage Users") },
            selected = false,
            onClick = {
                navController.navigate(Screen.UserManagement.route)
                closeDrawer()
            }
        )
        Divider(modifier = Modifier.padding(vertical = 16.dp))
        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Settings, null) },
            label = { Text("Settings") },
            selected = false,
            onClick = {
                navController.navigate(Screen.Settings.route)
                closeDrawer()
            }
        )
    }
}

@Composable
fun ProductList(modifier: Modifier = Modifier, products: List<Product>, onProductClick: (Product) -> Unit) {
    LazyColumn(modifier = modifier.fillMaxSize()) {
        items(products) { product ->
            ListItem(
                headlineContent = { Text(product.name) },
                supportingContent = { Text("In Stock: ${product.quantity}") },
                trailingContent = { Text("€${"%.2f".format(product.price)}") },
                modifier = Modifier.clickable { onProductClick(product) }
            )
            Divider()
        }
    }
}

@Composable
fun CartView(modifier: Modifier = Modifier, cartItems: List<CartItem>, onRemoveClick: (CartItem) -> Unit) {
    Card(modifier.fillMaxWidth()) {
        Column {
            Text(
                "Cart",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(16.dp)
            )
            Divider()
            LazyColumn {
                items(cartItems) { item ->
                    ListItem(
                        headlineContent = { Text(item.product.name) },
                        supportingContent = { Text("€${"%.2f".format(item.product.price)}") },
                        trailingContent = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Qty: ${item.quantity}")
                                IconButton(onClick = { onRemoveClick(item) }) {
                                    Icon(Icons.Default.RemoveCircle, "Remove")
                                }
                            }
                        }
                    )
                    Divider()
                }
            }
        }
    }
}

@Composable
fun SaleSummary(modifier: Modifier = Modifier, onCompleteSale: () -> Unit) {
    Card(modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Sale Summary", style = MaterialTheme.typography.titleMedium)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Subtotal")
                Text("€120.50")
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Tax")
                Text("€24.10")
            }
            Divider()
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Total", style = MaterialTheme.typography.titleSmall)
                Text("€144.60", style = MaterialTheme.typography.titleSmall)
            }
            Spacer(Modifier.height(8.dp))
            Button(onClick = onCompleteSale, modifier = Modifier.fillMaxWidth()) {
                Text("Complete Sale")
            }
        }
    }
}