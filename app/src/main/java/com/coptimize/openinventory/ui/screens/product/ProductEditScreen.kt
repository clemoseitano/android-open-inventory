package com.coptimize.openinventory.ui.screens.product

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductEditScreen(
    // The nav argument should be a String to handle the default "-1" case easily
    productId: String?,
    windowSizeClass: WindowSizeClass,
    onNavigateUp: () -> Unit,
    viewModel: ProductEditViewModel = hiltViewModel()
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
    val isHandset = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact

    // When save is successful, navigate back
    LaunchedEffect(uiState.isSaveSuccessful) {
        if (uiState.isSaveSuccessful) {
            onNavigateUp()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (uiState.isExistingProduct) "Edit Product" else "Add Product") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            if (isHandset) {
                ProductEditHandsetLayout(
                    modifier = Modifier.padding(paddingValues),
                    uiState = uiState,
                    viewModel = viewModel,
                    onSave = { viewModel.saveProduct() }
                )
            } else {
                ProductEditTabletLayout(
                    modifier = Modifier.padding(paddingValues),
                    uiState = uiState,
                    viewModel = viewModel,
                    onSave = { viewModel.saveProduct() }
                )
            }
        }
    }
}

@Composable
private fun ProductEditHandsetLayout(
    modifier: Modifier,
    uiState: ProductEditUiState,
    viewModel: ProductEditViewModel,
    onSave: () -> Unit
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        item {
            ProductFormFields(
                uiState = uiState,
                onNameChange = viewModel::onNameChange,
                onCategoryChange = viewModel::onCategoryChange,
                onManufacturerChange = viewModel::onManufacturerChange,
                onBarcodeChange = viewModel::onBarcodeChange,
                onPriceChange = viewModel::onPriceChange,
                onQuantityChange = viewModel::onQuantityChange,
                onTaxChange = viewModel::onTaxChange,
                onTaxTypeChange = viewModel::onTaxTypeChange,
                onArchivedChange = viewModel::onArchivedChange
            )
        }
        item {
            Button(onClick = onSave, modifier = Modifier.fillMaxWidth()) {
                Text("Save Product")
            }
        }
    }
}

@Composable
private fun ProductEditTabletLayout(
    modifier: Modifier,
    uiState: ProductEditUiState,
    viewModel: ProductEditViewModel,
    onSave: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                ProductFormFields(
                    uiState = uiState,
                    onNameChange = viewModel::onNameChange,
                    onCategoryChange = viewModel::onCategoryChange,
                    onManufacturerChange = viewModel::onManufacturerChange,
                    onBarcodeChange = viewModel::onBarcodeChange,
                    onPriceChange = viewModel::onPriceChange,
                    onQuantityChange = viewModel::onQuantityChange,
                    onTaxChange = viewModel::onTaxChange,
                    onTaxTypeChange = viewModel::onTaxTypeChange,
                    onArchivedChange = viewModel::onArchivedChange
                )
            }
            item {
                Button(onClick = onSave, modifier = Modifier.fillMaxWidth()) {
                    Text("Save Product")
                }
            }
        }
        // This second pane could have product history, sales stats, etc. in a real app
        Box(modifier = Modifier.weight(1f))
    }
}


@Composable
private fun ProductFormFields(
    uiState: ProductEditUiState,
    onNameChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    onManufacturerChange: (String) -> Unit,
    onBarcodeChange: (String) -> Unit,
    onPriceChange: (String) -> Unit,
    onQuantityChange: (String) -> Unit,
    onTaxChange: (String) -> Unit,
    onTaxTypeChange: (Boolean) -> Unit,
    onArchivedChange: (Boolean) -> Unit
) {
    // --- Primary Details ---
    Text("Item Details", style = MaterialTheme.typography.titleMedium)

    OutlinedTextField(
        value = uiState.name,
        onValueChange = onNameChange,
        label = { Text("Item Name*") },
        isError = uiState.nameError != null,
        supportingText = { if (uiState.nameError != null) Text(uiState.nameError) }
    )
    OutlinedTextField(value = uiState.category, onValueChange = onCategoryChange, label = { Text("Category*") })
    OutlinedTextField(value = uiState.manufacturer, onValueChange = onManufacturerChange, label = { Text("Manufacturer") })
    OutlinedTextField(value = uiState.barcode, onValueChange = onBarcodeChange, label = { Text("Barcode") })

    Spacer(modifier = Modifier.height(16.dp))

    // --- Pricing and Stock ---
    Text("Pricing & Stock", style = MaterialTheme.typography.titleMedium)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            modifier = Modifier.weight(1f),
            value = uiState.price,
            onValueChange = onPriceChange,
            label = { Text("Unit Price*") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            isError = uiState.priceError != null,
            supportingText = { if (uiState.priceError != null) Text(uiState.priceError) }
        )
        OutlinedTextField(
            modifier = Modifier.weight(1f),
            value = uiState.quantity,
            onValueChange = onQuantityChange,
            label = { Text("Quantity*") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
    }

    // --- Tax ---
    OutlinedTextField(
        value = uiState.tax,
        onValueChange = onTaxChange,
        label = { Text("Tax") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
    )
    Row(verticalAlignment = Alignment.CenterVertically) {
        RadioButton(selected = uiState.isTaxFlatRate, onClick = { onTaxTypeChange(true) })
        Text("Flat Rate")
        Spacer(Modifier.width(16.dp))
        RadioButton(selected = !uiState.isTaxFlatRate, onClick = { onTaxTypeChange(false) })
        Text("Percentage (%)")
    }

    Spacer(modifier = Modifier.height(16.dp))

    // --- Other ---
    // In a real app, this would open the system's image picker
    Button(onClick = { /* TODO: Launch Image Picker */ }) {
        Text("Browse for Image")
    }
    if (uiState.imagePath.isNotBlank()) {
        Text("Image: ${uiState.imagePath}", style = MaterialTheme.typography.bodySmall)
    }

    // Show archive toggle only for existing products
    if (uiState.isExistingProduct) {
        Spacer(modifier = Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Archived", modifier = Modifier.weight(1f))
            Switch(checked = uiState.isArchived, onCheckedChange = onArchivedChange)
        }
    }
}