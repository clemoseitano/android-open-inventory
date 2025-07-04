package com.coptimize.openinventory.ui.screens.product

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.coptimize.openinventory.data.Product
import com.coptimize.openinventory.data.SampleData
import com.coptimize.openinventory.navigation.Screen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ProductManagementScreen(windowSizeClass: WindowSizeClass, navController: NavController) {
    val pagerState = rememberPagerState(pageCount = { 2 })
    val scope = rememberCoroutineScope()
    val tabs = listOf("Active", "Archived")

    // State for the screen
    var activeProducts by remember { mutableStateOf(SampleData.products) }
    var archivedProducts by remember { mutableStateOf(SampleData.products.take(5)) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Product Management") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
                        onEdit = { product ->
                            navController.navigate(Screen.ProductEdit.createRoute(product.id))
                        },
                        onDelete = { /* TODO: Call ViewModel to delete */ }
                    )
                    1 -> ProductManagementList(
                        products = archivedProducts,
                        onRestore = { /* TODO: Call ViewModel to restore */ }
                    )
                }
            }
        }
    }
}

@Composable
fun ProductManagementList(
    products: List<Product>,
    onEdit: ((Product) -> Unit)? = null,
    onDelete: ((Product) -> Unit)? = null,
    onRestore: ((Product) -> Unit)? = null
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(products) { product ->
            Card(elevation = CardDefaults.cardElevation(2.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(product.name, style = MaterialTheme.typography.titleMedium)
                        Text("Stock: ${product.stockQuantity} | Barcode: ${product.barcode}", style = MaterialTheme.typography.bodySmall)
                    }
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