package com.coptimize.openinventory.ui.screens.product

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.coptimize.openinventory.ui.formatAsDateForDisplay
import java.util.Calendar
import java.util.Date

private enum class DatePickerTarget { PURCHASE, EXPIRY }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductEditScreen(
    productId: String?,
    windowSizeClass: WindowSizeClass,
    onNavigateUp: () -> Unit,
    viewModel: ProductEditViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isHandset = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact
    var showEnrollmentCamera by remember { mutableStateOf(false) }

    if (showEnrollmentCamera) {
        Dialog(
            onDismissRequest = { showEnrollmentCamera = false },
            properties = DialogProperties(usePlatformDefaultWidth = false) // Full screen
        ) {
            ProductEnrollmentCameraScreen(
                onDismiss = { showEnrollmentCamera = false },
                onComplete = { barcode, uris ->
                    viewModel.onEnrollmentComplete(barcode, uris)
                    showEnrollmentCamera = false
                }
            )
        }
    }


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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        else if (uiState.isAnalyzing) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(Modifier.height(16.dp))
                    Text("Analyzing Product Image...")
                }
            }
        } else {
            // Pass the lambda to open the camera screen
            val openCamera: () -> Unit = { showEnrollmentCamera = true }

            if (isHandset) {
                ProductEditHandsetLayout(
                    modifier = Modifier.padding(paddingValues),
                    uiState = uiState,
                    viewModel = viewModel,
                    onSave = { viewModel.saveProduct() },
                    onOpenCamera = openCamera
                )
            } else {
                ProductEditTabletLayout(
                    modifier = Modifier.padding(paddingValues),
                    uiState = uiState,
                    viewModel = viewModel,
                    onSave = { viewModel.saveProduct() },
                    onOpenCamera = openCamera
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
    onSave: () -> Unit,
    onOpenCamera: () -> Unit // Receive the lambda
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
                onOpenCamera = onOpenCamera // Pass it down
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
    onSave: () -> Unit,
    onOpenCamera: () -> Unit // Receive the lambda
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
                    onOpenCamera = onOpenCamera // Pass it down
                )
            }
            item {
                Button(onClick = onSave, modifier = Modifier.fillMaxWidth()) {
                    Text("Save Product")
                }
            }
        }
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
    onOpenCamera: () -> Unit // New lambda parameter
) {
    var activeDatePicker by remember { mutableStateOf<DatePickerTarget?>(null) }

    if (activeDatePicker != null) {
        val datePickerState = if (activeDatePicker == DatePickerTarget.PURCHASE) {
            // For PURCHASE, create the state WITH the selectableDates constraint.
            rememberDatePickerState(
                initialSelectedDateMillis = uiState.purchaseDate.time,
                selectableDates = object : SelectableDates {
                    override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                        // Only allow selection of dates up to and including today.
                        return utcTimeMillis <= System.currentTimeMillis()
                    }
                }
            )
        } else {
            rememberDatePickerState(
                initialSelectedDateMillis = uiState.expiryDate?.time
            )
        }

        DatePickerDialog(
            onDismissRequest = { activeDatePicker = null },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val selectedDate = Date(millis)
                            when (activeDatePicker) {
                                DatePickerTarget.PURCHASE -> onPurchaseDateChange(selectedDate)
                                DatePickerTarget.EXPIRY -> onExpiryDateChange(selectedDate)
                                null -> {}
                            }
                        }
                        activeDatePicker = null
                    },
                    enabled = datePickerState.selectedDateMillis != null
                ) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { activeDatePicker = null }) { Text("Cancel") } }
        ) {
            DatePicker(
                state = datePickerState,
            )
        }
    }

    // --- Primary Details ---
    SectionTitle("Item Details")
    OutlinedTextField(value = uiState.name, onValueChange = onNameChange, label = { Text("Item Name*") }, isError = uiState.nameError != null, supportingText = { if (uiState.nameError != null) Text(uiState.nameError) }, singleLine = true, modifier = Modifier.fillMaxWidth())

    // --- Barcode with Camera Button ---
    Row(verticalAlignment = Alignment.CenterVertically) {
        OutlinedTextField(
            value = uiState.barcode,
            onValueChange = onBarcodeChange,
            label = { Text("Barcode") },
            singleLine = true,
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = onOpenCamera) { // Trigger the camera flow
            Icon(Icons.Default.CameraAlt, "Scan or Add Photos")
        }
    }

    OutlinedTextField(value = uiState.category, onValueChange = onCategoryChange, label = { Text("Category (Item Class)*") }, singleLine = true, modifier = Modifier.fillMaxWidth())
    OutlinedTextField(value = uiState.manufacturer, onValueChange = onManufacturerChange, label = { Text("Manufacturer") }, singleLine = true, modifier = Modifier.fillMaxWidth())

    // --- Supplier Info ---
    SectionTitle("Supplier Information")
    OutlinedTextField(value = uiState.supplier, onValueChange = onSupplierChange, label = { Text("Supplier") }, singleLine = true, modifier = Modifier.fillMaxWidth())
    OutlinedTextField(value = uiState.supplierContact, onValueChange = onSupplierContactChange, label = { Text("Supplier Contact") }, singleLine = true, modifier = Modifier.fillMaxWidth())

    // --- Location Info ---
    SectionTitle("Location")
    OutlinedTextField(value = uiState.storeSection, onValueChange = onStoreSectionChange, label = { Text("Store Section") }, singleLine = true, modifier = Modifier.fillMaxWidth())
    OutlinedTextField(value = uiState.shelfAisle, onValueChange = onShelfAisleChange, label = { Text("Shelf/Aisle #") }, singleLine = true, modifier = Modifier.fillMaxWidth())

    // --- Pricing & Stock ---
    SectionTitle("Pricing & Stock")
    OutlinedTextField(value = uiState.price, onValueChange = onPriceChange, label = { Text("Unit Price*") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), isError = uiState.priceError != null, supportingText = { if (uiState.priceError != null) Text(uiState.priceError) }, singleLine = true, modifier = Modifier.fillMaxWidth())
    OutlinedTextField(value = uiState.purchasePrice, onValueChange = onPurchasePriceChange, label = { Text("Purchase Price") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true, modifier = Modifier.fillMaxWidth())

    Row(verticalAlignment = Alignment.CenterVertically) {
        OutlinedTextField(modifier = Modifier.weight(1f), value = uiState.quantityToAdd, onValueChange = onQuantityToAddChange, label = { Text("Quantity to Add*") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true)
        Spacer(Modifier.width(8.dp))
        Text("${uiState.quantityInStock} in stock", style = MaterialTheme.typography.bodyMedium)
    }

    // --- Dates ---
    SectionTitle("Dates")
    // Wrap the Purchase Date field in a Box
    Box(modifier = Modifier.clickable { activeDatePicker = DatePickerTarget.PURCHASE }) {
        OutlinedTextField(
            value = uiState.purchaseDate.time.formatAsDateForDisplay(),
            onValueChange = {},
            label = { Text("Purchase Date") },
            // Disable the text field entirely to let the Box handle clicks
            enabled = false,
            modifier = Modifier.fillMaxWidth(),
            // Adjust the colors to make the disabled field look enabled and clear
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledBorderColor = MaterialTheme.colorScheme.outline,
                disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        )
    }

    // Wrap the Expiry Date field in a Box
    Box(modifier = Modifier.clickable { activeDatePicker = DatePickerTarget.EXPIRY }) {
        OutlinedTextField(
            value = uiState.expiryDate?.time?.formatAsDateForDisplay() ?: "N/A",
            onValueChange = {},
            label = { Text("Expiry Date") },
            enabled = false, // Disable the text field
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledBorderColor = MaterialTheme.colorScheme.outline,
                disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        )
    }

    // --- Tax ---
    SectionTitle("Tax")
    Row(verticalAlignment = Alignment.CenterVertically) {
        OutlinedTextField(modifier = Modifier.weight(1f), value = uiState.tax, onValueChange = onTaxChange, label = { Text("Tax") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true)
        Spacer(Modifier.width(8.dp))
        Checkbox(checked = uiState.isTaxFlatRate, onCheckedChange = onTaxTypeChange)
        Text("Flat Rate")
    }

    // --- Meta ---
    SectionTitle("Meta")
    // Display the primary image
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current).data(uiState.imagePath).crossfade(true).build(),
        contentDescription = "Selected Product Image",
        modifier = Modifier
            .height(150.dp)
            .fillMaxWidth()
            .border(
                1.dp,
                MaterialTheme.colorScheme.outline,
                shape = MaterialTheme.shapes.small
            ),
        contentScale = ContentScale.Crop
    )


    // Show archive toggle only for existing products
    if (uiState.isExistingProduct) {
        Spacer(modifier = Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Archived", modifier = Modifier.weight(1f))
            Switch(checked = uiState.isArchived, onCheckedChange = onArchivedChange)
        }
    }
}

// Helper composable for section titles
@Composable
private fun SectionTitle(title: String) {
    Spacer(modifier = Modifier.height(16.dp))
    Text(title, style = MaterialTheme.typography.titleMedium)
    Spacer(modifier = Modifier.height(8.dp))
}