package com.my.eventtablemerger

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draganddrop.awtTransferable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.google.api.client.auth.oauth2.AuthorizationCodeRequestUrl
import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.util.store.FileDataStoreFactory
import com.my.eventtablemerger.core.di.initKoin
import com.my.eventtablemerger.features.screens.login.presentation.CredentialsProvider
import com.my.eventtablemerger.features.screens.login.presentation.JSON_FACTORY
import com.my.eventtablemerger.features.screens.login.presentation.SCOPES
import com.my.eventtablemerger.features.screens.login.presentation.TOKENS_DIRECTORY_PATH
import com.my.eventtablemerger.features.screens.search.ExcelOperations
import com.my.eventtablemerger.features.screens.search.LeaderUser
import com.my.eventtablemerger.features.screens.search.PlatformConfiguration
import com.my.eventtablemerger.features.screens.search.PlatformWindowMeasure
import com.my.eventtablemerger.test.authorizeUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.DataFormatter
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import java.awt.Desktop
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.net.URI
import javax.swing.JFileChooser
import javax.swing.JFrame

@Composable
fun DesktopTheme(
    content: @Composable () -> Unit
) {

    MaterialTheme(
        colorScheme = darkColorScheme()
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
            content = content
        )
    }
}

fun main() {
    initKoin(platformModules = listOf(
        desktopCredentialsProviderModule,
        desktopPlatformConfigurationModule,
        desktopPlatformWindowMeasureModule,
        desktopExcelOperationsModule

    )) {}
    application {
        Window(onCloseRequest = ::exitApplication, title = "EventTableMerger") {
            App()

            /*var selectedFiles by remember { mutableStateOf<Set<File>>(emptySet()) }
            var message by remember { mutableStateOf("") }
            var isProcessing by remember { mutableStateOf(false) }
            var isDragOver by remember { mutableStateOf(false) }
            val scope = rememberCoroutineScope()

            // Применение DropTarget к текущему окну
            LaunchedEffect(Unit) {
                val awtWindow = window
                DropTarget(
                    awtWindow,
                    DnDConstants.ACTION_COPY,
                    object : java.awt.dnd.DropTargetListener {
                        override fun dragEnter(dtde: java.awt.dnd.DropTargetDragEvent?) {
                            isDragOver = true
                        }

                        override fun dragOver(dtde: java.awt.dnd.DropTargetDragEvent?) {}
                        override fun dropActionChanged(dtde: java.awt.dnd.DropTargetDragEvent?) {}
                        override fun dragExit(dte: java.awt.dnd.DropTargetEvent?) {
                            isDragOver = false
                        }

                        override fun drop(dtde: DropTargetDropEvent) {
                            dtde.acceptDrop(DnDConstants.ACTION_COPY)
                            val transferable = dtde.transferable
                            val files =
                                transferable.getTransferData(DataFlavor.javaFileListFlavor) as List<File>
                            val newFiles =
                                files.filter { it.name.endsWith(".xlsx") || it.name.endsWith(".xls") }
                                    .toSet()
                            selectedFiles = selectedFiles + newFiles
                            message = "Выбрано файлов: ${selectedFiles.size}"
                            isDragOver = false
                        }
                    },
                    true
                )
            }

            DesktopTheme {
                DesktopApp(
                    selectedFiles = selectedFiles,
                    message = message,
                    isProcessing = isProcessing,
                    isDragOver = isDragOver,
                    onFilesSelected = { selectedFiles = it },
                    onMessageChange = { message = it },
                    onProcessingChange = { isProcessing = it },
                    scope = scope
                )
            }*/
            authorizeUser()
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class)
@Composable
@Preview
fun DesktopApp(
    selectedFiles: Set<File>,
    message: String,
    isProcessing: Boolean,
    isDragOver: Boolean,
    onFilesSelected: (Set<File>) -> Unit,
    onMessageChange: (String) -> Unit,
    onProcessingChange: (Boolean) -> Unit,
    scope: CoroutineScope
) {
    var showTargetBorder by remember { mutableStateOf(false) }
    var logMessage by remember { mutableStateOf("") } // State for log messages

    val dragAndDropTarget = remember {
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
                onFilesSelected(selectedFiles + newFiles)
                onMessageChange("Выбрано файлов: ${selectedFiles.size + newFiles.size}")
                showTargetBorder = false
                return true
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Область для перетаскивания файлов
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .then(
                    if (showTargetBorder)
                        Modifier.border(2.dp, Color.Blue)
                    else
                        Modifier.border(2.dp, Color.Gray)
                )
                .dragAndDropTarget(
                    shouldStartDragAndDrop = { true },
                    target = dragAndDropTarget
                )
        ) {
            Text("Перетащите файлы сюда", modifier = Modifier.align(Alignment.Center))
        }

        // Кнопка выбора файлов
        Button(onClick = {
            val chooser = JFileChooser()
            chooser.isMultiSelectionEnabled = true
            val result = chooser.showOpenDialog(null)
            if (result == JFileChooser.APPROVE_OPTION) {
                val newFiles = chooser.selectedFiles.filter { it.name.endsWith(".xlsx") || it.name.endsWith(".xls") }.toSet()
                onFilesSelected(selectedFiles + newFiles)
                onMessageChange("Выбрано файлов: ${selectedFiles.size}")
            }
        }) {
            Text("Выбрать Excel файлы")
        }

        // Список выбранных файлов
        LazyColumn(
            modifier = Modifier.fillMaxWidth().height(200.dp).border(1.dp, Color.Gray)
        ) {
            items(selectedFiles.toList()) { file ->
                FileListItem(
                    file = file,
                    onRemove = {
                        onFilesSelected(selectedFiles - file)
                        onMessageChange("Выбрано файлов: ${selectedFiles.size - 1}")
                    }
                )
            }
        }

        // Кнопка обработки файлов
        if (selectedFiles.isNotEmpty()) {
            Button(
                onClick = {
                    scope.launch {
                        onProcessingChange(true)
                        onMessageChange("Обработка файлов...")
                        try {
                            withContext(Dispatchers.IO) {
                                val allData = selectedFiles.flatMap { readExcel(it) }
                                val output = File(selectedFiles.first().parent, "merged_output.xlsx")
                                mergeToExcel(allData, output)
                            }
                            onMessageChange("Уникальные записи сохранены в: merged_output.xlsx")
                            logMessage = "Обработка завершена успешно." // Update log message
                        } catch (e: Exception) {
                            onMessageChange("Ошибка при обработке файлов: ${e.message}")
                            logMessage = "Ошибка: ${e.message}" // Update log message
                        } finally {
                            onProcessingChange(false)
                        }
                    }
                },
                enabled = !isProcessing
            ) {
                Text(if (isProcessing) "Обработка..." else "Обработать файлы")
            }
        }

        Text(message)

        // Log message display
        Text(
            text = logMessage,
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
            color = Color.Red // You can change the color as needed
        )
    }
}

@Composable
private fun FileListItem(file: File, onRemove: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        /*Icon(
            imageVector = vectorResource(Res.drawabl),
            contentDescription = "Excel file",
            modifier = Modifier.size(96.dp),
            tint = Color(0xFF1D6F42) // Зеленый цвет, ассоциирующийся с Excel
        )*/
        Spacer(Modifier.width(8.dp))
        Text(
            text = file.name,
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = onRemove) {
            Icon(Icons.Default.Close, contentDescription = "Remove file")
        }
    }
}

