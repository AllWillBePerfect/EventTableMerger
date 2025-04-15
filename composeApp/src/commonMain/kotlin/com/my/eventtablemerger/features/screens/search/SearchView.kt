package com.my.eventtablemerger.features.screens.search

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draganddrop.awtTransferable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.api.client.http.FileContent
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File as DriveFile
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.my.eventtablemerger.features.screens.login.presentation.CredentialsProvider
import com.my.eventtablemerger.features.screens.login.presentation.getDriveService
import com.my.eventtablemerger.features.screens.observe.FolderInfo
import eventtablemerger.composeapp.generated.resources.Res
import eventtablemerger.composeapp.generated.resources.microsoft_excel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.vectorResource
import org.koin.compose.koinInject
import java.awt.datatransfer.DataFlavor
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import javax.swing.JFileChooser

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchView(
    globalNavController: NavController,
    folderId: String,
    platformWindowMeasure: PlatformWindowMeasure = koinInject(),
    credentialsProvider: CredentialsProvider = koinInject()
) {
    val innerNavController = rememberNavController()
    val screenWidthDp = platformWindowMeasure.getWidth()
    val screenHeightDp = platformWindowMeasure.getHeight()

    val isWideScreen = screenWidthDp >= 600.dp

    var folderInfo by remember { mutableStateOf<List<FolderInfo>?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var parentFolderName by remember { mutableStateOf("Loading...") }

    LaunchedEffect(folderId) {
        isLoading = true
        try {
            withContext(Dispatchers.IO) {
                val credential = credentialsProvider.getCredentials()
                val driveService = getDriveService(credential)

                // Получаем название родительской папки
                parentFolderName = fetchFolderName(driveService, folderId)

                folderInfo = fetchSubFolders(driveService, folderId)
            }
            println("Folder info retrieved: $folderInfo")
        } catch (e: Exception) {
            println("Error fetching folder info: ${e.message}")
        } finally {
            isLoading = false
        }
    }

    var selectedFiles by remember { mutableStateOf<Set<File>>(emptySet()) }
    var message by remember { mutableStateOf("") }
    var isProcessing by remember { mutableStateOf(false) }
    var isDragOver by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var outputData by remember { mutableStateOf<List<LeaderUser>>(emptyList()) }

    LaunchedEffect(message) {
        print("Message updated: $message")
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(parentFolderName) },
                navigationIcon = {
                    IconButton(onClick = { globalNavController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = if (!isWideScreen && innerNavController.previousBackStackEntry != null) {
                    {
                        IconButton(onClick = { innerNavController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                } else {
                    {}
                }
            )
        }
    ) { innerPadding ->
        if (isLoading) {
            // Show loading indicator
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (isWideScreen) {
            Row(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
            ) {
                ScreenA(
                    modifier = Modifier.weight(1f),
                    selectedFiles = selectedFiles,
                    message = message,
                    isProcessing = isProcessing,
                    isDragOver = isDragOver,
                    updateFiles = { selectedFiles = it },
                    onMessageChange = { message = it },
                    onProcessingChange = { isProcessing = it },
                    scope = scope,
                    folderId = folderId,
                    onOutputDataReady = { data -> outputData = data } // Capture the output data
                )
                ScreenB(modifier = Modifier.weight(1f), outputData = outputData) // Pass the data to Screen B
            }
        } else {
            NavHost(
                navController = innerNavController,
                startDestination = "screenB",
                modifier = Modifier.padding(innerPadding)
            ) {
                composable("screenA") {
                    ScreenA(
                        selectedFiles = selectedFiles,
                        message = message,
                        isProcessing = isProcessing,
                        isDragOver = isDragOver,
                        updateFiles = { selectedFiles = it },
                        onMessageChange = { message = it },
                        onProcessingChange = { isProcessing = it },
                        scope = scope,
                        folderId = folderId,
                        onOutputDataReady = { data -> outputData = data } // Capture the output data
                    )
                }
                composable("screenB") {
                    ScreenB(
                        onOpenScreenA = {
                            innerNavController.navigate("screenA")
                        },
                        outputData = outputData // Pass the data to Screen B
                    )
                }
            }
        }
    }
}

suspend fun fetchSubFolders(driveService: Drive, parentFolderId: String): List<FolderInfo> {
    val result = driveService.files().list()
        .setQ("'$parentFolderId' in parents and mimeType = 'application/vnd.google-apps.folder'")
        .setFields("files(id, name)")
        .execute()

    return result.files.map { file -> FolderInfo(file.name, file.id) }
}

suspend fun fetchFolderName(driveService: Drive, folderId: String): String {
    val result = driveService.files().get(folderId).setFields("name").execute()
    return result.name
}

@Composable
fun ScreenB(modifier: Modifier = Modifier, onOpenScreenA: () -> Unit = {}, outputData: List<LeaderUser>) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredItems = outputData.filter { it.fio.contains(searchQuery, ignoreCase = true) }
    val clipboardManager = LocalClipboardManager.current

    LaunchedEffect(outputData) {
        println("Data updated: $outputData")
    }

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Search") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(36.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (filteredItems.isEmpty()) {
            Text(
                text = "Список пуст",
                modifier = Modifier.padding(8.dp)
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().fillMaxHeight()
            ) {
                items(filteredItems) { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                // Copy the ID to clipboard
                                clipboardManager.setText(AnnotatedString(item.id))
                                println("ID copied to clipboard: ${item.id}")
                            },
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = "ID: ${item.id}", style = MaterialTheme.typography.titleMedium)
                            Text(text = "ФИО: ${item.fio}", style = MaterialTheme.typography.titleMedium)
                            Text(text = "Компания: ${item.company ?: "N/A"}", style = MaterialTheme.typography.bodyMedium)
                            Text(text = "Должность: ${item.jobTitle ?: "N/A"}", style = MaterialTheme.typography.bodyMedium)
                            Text(text = "Возраст: ${item.age}", style = MaterialTheme.typography.bodyMedium)
                            Text(text = "Email: ${item.email}", style = MaterialTheme.typography.bodyMedium)
                            Text(text = "Телефон: ${item.phone}", style = MaterialTheme.typography.bodyMedium)
                            Text(text = "Город: ${item.city}", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
@Composable
fun ScreenA(
    modifier: Modifier = Modifier,
    selectedFiles: Set<File>,
    message: String,
    isProcessing: Boolean,
    isDragOver: Boolean,
    updateFiles: (Set<File>) -> Unit,
    onMessageChange: (String) -> Unit,
    onProcessingChange: (Boolean) -> Unit,
    scope: CoroutineScope,
    folderId: String,
    excelOperations: ExcelOperations = koinInject(),
    credentialsProvider: CredentialsProvider = koinInject(),
    onOutputDataReady: (List<LeaderUser>) -> Unit // Callback to pass data to Screen B
) {
    var showTargetBorder by remember { mutableStateOf(false) }
    var versionCount by remember { mutableStateOf(0) }
    var lastUpdateDate by remember { mutableStateOf("N/A") }
    var recordDifference by remember { mutableStateOf(0) }

    // Function to update version info and record difference
    suspend fun updateVersionInfo() {
        withContext(Dispatchers.IO) {
            val driveService = getDriveService(credentialsProvider.getCredentials())

            // Fetch existing folders to determine the number of versions
            val existingFolders = driveService.files().list()
                .setQ("'$folderId' in parents and mimeType = 'application/vnd.google-apps.folder'")
                .setFields("files(name, id)")
                .execute()
                .files

            versionCount = existingFolders.size

            // Find the folder with the highest version number
            val latestVersionFolder = existingFolders.maxByOrNull { file ->
                file.name.trim().removePrefix("v").toIntOrNull() ?: 0
            }

            latestVersionFolder?.let { folder ->
                launch {
                    // Fetch meta.json from the latest version folder
                    val metaFile = driveService.files().list()
                        .setQ("'${folder.id}' in parents and name = 'meta.json'")
                        .setFields("files(id, name)")
                        .execute()
                        .files
                        .firstOrNull()

                    metaFile?.let {
                        val metaContent = driveService.files().get(it.id).executeMediaAsInputStream().reader().readText()
                        val gson = Gson()
                        val metaJson = gson.fromJson(metaContent, JsonObject::class.java)
                        lastUpdateDate = metaJson.get("date").asString
                        val latestRecordCount = metaJson.get("recordCount").asInt

                        // Fetch previous meta.json to calculate record difference
                        val previousVersionFolder = existingFolders
                            .filter { it.id != folder.id }
                            .maxByOrNull { file -> file.name.trim().removePrefix("v").toIntOrNull() ?: 0 }

                        val previousRecordCount = previousVersionFolder?.let { prevFolder ->
                            val prevMetaFile = driveService.files().list()
                                .setQ("'${prevFolder.id}' in parents and name = 'meta.json'")
                                .setFields("files(id, name)")
                                .execute()
                                .files
                                .firstOrNull()

                            prevMetaFile?.let { prevMeta ->
                                val prevMetaContent = driveService.files().get(prevMeta.id).executeMediaAsInputStream().reader().readText()
                                val prevMetaJson = gson.fromJson(prevMetaContent, JsonObject::class.java)
                                prevMetaJson.get("recordCount").asInt
                            }
                        } ?: latestRecordCount

                        recordDifference = latestRecordCount - previousRecordCount


                    }
                }

                launch {
                    // Fetch merged_output.xlsx from the latest version folder
                    val mergedFile = driveService.files().list()
                        .setQ("'${folder.id}' in parents and name = 'merged_output.xlsx'")
                        .setFields("files(id, name)")
                        .execute()
                        .files
                        .firstOrNull()

                    mergedFile?.let {
                        val outputStream = File.createTempFile("temp_merged_output", ".xlsx")
                        driveService.files().get(it.id).executeMediaAndDownloadTo(outputStream.outputStream())
                        val outputData = excelOperations.readMergedOutput(outputStream)
                        onOutputDataReady(outputData) // Pass the data to Screen B
                    }
                }
            }
        }
    }

    LaunchedEffect(folderId) {
        try {
            updateVersionInfo()


        } catch (e: Exception) {
            println("Error fetching version info or initial data: ${e.message}")
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Image(
                        imageVector = vectorResource(Res.drawable.microsoft_excel),
                        contentDescription = "Excel file",
                        modifier = Modifier.size(96.dp),
                    )
                    Text(text = "Количество изменений: $versionCount")
                }
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = "Дата обновления: $lastUpdateDate")
                    Text(
                        buildAnnotatedString {
                            append("Участники: ")
                            withStyle(style = SpanStyle(color = when {
                                recordDifference > 0 -> Color.Green
                                recordDifference < 0 -> Color.Red
                                else -> Color.Unspecified
                            })
                            ) {
                                append(if (recordDifference > 0) "+$recordDifference" else recordDifference.toString())
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f)) // Заполняет оставшееся пространство

            // Область для перетаскивания файлов и отображения списка
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .then(
                        if (showTargetBorder)
                            Modifier.border(2.dp, Color.Blue)
                        else
                            Modifier.border(2.dp, Color.Gray)
                    )
                    .dragAndDropTarget(
                        shouldStartDragAndDrop = { true },
                        target = remember {
                            object : DragAndDropTarget {
                                override fun onStarted(event: DragAndDropEvent) {
                                    showTargetBorder = true
                                }

                                override fun onEnded(event: DragAndDropEvent) {
                                    showTargetBorder = false
                                }

                                override fun onDrop(event: DragAndDropEvent): Boolean {
                                    val transferable = event.awtTransferable
                                    val files = transferable.getTransferData(DataFlavor.javaFileListFlavor) as? List<File> ?: emptyList()
                                    val newFiles = files.filter { it.name.endsWith(".xlsx") || it.name.endsWith(".xls") }.toSet()
                                    println("Before update: $selectedFiles")
                                    updateFiles(selectedFiles + newFiles)
                                    println("After update: ${selectedFiles + newFiles}")
                                    onMessageChange("Выбрано файлов: ${selectedFiles.size + newFiles.size}")
                                    showTargetBorder = false
                                    return true
                                }
                            }
                        }
                    )
            ) {
                if (selectedFiles.isEmpty())
                    Text("Перетащите файлы сюда", modifier = Modifier.padding(bottom = 16.dp).align(Alignment.BottomCenter))
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 256.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(selectedFiles.toList()) { file ->
                        FileListItem(
                            file = file,
                            onRemove = {
                                updateFiles(selectedFiles - file)
                                onMessageChange("Выбрано файлов: ${selectedFiles.size - 1}")
                            }
                        )
                    }
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .padding(8.dp)
                                .clickable {
                                    val chooser = JFileChooser()
                                    chooser.isMultiSelectionEnabled = true
                                    val result = chooser.showOpenDialog(null)
                                    if (result == JFileChooser.APPROVE_OPTION) {
                                        val newFiles =
                                            chooser.selectedFiles.filter { it.name.endsWith(".xlsx") || it.name.endsWith(".xls") }
                                                .toSet()
                                        updateFiles(selectedFiles + newFiles)
                                        onMessageChange("Выбрано файлов: ${selectedFiles.size + newFiles.size}")
                                    }
                                },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(0.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add Excel files",
                                    modifier = Modifier.size(48.dp),
                                    tint = Color(0xFF1D6F42) // Зеленый цвет, ассоциирующийся с Excel
                                )
                                Text("Добавить Excel файлы")

                            }
                        }
                    }
                }
            }

            Button(
                onClick = {
                    scope.launch {
                        onProcessingChange(true)
                        onMessageChange("Обработка файлов...")
                        var newFolderId: String? = null
                        try {
                            withContext(Dispatchers.IO) {
                                val allData = selectedFiles.flatMap { excelOperations.readExcelToMerge(it) }

                                // Create new folder in Google Drive
                                val driveService = getDriveService(credentialsProvider.getCredentials())

                                // Fetch existing folders to determine the next version number
                                val existingFolders = driveService.files().list()
                                    .setQ("'$folderId' in parents and mimeType = 'application/vnd.google-apps.folder'")
                                    .setFields("files(name)")
                                    .execute()
                                    .files

                                val nextVersion = if (existingFolders.isEmpty()) {
                                    1
                                } else {
                                    val versionNumbers = existingFolders.mapNotNull { file ->
                                        file.name.trim().removePrefix("v").toIntOrNull()
                                    }
                                    (versionNumbers.maxOrNull() ?: 0) + 1
                                }

                                val newFolderName = "v$nextVersion"

                                val folderMetadata = DriveFile()
                                folderMetadata.name = newFolderName
                                folderMetadata.mimeType = "application/vnd.google-apps.folder"
                                folderMetadata.parents = listOf(folderId)

                                val folder = driveService.files().create(folderMetadata)
                                    .setFields("id")
                                    .execute()
                                newFolderId = folder.id

                                // Save merged Excel file to Google Drive
                                val output = File.createTempFile("merged_output", ".xlsx")
                                excelOperations.mergeToExcel(allData, output)

                                val fileMetadata = DriveFile()
                                fileMetadata.name = "merged_output.xlsx"
                                fileMetadata.parents = listOf(newFolderId)

                                val fileContent = FileContent("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", output)
                                driveService.files().create(fileMetadata, fileContent)
                                    .setFields("id")
                                    .execute()

                                // Read the merged output file to extract ID and name
                                val outputData = excelOperations.readMergedOutput(output)
                                onOutputDataReady(outputData) // Pass the data to Screen B

                                // Create meta.json with current date and time
                                val metaFile = File.createTempFile("meta", ".json")
                                val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                                val metaContent = """{"date": "${dateFormat.format(Date())}", "recordCount": ${outputData.size}}"""
                                metaFile.writeText(metaContent)

                                val metaFileMetadata = DriveFile()
                                metaFileMetadata.name = "meta.json"
                                metaFileMetadata.parents = listOf(newFolderId)

                                val metaFileContent = FileContent("application/json", metaFile)
                                driveService.files().create(metaFileMetadata, metaFileContent)
                                    .setFields("id")
                                    .execute()
                            }
                            onMessageChange("Unique records saved in folder ID: $newFolderId")
                            updateFiles(emptySet()) // Clear selected files after processing

                            // Update version info and record difference after merging
                            updateVersionInfo()
                        } catch (e: Exception) {
                            onMessageChange("Error during processing: ${e.message}")
                        } finally {
                            onProcessingChange(false)
                        }
                    }
                },
                enabled = selectedFiles.isNotEmpty() && !isProcessing,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White
                    )
                } else {
                    Text("Обработать файлы")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            /*Text(
                text = message,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )*/
        }
    }
}

@Composable
private fun FileListItem(file: File, onRemove: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                imageVector = vectorResource(Res.drawable.microsoft_excel), // Используйте подходящую иконку
                contentDescription = "Excel file",
                modifier = Modifier.size(48.dp),
            )
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = file.name,
                    modifier = Modifier.weight(1f),
//                    maxLines = 1,
//                    overflow = TextOverflow.Ellipsis
                )
                IconButton(onClick = onRemove) {
                    Icon(Icons.Default.Close, contentDescription = "Remove file")
                }
            }
        }
    }
}

interface ExcelOperations {
    suspend fun readExcelToMerge(file: File): List<LeaderUser>
    suspend fun readMergedOutput(file: File): List<LeaderUser>
    suspend fun mergeToExcel(data: List<LeaderUser>, output: File)
}

data class LeaderUser(
    val id: String,
    val fio: String,
    val company: String?,
    val jobTitle: String?,
    val age: Int,
    val email: String,
    val phone: String,
    val city: String,
)

interface PlatformConfiguration {
    @Composable
    fun rememberPlatformConfiguration(): Int
}

interface PlatformWindowMeasure {
    @Composable
    fun getHeight(): Dp

    @Composable
    fun getWidth(): Dp
}