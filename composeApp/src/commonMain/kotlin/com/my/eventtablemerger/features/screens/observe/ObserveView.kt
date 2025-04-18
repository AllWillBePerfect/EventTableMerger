package com.my.eventtablemerger.features.screens.observe

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File
import com.my.eventtablemerger.core.drive.clearCredentials
import com.my.eventtablemerger.features.screens.login.presentation.CredentialsProvider
import com.my.eventtablemerger.features.screens.login.presentation.getDriveService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

data class FolderInfo(val name: String, val id: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ObserveView(
    viewState: ObserveViewState,
    onAction: (ObserveAction) -> Unit,
    navController: NavController,
    credentialsProvider: CredentialsProvider = koinInject(),
    navigateToSearchScreen: (String) -> Unit
) {
    var folderInfos by remember { mutableStateOf<List<FolderInfo>>(emptyList()) }
    var eventManagerFolderId by remember { mutableStateOf<String?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    var newFolderName by remember { mutableStateOf("") }
    var folderNameError by remember { mutableStateOf<String?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var folderToDelete by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    var isRefresh by remember { mutableStateOf(true) }

    var showLogoutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(isRefresh) {
        if (isRefresh) {
            isLoading = true
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    println("Attempting to get credentials...")
                    val credential = credentialsProvider.getCredentials()
                    println("Credentials obtained successfully.")

                    val driveService = getDriveService(credential)
                    println("Drive service initialized.")

                    eventManagerFolderId = getOrCreateEventManagerFolder(driveService)
                    println("EventManager folder ID: $eventManagerFolderId")

                    eventManagerFolderId?.let {
                        folderInfos = listFolderInfos(driveService, it)
                        println("Folder infos retrieved: $folderInfos")
                    }
                } catch (e: Exception) {
                    println("Error occurred: ${e.message}")
                } finally {
                    isLoading = false
                    isRefresh = false
                }

            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Create New Folder") },
            text = {
                Column {
                    OutlinedTextField(
                        value = newFolderName,
                        onValueChange = { name ->
                            newFolderName = name
                            folderNameError = if (folderInfos.any { it.name == name }) {
                                "Folder with this name already exists."
                            } else {
                                null
                            }
                        },
                        label = { Text("Folder Name") },
                        isError = folderNameError != null
                    )
                    folderNameError?.let { error ->
                        Text(text = error, color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (folderNameError == null && newFolderName.isNotBlank()) {
                            isLoading = true
                            CoroutineScope(Dispatchers.IO).launch {
                                try {
                                    val credential = credentialsProvider.getCredentials()
                                    val driveService = getDriveService(credential)
                                    eventManagerFolderId?.let { parentId ->
                                        val newFolderId =
                                            createNewFolder(driveService, newFolderName, parentId)
                                        folderInfos = listFolderInfos(driveService, parentId)
                                        println("New folder created with ID: $newFolderId")
                                    }
                                } catch (e: Exception) {
                                    println("Error creating new folder: ${e.message}")
                                } finally {
                                    isLoading = false
                                    newFolderName = ""
                                }
                            }
                            showDialog = false
                        }
                    }
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Folder") },
            text = { Text("Are you sure you want to delete this folder?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        folderToDelete?.let { folderName ->
                            isLoading = true
                            CoroutineScope(Dispatchers.IO).launch {
                                try {
                                    val credential = credentialsProvider.getCredentials()
                                    val driveService = getDriveService(credential)
                                    eventManagerFolderId?.let { parentId ->
                                        deleteFolder(driveService, folderName, parentId)
                                        folderInfos = listFolderInfos(driveService, parentId)
                                        println("Folder deleted: $folderName")
                                    }
                                } catch (e: Exception) {
                                    println("Error deleting folder: ${e.message}")
                                } finally {
                                    isLoading = false
                                }
                            }
                        }
                        showDeleteDialog = false
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Confirm Logout") },
            text = { Text("Are you sure you want to log out?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        clearCredentials()
                        navController.popBackStack()
                        showLogoutDialog = false
                    }
                ) {
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("No")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(text = "Event Table Manager")
                },
                actions = {
                    IconButton(
                        modifier = Modifier.padding(16.dp),
                        enabled = !isRefresh,
                        onClick = { isRefresh = true },
                        content = {
                            Icon(
                                imageVector = Icons.Filled.Refresh,
                                contentDescription = "Refresh",
                            )
                        }
                    )

                    ElevatedButton(onClick = {
                        showLogoutDialog = true
                    }) {
                        Text("Log Out")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Add Folder")
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier.padding(top = innerPadding.calculateTopPadding())
        ) {
            Column {
                if (isLoading) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                ) {
                    items(folderInfos) { folderInfo ->
                        FolderCard(
                            folderName = folderInfo.name,
                            folderId = folderInfo.id,
                            onItemClick = { id ->
                                navigateToSearchScreen(id) // Передаем folderId в функцию навигации
                            },
                            onDeleteClick = {
                                folderToDelete = folderInfo.name
                                showDeleteDialog = true
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FolderCard(
    folderName: String,
    folderId: String,
    onItemClick: (String) -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onItemClick(folderId) }, // Передаем folderId при клике
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = folderName,
                style = MaterialTheme.typography.titleLarge,
            )
            IconButton(onClick = onDeleteClick) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete Folder")
            }
        }
    }
}

suspend fun getOrCreateEventManagerFolder(driveService: Drive): String {
    println("Checking for EventManager folder...")
    val result = driveService.files().list()
        .setQ("name = 'EventManager' and mimeType = 'application/vnd.google-apps.folder' and 'root' in parents")
        .setFields("files(id, name)")
        .execute()

    val files = result.files
    if (files != null && files.isNotEmpty()) {
        println("EventManager folder exists with ID: ${files[0].id}")
        return files[0].id
    }

    println("EventManager folder not found. Creating new folder...")
    val fileMetadata = File()
    fileMetadata.name = "EventManager"
    fileMetadata.mimeType = "application/vnd.google-apps.folder"
    fileMetadata.parents = listOf("root")

    val file = driveService.files().create(fileMetadata)
        .setFields("id")
        .execute()

    println("New EventManager folder created with ID: ${file.id}")
    return file.id
}

suspend fun listFolderInfos(driveService: Drive, folderId: String): List<FolderInfo> {
    println("Listing folders inside EventManager...")
    val result = driveService.files().list()
        .setQ("'$folderId' in parents and mimeType = 'application/vnd.google-apps.folder'")
        .setFields("files(id, name)")
        .execute()

    val folderInfos = result.files?.map { FolderInfo(it.name, it.id) } ?: emptyList()
    println("Folders found: $folderInfos")
    return folderInfos
}

suspend fun createNewFolder(driveService: Drive, folderName: String, parentId: String): String {
    println("Creating new folder: $folderName in parent ID: $parentId")
    val fileMetadata = File()
    fileMetadata.name = folderName
    fileMetadata.mimeType = "application/vnd.google-apps.folder"
    fileMetadata.parents = listOf(parentId)

    val file = driveService.files().create(fileMetadata)
        .setFields("id")
        .execute()

    println("New folder created with ID: ${file.id}")
    return file.id
}

suspend fun deleteFolder(driveService: Drive, folderName: String, parentId: String) {
    println("Deleting folder: $folderName in parent ID: $parentId")
    val result = driveService.files().list()
        .setQ("name = '$folderName' and '$parentId' in parents and mimeType = 'application/vnd.google-apps.folder'")
        .setFields("files(id)")
        .execute()

    val files = result.files
    if (files != null && files.isNotEmpty()) {
        val fileId = files[0].id
        driveService.files().delete(fileId).execute()
        println("Folder deleted with ID: $fileId")
    } else {
        println("Folder not found: $folderName")
    }
}