suspend fun readExcel(file: File): List<Pair<String, String>> = withContext(Dispatchers.IO) {
    val inputStream = FileInputStream(file)
    val workbook = WorkbookFactory.create(inputStream)
    val sheet = workbook.getSheetAt(0)
    val data = mutableListOf<Pair<String, String>>()
    val formatter = DataFormatter()

    for (row in sheet) {
        val idCell = row.getCell(0)
        val nameCell = row.getCell(1)
        if (idCell != null && nameCell != null) {
            val id = when (idCell.cellType) {
                CellType.NUMERIC -> idCell.numericCellValue.toLong().toString()
                else -> formatter.formatCellValue(idCell)
            }.trim()
            val name = formatter.formatCellValue(nameCell).trim()
            data.add(id to name)
        }
    }

    inputStream.close()
    workbook.close()
    return@withContext data
}

suspend fun mergeToExcel(data: List<Pair<String, String>>, output: File) =
    withContext(Dispatchers.IO) {
        val unique = data.toSet()
        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("Участники")

        unique.forEachIndexed { index, (id, name) ->
            val row = sheet.createRow(index)
            row.createCell(0).setCellValue(id)
            row.createCell(1).setCellValue(name)
        }

        FileOutputStream(output).use { workbook.write(it) }
        workbook.close()
    }


