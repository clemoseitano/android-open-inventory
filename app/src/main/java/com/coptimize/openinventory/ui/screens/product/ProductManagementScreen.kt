package com.coptimize.openinventory.ui.screens.product

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.coptimize.openinventory.data.model.Product
import com.coptimize.openinventory.navigation.Screen
import com.coptimize.openinventory.ui.CurrencyViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ProductManagementScreen(
    windowSizeClass: WindowSizeClass,
    navController: NavController,
    viewModel: ProductManagementViewModel = hiltViewModel(),
    currencyViewModel: CurrencyViewModel = hiltViewModel()
) {
    val pagerState = rememberPagerState(pageCount = { 2 })
    val scope = rememberCoroutineScope()
    val tabs = listOf("Active", "Archived")

    val activeProducts by viewModel.activeProducts.collectAsStateWithLifecycle()
    val archivedProducts by viewModel.archivedProducts.collectAsStateWithLifecycle()
    val currencySymbol by currencyViewModel.currencySymbol.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Product Management") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                navController.navigate(Screen.ProductEdit.createRoute(null))
            }) {
                Icon(Icons.Default.Add, contentDescription = "Add Product")
            }
        }
    ) { paddingValues ->
        Column(Modifier.padding(paddingValues)) {
            TabRow(selectedTabIndex = pagerState.currentPage) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                        text = { Text(title) }
                    )
                }
            }
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                when (page) {
                    0 -> ProductManagementList(
                        products = activeProducts,
                        currencySymbol = currencySymbol, // Pass symbol down
                        onEdit = { product ->
                            navController.navigate(Screen.ProductEdit.createRoute(product.id.toLong()))
                        },
                        onDelete = { product -> viewModel.deleteProduct(product = product) }
                    )
                    1 -> ProductManagementList(
                        products = archivedProducts,
                        currencySymbol = currencySymbol, // Pass symbol down
                        onRestore = { product -> viewModel.restoreProduct(product = product) }
                    )
                }
            }
        }
    }
}

@Composable
fun ProductManagementList(
    products: List<Product>,
    currencySymbol: String, // Receive the symbol
    onEdit: ((Product) -> Unit)? = null,
    onDelete: ((Product) -> Unit)? = null,
    onRestore: ((Product) -> Unit)? = null
) {
    if (products.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
            Text("No products in this list.", style = MaterialTheme.typography.bodyLarge)
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(products, key = { it.id }) { product ->
            Card(elevation = CardDefaults.cardElevation(2.dp)) {
                ListItem(
                    headlineContent = { Text(product.name, style = MaterialTheme.typography.titleMedium) },
                    supportingContent = { Text("Stock: ${product.quantity} | Barcode: ${product.barcode ?: "N/A"}") },
                    // Display the price with the correct currency symbol
                    trailingContent = { Text("$currencySymbol${"%.2f".format(product.price)}", style = MaterialTheme.typography.bodyLarge) },
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp), horizontalArrangement = Arrangement.End) {
                    if (onEdit != null && onDelete != null) {
                        TextButton(onClick = { onEdit(product) }) { Text("Edit") }
                        TextButton(onClick = { onDelete(product) }) { Text("Delete") }
                    }
                    if (onRestore != null) {
                        TextButton(onClick = { onRestore(product) }) { Text("Restore") }
                    }
                }
            }
        }
    }
}