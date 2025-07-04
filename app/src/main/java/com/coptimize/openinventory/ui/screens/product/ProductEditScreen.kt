package com.coptimize.openinventory.ui.screens.product

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductEditScreen(
    productId: Long?,
    windowSizeClass: WindowSizeClass,
    onNavigateUp: () -> Unit
) {
    val isNewProduct = productId == null || productId == -1L
    val isHandset = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isNewProduct) "Add Product" else "Edit Product") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (isHandset) {
            ProductEditHandsetLayout(Modifier.padding(paddingValues), onSave = { onNavigateUp() })
        } else {
            ProductEditTabletLayout(Modifier.padding(paddingValues), onSave = { onNavigateUp() })
        }
    }
}

@Composable
private fun ProductEditHandsetLayout(modifier: Modifier, onSave: () -> Unit) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        item { ProductFormFields() }
        item {
            Button(onClick = onSave, modifier = Modifier.fillMaxWidth()) {
                Text("Save Product")
            }
        }
    }
}

@Composable
private fun ProductEditTabletLayout(modifier: Modifier, onSave: () -> Unit) {
    Row(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // In a real app, you might split fields into two columns
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            item { ProductFormFields() }
            item {
                Button(onClick = onSave, modifier = Modifier.fillMaxWidth()) {
                    Text("Save Product")
                }
            }
        }
        // Could have a live preview or related items on the right pane
        Box(modifier = Modifier.weight(1f)) {
            // Placeholder for a second pane
        }
    }
}

@Composable
private fun ProductFormFields() {
    // This composable contains the actual form fields, shared by both layouts
    var itemName by remember { mutableStateOf("") }
    var itemPrice by remember { mutableStateOf("") }
    var itemQuantity by remember { mutableStateOf("") }
    var itemBarcode by remember { mutableStateOf("") }

    Text("Item Details", style = MaterialTheme.typography.titleMedium)
    OutlinedTextField(value = itemName, onValueChange = { itemName = it }, label = { Text("Item Name*") })
    OutlinedTextField(value = itemBarcode, onValueChange = { itemBarcode = it }, label = { Text("Barcode") })
    OutlinedTextField(value = itemPrice, onValueChange = { itemPrice = it }, label = { Text("Unit Price*") })
    OutlinedTextField(value = itemQuantity, onValueChange = { itemQuantity = it }, label = { Text("Quantity*") })

    Spacer(modifier = Modifier.height(16.dp))

    Text("Additional Info", style = MaterialTheme.typography.titleMedium)
    // Add other fields like category, supplier, etc.
    OutlinedTextField(value = "", onValueChange = {}, label = { Text("Item Class*") })
    OutlinedTextField(value = "", onValueChange = {}, label = { Text("Supplier") })
    OutlinedTextField(value = "", onValueChange = {}, label = { Text("Purchase Price") })
}