class DesktopExcelOperations : ExcelOperations {
    override suspend fun readExcelToMerge(file: File): List<LeaderUser> = withContext(Dispatchers.IO) {
        val inputStream = FileInputStream(file)
        val workbook = WorkbookFactory.create(inputStream)
        val sheet = workbook.getSheetAt(0)
        val data = mutableListOf<LeaderUser>()
        val formatter = DataFormatter()

        for (rowIndex in 1 until sheet.physicalNumberOfRows) {
            val row = sheet.getRow(rowIndex)
            val idCell = row.getCell(0)
            val nameCell = row.getCell(1)
            val ageCell = row.getCell(2)
            val companyCell = row.getCell(3)
            val jobTitleCell = row.getCell(4)
            val emailCell = row.getCell(11)
            val phoneCell = row.getCell(12)
            val cityCell = row.getCell(13)

            if (idCell != null && nameCell != null && ageCell != null && emailCell != null && phoneCell != null && cityCell != null) {
                val id = when (idCell.cellType) {
                    CellType.NUMERIC -> idCell.numericCellValue.toLong().toString()
                    else -> formatter.formatCellValue(idCell)
                }.trim()
                val name = formatter.formatCellValue(nameCell).trim()
                val age = formatter.formatCellValue(ageCell).toIntOrNull() ?: 0
                val company = companyCell?.let { formatter.formatCellValue(it).takeIf { it.isNotBlank() } }
                val jobTitle = jobTitleCell?.let { formatter.formatCellValue(it).takeIf { it.isNotBlank() } }
                val email = formatter.formatCellValue(emailCell).trim()
                val phone = formatter.formatCellValue(phoneCell).trim()
                val city = formatter.formatCellValue(cityCell).trim()

                data.add(LeaderUser(id, name, company, jobTitle, age, email, phone, city))
            }
        }

        inputStream.close()
        workbook.close()
        return@withContext data
    }

    override suspend fun readMergedOutput(file: File): List<LeaderUser> = withContext(Dispatchers.IO) {
        val inputStream = FileInputStream(file)
        val workbook = WorkbookFactory.create(inputStream)
        val sheet = workbook.getSheetAt(0)
        val data = mutableListOf<LeaderUser>()
        val formatter = DataFormatter()

        for (rowIndex in 0 until sheet.physicalNumberOfRows) {
            val row = sheet.getRow(rowIndex)
            val idCell = row.getCell(0)
            val nameCell = row.getCell(1)
            val ageCell = row.getCell(2)
            val companyCell = row.getCell(3)
            val jobTitleCell = row.getCell(4)
            val emailCell = row.getCell(5)
            val phoneCell = row.getCell(6)
            val cityCell = row.getCell(7)

            if (idCell != null && nameCell != null && ageCell != null && emailCell != null && phoneCell != null && cityCell != null) {
                val id = when (idCell.cellType) {
                    CellType.NUMERIC -> idCell.numericCellValue.toLong().toString()
                    else -> formatter.formatCellValue(idCell)
                }.trim()
                val name = formatter.formatCellValue(nameCell).trim()
                val age = formatter.formatCellValue(ageCell).toIntOrNull() ?: 0
                val company = companyCell?.let { formatter.formatCellValue(it).takeIf { it.isNotBlank() } }
                val jobTitle = jobTitleCell?.let { formatter.formatCellValue(it).takeIf { it.isNotBlank() } }
                val email = formatter.formatCellValue(emailCell).trim()
                val phone = formatter.formatCellValue(phoneCell).trim()
                val city = formatter.formatCellValue(cityCell).trim()

                data.add(LeaderUser(id, name, company, jobTitle, age, email, phone, city))
            }
        }

        inputStream.close()
        workbook.close()
        return@withContext data
    }

