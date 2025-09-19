package com.coptimize.openinventory.ui.screens.user

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.coptimize.openinventory.data.model.User
import com.coptimize.openinventory.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserManagementScreen(
    windowSizeClass: WindowSizeClass,
    navController: NavController,
    viewModel: UserManagementViewModel = hiltViewModel()
) {
    val users by viewModel.users.collectAsStateWithLifecycle()
    var userToDelete by remember { mutableStateOf<User?>(null) }

    // Get the current user's ID from the ViewModel
    val currentUserId = viewModel.currentUserId

    // --- Confirmation Dialog for Deletion (Unchanged) ---
    if (userToDelete != null) {
        AlertDialog(
            onDismissRequest = { userToDelete = null },
            title = { Text("Confirm Deletion") },
            text = { Text("Are you sure you want to remove user '${userToDelete!!.username}'?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteUser(userToDelete!!.id)
                        userToDelete = null
                    }
                ) { Text("Remove") }
            },
            dismissButton = {
                TextButton(onClick = { userToDelete = null }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Users") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                navController.navigate(Screen.UserEdit.createRoute(null))
            }) {
                Icon(Icons.Default.Add, "Add User")
            }
        }
    ) { paddingValues ->
        if (users.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Text("No users found.")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(users, key = { it.id }) { user ->
                    UserListItem(
                        user = user,
                        // Pass the current user's ID to the list item
                        currentUserId = currentUserId,
                        onDeleteClick = { userToDelete = user },
                        onEditClick = { navController.navigate(Screen.UserEdit.createRoute(user.id)) }
                    )
                }
            }
        }
    }
}

@Composable
fun UserListItem(
    user: User,
    currentUserId: String?,
    onDeleteClick: () -> Unit,
    onEditClick: () -> Unit,
) {
    val isCurrentUser = user.id == currentUserId

    Card {
        ListItem(
            headlineContent = { Text(user.username, fontWeight = FontWeight.Bold) },
            supportingContent = { Text("Role: ${user.role.replaceFirstChar { it.uppercase() }}") },
            trailingContent = {
                Row {
                    IconButton(onClick = onEditClick) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit User")
                    }
                    IconButton(
                        onClick = onDeleteClick,
                        enabled = !isCurrentUser
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Remove User",
                            tint = if (isCurrentUser) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f) else LocalContentColor.current
                        )
                    }
                }
            }
        )
    }
}