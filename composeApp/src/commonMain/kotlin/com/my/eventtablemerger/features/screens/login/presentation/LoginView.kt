package com.my.eventtablemerger.features.screens.login.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.FileList
import com.my.eventtablemerger.core.utils.FileLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import java.awt.Desktop
import java.io.File
import java.net.URI
import java.nio.file.Paths


@Composable
fun LoginView(
    credentialsProvider: CredentialsProvider = koinInject(),
    navigateToObserveScreen: () -> Unit,
    logger: FileLogger = koinInject()
) {
    var isAuthorized by remember { mutableStateOf(isUserAuthorized()) }
    var logMessage by remember { mutableStateOf("") } // State for log messages

    // Проверка авторизации при загрузке экрана
    LaunchedEffect(isAuthorized) {
        if (isAuthorized) {
            navigateToObserveScreen()
        }
    }

    LaunchedEffect(logMessage) {
        logger.log(logMessage)
    }

    Scaffold(
        topBar = {}
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = if (isAuthorized) "Authorized" else "Not Authorized",
                modifier = Modifier.fillMaxWidth()
            )

            Button(onClick = {
                // Запуск процесса авторизации в фоновом потоке
                CoroutineScope(Dispatchers.IO).launch {
                    logMessage = "Процесс авторизации запущен..." // Update log message
                    try {
                        logMessage = "Получение credential"
                        val credential = credentialsProvider.getCredentials()
                        logMessage = "Получение driveService"
                        val driveService = getDriveService(credential)
                        logMessage = "Запрос файлов"
                        listFiles(driveService)
                        isAuthorized = true
                        logMessage = "Авторизация прошла успешно." // Update log message
                    } catch (e: Exception) {
                        e.printStackTrace()
                        isAuthorized = false
                        logMessage = "Ошибка авторизации: ${e.message}" // Update log message
                    }
                }
            }) {
                Text("Authorize with Google")
            }

            Button(onClick = {
                // Запуск процесса получения файлов из корневой директории
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val credential = credentialsProvider.getCredentials()
                        val driveService = getDriveService(credential)
                        listRootFiles(driveService)
                        logMessage = "Файлы из корневой директории получены." // Update log message
                    } catch (e: Exception) {
                        e.printStackTrace()
                        logMessage = "Ошибка получения файлов: ${e.message}" // Update log message
                    }
                }
            }) {
                Text("List Root Directory Files")
            }

            Button(onClick = {
                // Очистка данных аутентификации
                clearCredentials()
                isAuthorized = false
                logMessage = "Вы вышли из системы." // Update log message
            }) {
                Text("Logout")
            }

            // Log message display
            Text(
                text = logMessage,
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                color = Color.Red // You can change the color as needed
            )
        }
    }
}

fun getDriveService(credential: Credential): Drive {
    return Drive.Builder(
        GoogleNetHttpTransport.newTrustedTransport(),
        JSON_FACTORY,
        credential
    )
        .setApplicationName("EventTableMerger")
        .build()
}

fun listFiles(driveService: Drive) {
    val result: FileList = driveService.files().list()
        .setPageSize(10)
        .setFields("nextPageToken, files(id, name)")
        .execute()
    val files = result.files
    if (files == null || files.isEmpty()) {
        println("No files found.")
    } else {
        println("Files:")
        for (file in files) {
            println("${file.name} (${file.id})")
        }
    }
}

fun listRootFiles(driveService: Drive) {
    val result: FileList = driveService.files().list()
        .setQ("'root' in parents")
        .setPageSize(10)
        .setFields("nextPageToken, files(id, name)")
        .execute()
    val files = result.files
    if (files == null || files.isEmpty()) {
        println("No files found in root directory.")
    } else {
        println("Root Directory Files:")
        for (file in files) {
            println("${file.name} (${file.id})")
        }
    }
}

fun isUserAuthorized(): Boolean {
    val tokensDir = File(TOKENS_DIRECTORY_PATH)
    return tokensDir.exists() && tokensDir.listFiles()?.isNotEmpty() == true
}

interface CredentialsProvider {
    suspend fun getCredentials(): Credential
}

interface AuthorizationCodeReceiver {
    fun getRedirectUri(): String
    fun stop()
    suspend fun waitForCode(): String
}

fun clearCredentials() {
    val tokensDir = File(TOKENS_DIRECTORY_PATH)
    if (tokensDir.exists()) {
        tokensDir.deleteRecursively()
    }
}

val TOKENS_DIRECTORY_PATH = Paths.get(System.getProperty("user.home"), ".eventtablemerger", "tokens").toString()
val JSON_FACTORY: JsonFactory = GsonFactory.getDefaultInstance()
val SCOPES = listOf(
    "https://www.googleapis.com/auth/drive",
    "https://www.googleapis.com/auth/drive.file"
)

fun openBrowser(url: String) {
    if (Desktop.isDesktopSupported()) {
        Desktop.getDesktop().browse(URI(url))
    } else {
        println("Desktop is not supported. Please open the following URL manually: $url")
    }
}