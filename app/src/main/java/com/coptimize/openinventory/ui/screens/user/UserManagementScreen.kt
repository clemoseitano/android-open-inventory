package com.coptimize.openinventory.ui.screens.user

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.coptimize.openinventory.data.SampleData
import com.coptimize.openinventory.data.User
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserManagementScreen(windowSizeClass: WindowSizeClass, navController: NavController) {
    var users by remember { mutableStateOf(SampleData.users) }
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Users") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { /* TODO: Navigate to create user screen */ }) {
                Icon(Icons.Default.Add, "Add User")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(users) { user ->
                UserListItem(user, dateFormat) {
                    // TODO: Handle delete click
                }
            }
        }
    }
}

@Composable
fun UserListItem(user: User, dateFormat: SimpleDateFormat, onDelete: (User) -> Unit) {
    Card {
        ListItem(
            headlineContent = { Text(user.name) },
            supportingContent = { Text("Role: ${user.role} | Last Login: ${dateFormat.format(user.lastLogin)}") },
            trailingContent = {
                TextButton(onClick = { onDelete(user) }) {
                    Text("Remove")
                }
            }
        )
    }
}