    override suspend fun mergeToExcel(data: List<LeaderUser>, output: File) =
        withContext(Dispatchers.IO) {
            val unique = data.toSet()
            val workbook = XSSFWorkbook()
            val sheet = workbook.createSheet("Участники")

            unique.forEachIndexed { index, leaderUser ->
                val row = sheet.createRow(index)
                row.createCell(0).setCellValue(leaderUser.id)
                row.createCell(1).setCellValue(leaderUser.fio)
                row.createCell(2).setCellValue(leaderUser.age.toDouble())
                row.createCell(3).setCellValue(leaderUser.company ?: "")
                row.createCell(4).setCellValue(leaderUser.jobTitle ?: "")
                row.createCell(5).setCellValue(leaderUser.email)
                row.createCell(6).setCellValue(leaderUser.phone)
                row.createCell(7).setCellValue(leaderUser.city)
            }

            FileOutputStream(output).use { workbook.write(it) }
            workbook.close()
        }
}



val desktopExcelOperationsModule = module {
    singleOf(::DesktopExcelOperations) { bind<ExcelOperations>() }
}

class DesktopCredentialsProvider : CredentialsProvider {
    override suspend fun getCredentials(): Credential {
        val inputStream = DesktopCredentialsProvider::class.java.getResourceAsStream("/credentials.json")
//        val inputStream = File("C:\\Users\\abwfaat\\Desktop\\credentials.json").inputStream()
        val clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, InputStreamReader(inputStream))

        val flow = GoogleAuthorizationCodeFlow.Builder(
            GoogleNetHttpTransport.newTrustedTransport(),
            JSON_FACTORY,
            clientSecrets,
            SCOPES
        )
            .setDataStoreFactory(FileDataStoreFactory(File(TOKENS_DIRECTORY_PATH)))
            .setAccessType("offline")
            .build()

        val receiver = LocalServerReceiver.Builder().setPort(8889).build()
        val authApp = object : AuthorizationCodeInstalledApp(flow, receiver) {
            override fun onAuthorization(authorizationUrl: AuthorizationCodeRequestUrl?) {
                val url = authorizationUrl?.build()
                println("Открой ссылку вручную в браузере: $authorizationUrl")
                try {
                    val uri = URI(url)
                    if (Desktop.isDesktopSupported()) {
                        Desktop.getDesktop().browse(uri)
                    } else {
                        println("Desktop API не поддерживается на этой платформе.")
                    }
                } catch (e: Exception) {
                    println("Не удалось открыть браузер: ${e.message}")
                }
            }
        }
        return authApp.authorize("user")
    }

}



val desktopCredentialsProviderModule = module {
    singleOf(::DesktopCredentialsProvider) { bind<CredentialsProvider>() }
}

class DesktopPlatformConfiguration : PlatformConfiguration {
    @Composable
    override fun rememberPlatformConfiguration(): Int {
        val frame = JFrame()
        val screenWidthPx =  mutableStateOf(frame.width)
        val currentScreenWidthPx = rememberUpdatedState(screenWidthPx.value)

        LaunchedEffect(frame) {
            frame.addComponentListener(object : ComponentAdapter() {
                override fun componentResized(e: ComponentEvent?) {
                    // Обновляем ширину окна при изменении размера
                    screenWidthPx.value = frame.width
                }
            })
        }

        val density = Toolkit.getDefaultToolkit().screenResolution / 96.0 // Assuming 96 DPI as base
        return (currentScreenWidthPx.value / density).toInt()
    }
}

val desktopPlatformConfigurationModule = module {
    singleOf(::DesktopPlatformConfiguration) { bind<PlatformConfiguration>() }
}

class DesktopPlatformWindowMeasure: PlatformWindowMeasure {
    @Composable
    override fun getHeight(): Dp {
        val windowState = rememberWindowState()
        val height by remember { derivedStateOf { windowState.size.height } }
        return height

    }

    @Composable
    override fun getWidth(): Dp {
        val windowState = rememberWindowState()
        val width by remember { derivedStateOf { windowState.size.width } }
        return width
    }
}

val desktopPlatformWindowMeasureModule = module {
    singleOf(::DesktopPlatformWindowMeasure) { bind<PlatformWindowMeasure>() }
}