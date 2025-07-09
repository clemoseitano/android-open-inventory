package com.coptimize.openinventory.ui.screens.report

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.coptimize.openinventory.data.model.SaleWithItems
import com.coptimize.openinventory.ui.formatAsDateForDisplay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesReportScreen(
    windowSizeClass: WindowSizeClass,
    navController: NavController,
    viewModel: SalesReportViewModel = hiltViewModel()
) {
    val isHandset = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sales Report") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (isHandset) {
            SalesReportHandsetLayout(
                modifier = Modifier.padding(paddingValues),
                uiState = uiState,
                onDateRangeSelected = viewModel::onDateRangeSelected
            )
        } else {
            SalesReportTabletLayout(
                modifier = Modifier.padding(paddingValues),
                uiState = uiState,
                onDateRangeSelected = viewModel::onDateRangeSelected
            )
        }
    }
}

@Composable
fun SalesReportHandsetLayout(
    modifier: Modifier = Modifier,
    uiState: SalesReportUiState,
    onDateRangeSelected: (Long, Long) -> Unit
) {
    Column(modifier.fillMaxSize()) {
        FilterAndSummarySection(
            modifier = modifier,
            isHandset = true,
            uiState = uiState,
            onDateRangeSelected = onDateRangeSelected
        )
        HorizontalDivider()
        SalesReportList(uiState.salesWithItems)
    }
}

@Composable
fun SalesReportTabletLayout(
    modifier: Modifier = Modifier,
    uiState: SalesReportUiState,
    onDateRangeSelected: (Long, Long) -> Unit
) {
    Row(modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(0.35f)) {
            FilterAndSummarySection(
                modifier = modifier,
                isHandset = false,
                uiState = uiState,
                onDateRangeSelected = onDateRangeSelected
            )
        }
        VerticalDivider()
        Box(modifier = Modifier.weight(0.65f)) {
            SalesReportList(uiState.salesWithItems)
        }
    }
}

// An enum to keep track of which date picker is currently being shown.
private enum class DatePickerTarget { START, END }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterAndSummarySection(
    modifier: Modifier = Modifier,
    isHandset: Boolean,
    uiState: SalesReportUiState,
    onDateRangeSelected: (Long, Long) -> Unit
) {
    // State to track which picker to show, or null to show none.
    var activeDatePicker by remember { mutableStateOf<DatePickerTarget?>(null) }

    // --- Date Picker Dialog ---
    if (activeDatePicker != null) {
        val datePickerState = rememberDatePickerState()

        DatePickerDialog(
            onDismissRequest = { activeDatePicker = null },
            confirmButton = {
                TextButton(
                    onClick = {
                        val selectedDateMillis = datePickerState.selectedDateMillis ?: return@TextButton

                        // Update either the start or end date based on which picker was active.
                        when (activeDatePicker) {
                            DatePickerTarget.START -> onDateRangeSelected(selectedDateMillis, uiState.endDate.time)
                            DatePickerTarget.END -> onDateRangeSelected(uiState.startDate.time, selectedDateMillis)
                            null -> {}
                        }
                        activeDatePicker = null // Close the dialog
                    },
                    enabled = datePickerState.selectedDateMillis != null
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { activeDatePicker = null }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- Filter Options ---
        Text("Filter Options", style = MaterialTheme.typography.titleMedium)

        // --- Row for Start and End Date Fields ---
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Start Date Field
            OutlinedTextField(
                value = uiState.startDate.time.formatAsDateForDisplay(),
                onValueChange = {},
                label = { Text("Start Date") },
                readOnly = true,
                modifier = Modifier
                    .weight(1f)
                    .clickable { activeDatePicker = DatePickerTarget.START }, // Show start date picker
                trailingIcon = { Icon(Icons.Default.DateRange, "Select Start Date") }
            )

            // End Date Field
            OutlinedTextField(
                value = uiState.endDate.time.formatAsDateForDisplay(),
                onValueChange = {},
                label = { Text("End Date") },
                readOnly = true,
                modifier = Modifier
                    .weight(1f)
                    .clickable { activeDatePicker = DatePickerTarget.END }, // Show end date picker
                trailingIcon = { Icon(Icons.Default.DateRange, "Select End Date") }
            )
        }

        Spacer(Modifier.height(if (isHandset) 8.dp else 24.dp))
        HorizontalDivider()
        Spacer(Modifier.height(if (isHandset) 8.dp else 24.dp))

        // --- Summary ---
        Text("Report Summary", style = MaterialTheme.typography.titleMedium)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Total Sales:")
            Text("€${"%.2f".format(uiState.totalSalesValue)}")
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Total Transactions:")
            Text("${uiState.totalTransactions}")
        }
    }
}

@Composable
fun SalesReportList(sales: List<SaleWithItems>) {
    var expandedSaleIds by remember { mutableStateOf(setOf<String>()) }

    LazyColumn(contentPadding = PaddingValues(vertical = 8.dp)) {
        items(sales, key = { it.sale.id }) { saleItem ->
            val isExpanded = saleItem.sale.id in expandedSaleIds
            Column {
                ListItem(
                    modifier = Modifier.clickable {
                        expandedSaleIds = if (isExpanded) {
                            expandedSaleIds - saleItem.sale.id
                        } else {
                            expandedSaleIds + saleItem.sale.id
                        }
                    },
                    headlineContent = { Text("Sale to ${saleItem.sale.customerName}") }, // Customer name needs to be added to Sale model
                    supportingContent = { Text(saleItem.sale.createdAt) },
                    trailingContent = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("€${"%.2f".format(saleItem.sale.total)}")
                            Icon(
                                if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = "Expand"
                            )
                        }
                    }
                )
                if (isExpanded) {
                    Column(Modifier.padding(start = 32.dp, end = 16.dp, bottom = 8.dp)) {
                        saleItem.items.forEach { item ->
                            ListItem(
                                headlineContent = { Text(item.productName) },
                                supportingContent = { Text("${item.quantity} x €${"%.2f".format(item.price)}") },
                                trailingContent = { Text("€${"%.2f".format(item.quantity * item.price)}") }
                            )
                        }
                    }
                }
                HorizontalDivider()
            }
        }
    }
}

@Composable
fun VerticalDivider() {
    Box(
        modifier = Modifier
            .fillMaxHeight()
            .width(1.dp)
            .background(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
    )
}