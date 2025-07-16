package com.coptimize.openinventory.ui.screens.product

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.coptimize.openinventory.ui.formatAsDateForDisplay
import java.util.Date
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade

private enum class DatePickerTarget { PURCHASE, EXPIRY }

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
                onQuantityToAddChange = viewModel::onQuantityToAddChange,
                onTaxChange = viewModel::onTaxChange,
                onTaxTypeChange = viewModel::onTaxTypeChange,
                onArchivedChange = viewModel::onArchivedChange,
                onSupplierChange = viewModel::onSupplierChange,
                onSupplierContactChange = viewModel::onSupplierContactChange,
                onStoreSectionChange = viewModel::onStoreSectionChange,
                onShelfAisleChange = viewModel::onShelfAisleChange,
                onPurchasePriceChange = viewModel::onPurchasePriceChange,
                onPurchaseDateChange = viewModel::onPurchaseDateChange,
                onExpiryDateChange = viewModel::onExpiryDateChange,
                onImageSelected = viewModel::onImageSelected,
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
                    onQuantityToAddChange = viewModel::onQuantityToAddChange,
                    onTaxChange = viewModel::onTaxChange,
                    onTaxTypeChange = viewModel::onTaxTypeChange,
                    onArchivedChange = viewModel::onArchivedChange,
                    onSupplierChange = viewModel::onSupplierChange,
                    onSupplierContactChange = viewModel::onSupplierContactChange,
                    onStoreSectionChange = viewModel::onStoreSectionChange,
                    onShelfAisleChange = viewModel::onShelfAisleChange,
                    onPurchasePriceChange = viewModel::onPurchasePriceChange,
                    onPurchaseDateChange = viewModel::onPurchaseDateChange,
                    onExpiryDateChange = viewModel::onExpiryDateChange,
                    onImageSelected = viewModel::onImageSelected,
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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProductFormFields(
    uiState: ProductEditUiState,
    onNameChange: (String) -> Unit,
    onBarcodeChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    onManufacturerChange: (String) -> Unit,
    onSupplierChange: (String) -> Unit,
    onSupplierContactChange: (String) -> Unit,
    onStoreSectionChange: (String) -> Unit,
    onShelfAisleChange: (String) -> Unit,
    onPriceChange: (String) -> Unit,
    onQuantityToAddChange: (String) -> Unit,
    onPurchasePriceChange: (String) -> Unit,
    onTaxChange: (String) -> Unit,
    onTaxTypeChange: (Boolean) -> Unit,
    onPurchaseDateChange: (Date) -> Unit,
    onExpiryDateChange: (Date?) -> Unit,
    onArchivedChange: (Boolean) -> Unit,
    onImageSelected: (Uri?) -> Unit,
) {
    // State to manage the date picker dialog
    var activeDatePicker by remember { mutableStateOf<DatePickerTarget?>(null) }

    if (activeDatePicker != null) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { activeDatePicker = null },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val selectedDate = Date(millis)
                            when(activeDatePicker) {
                                DatePickerTarget.PURCHASE -> onPurchaseDateChange(selectedDate)
                                DatePickerTarget.EXPIRY -> onExpiryDateChange(selectedDate)
                                else -> {}
                            }
                        }
                        activeDatePicker = null
                    }
                ) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { activeDatePicker = null }) { Text("Cancel") } }
        ) {
            DatePicker(state = datePickerState)
        }
    }
    // 1. Create the ActivityResultLauncher.
    // This launcher asks the system to let the user pick any image.
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        // 3. This block is the callback. It's executed when the user
        //    either picks an image (uri is not null) or cancels (uri is null).
        //    We pass the result up to the ViewModel.
        onImageSelected(uri)
    }

    // --- Primary Details ---
    SectionTitle("Item Details")
    OutlinedTextField(value = uiState.name, onValueChange = onNameChange, label = { Text("Item Name*") }, isError = uiState.nameError != null, supportingText = { if (uiState.nameError != null) Text(uiState.nameError) })
    OutlinedTextField(value = uiState.barcode, onValueChange = onBarcodeChange, label = { Text("Barcode") })
    OutlinedTextField(value = uiState.category, onValueChange = onCategoryChange, label = { Text("Category (Item Class)*") })
    OutlinedTextField(value = uiState.manufacturer, onValueChange = onManufacturerChange, label = { Text("Manufacturer") })

    // --- Supplier Info ---
    SectionTitle("Supplier Information")
    OutlinedTextField(value = uiState.supplier, onValueChange = onSupplierChange, label = { Text("Supplier") })
    OutlinedTextField(value = uiState.supplierContact, onValueChange = onSupplierContactChange, label = { Text("Supplier Contact") })

    // --- Location Info ---
    SectionTitle("Location")
    OutlinedTextField(value = uiState.storeSection, onValueChange = onStoreSectionChange, label = { Text("Store Section") })
    OutlinedTextField(value = uiState.shelfAisle, onValueChange = onShelfAisleChange, label = { Text("Shelf/Aisle #") })

    // --- Pricing & Stock ---
    SectionTitle("Pricing & Stock")
    OutlinedTextField(value = uiState.price, onValueChange = onPriceChange, label = { Text("Unit Price*") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), isError = uiState.priceError != null, supportingText = { if (uiState.priceError != null) Text(uiState.priceError) })
    OutlinedTextField(value = uiState.purchasePrice, onValueChange = onPurchasePriceChange, label = { Text("Purchase Price") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))

    Row(verticalAlignment = Alignment.CenterVertically) {
        OutlinedTextField(modifier = Modifier.weight(1f), value = uiState.quantityToAdd, onValueChange = onQuantityToAddChange, label = { Text("Quantity to Add*") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
        Spacer(Modifier.width(8.dp))
        Text("${uiState.quantityInStock} in stock", style = MaterialTheme.typography.bodyMedium)
    }

    // --- Dates ---
    SectionTitle("Dates")
    OutlinedTextField(value = uiState.purchaseDate.time.formatAsDateForDisplay(), onValueChange = {}, label = { Text("Purchase Date") }, readOnly = true, modifier = Modifier.clickable { activeDatePicker = DatePickerTarget.PURCHASE })
    OutlinedTextField(value = uiState.expiryDate?.time?.formatAsDateForDisplay() ?: "N/A", onValueChange = {}, label = { Text("Expiry Date") }, readOnly = true, modifier = Modifier.clickable { activeDatePicker = DatePickerTarget.EXPIRY })

    // --- Tax ---
    SectionTitle("Tax")
    Row(verticalAlignment = Alignment.CenterVertically) {
        OutlinedTextField(modifier = Modifier.weight(1f), value = uiState.tax, onValueChange = onTaxChange, label = { Text("Tax") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
        Spacer(Modifier.width(8.dp))
        Checkbox(checked = uiState.isTaxFlatRate, onCheckedChange = onTaxTypeChange)
        Text("Flat Rate")
    }

    // --- Meta ---
    SectionTitle("Meta")

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // --- Image Preview on the Left ---
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(uiState.imagePath) // Coil can load directly from a file path
                .crossfade(true)
                .build(),
            //placeholder = painterResource(R.drawable.ic_placeholder_image), // A placeholder image from your drawable resources
            //error = painterResource(R.drawable.ic_error_image), // An error image
            contentDescription = "Selected Product Image",
            modifier = Modifier
                .size(100.dp) // A fixed size for the preview
                .border(1.dp, MaterialTheme.colorScheme.outline, shape = MaterialTheme.shapes.small),
            contentScale = ContentScale.Crop // Crop the image to fit the bounds
        )

        Spacer(Modifier.width(16.dp))

        // --- Button on the Right ---
        Button(onClick = {
            imagePickerLauncher.launch("image/*")
        }) {
//            Text("Browse...")
            Text("Browse for Image")
        }
    }

    // Display the path of the selected image
    if (uiState.imagePath.isNotBlank()) {
        Text("Image: ${uiState.imagePath}", style = MaterialTheme.typography.bodySmall)
    }
}

// Helper composable for section titles to reduce repetition
@Composable
private fun SectionTitle(title: String) {
    Spacer(modifier = Modifier.height(16.dp))
    Text(title, style = MaterialTheme.typography.titleMedium)
    Spacer(modifier = Modifier.height(8.dp))
}