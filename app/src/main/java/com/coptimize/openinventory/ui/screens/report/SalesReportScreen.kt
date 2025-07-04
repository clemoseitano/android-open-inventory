package com.coptimize.openinventory.ui.screens.report

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.coptimize.openinventory.data.Sale
import com.coptimize.openinventory.data.SampleData
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesReportScreen(windowSizeClass: WindowSizeClass, navController: NavController) {
    val isHandset = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact
    val sales by remember { mutableStateOf(SampleData.sales) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sales Report") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (isHandset) {
            SalesReportHandsetLayout(Modifier.padding(paddingValues), sales)
        } else {
            SalesReportTabletLayout(Modifier.padding(paddingValues), sales)
        }
    }
}

@Composable
fun SalesReportHandsetLayout(modifier: Modifier = Modifier, sales: List<Sale>) {
    Column(modifier.fillMaxSize()) {
        FilterAndSummarySection(isHandset = true)
        Divider()
        SalesReportList(sales)
    }
}

@Composable
fun SalesReportTabletLayout(modifier: Modifier = Modifier, sales: List<Sale>) {
    Row(modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(0.35f)) {
            FilterAndSummarySection(isHandset = false)
        }
        VerticalDivider()
        Box(modifier = Modifier.weight(0.65f)) {
            SalesReportList(sales)
        }
    }
}

@Composable
fun FilterAndSummarySection(modifier: Modifier = Modifier, isHandset: Boolean) {
    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Filter Options
        Text("Filter Options", style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(value = "Today", onValueChange = {}, label = { Text("Date Range") }, readOnly = true)
        Button(onClick = { /* TODO: Apply Filter */ }, modifier = Modifier.fillMaxWidth()) {
            Text("Apply Filter")
        }

        Spacer(Modifier.height(if (isHandset) 8.dp else 24.dp))
        Divider()
        Spacer(Modifier.height(if (isHandset) 8.dp else 24.dp))

        // Summary
        Text("Report Summary", style = MaterialTheme.typography.titleMedium)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Total Sales:")
            Text("€617.25")
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Total Transactions:")
            Text("5")
        }
    }
}

@Composable
fun SalesReportList(sales: List<Sale>) {
    var expandedSaleIds by remember { mutableStateOf(setOf<Long>()) }
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()) }

    LazyColumn(contentPadding = PaddingValues(vertical = 8.dp)) {
        items(sales) { sale ->
            val isExpanded = sale.id in expandedSaleIds
            Column {
                ListItem(
                    modifier = Modifier.clickable {
                        expandedSaleIds = if (isExpanded) {
                            expandedSaleIds - sale.id
                        } else {
                            expandedSaleIds + sale.id
                        }
                    },
                    headlineContent = { Text("Sale #${sale.id} to ${sale.customerName}") },
                    supportingContent = { Text(dateFormat.format(sale.date)) },
                    trailingContent = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("€${"%.2f".format(sale.totalAmount)}")
                            Icon(
                                if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = "Expand"
                            )
                        }
                    }
                )
                if (isExpanded) {
                    Column(Modifier.padding(start = 32.dp, end = 16.dp, bottom = 8.dp)) {
                        sale.items.forEach { item ->
                            ListItem(
                                headlineContent = { Text(item.productName) },
                                supportingContent = { Text("${item.quantity} x €${"%.2f".format(item.unitPrice)}") },
                                trailingContent = { Text("€${"%.2f".format(item.quantity * item.unitPrice)}") }
                            )
                        }
                    }
                }
                Divider()
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