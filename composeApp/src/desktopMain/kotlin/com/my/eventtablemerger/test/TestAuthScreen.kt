package com.my.eventtablemerger.test

import com.google.api.client.auth.oauth2.AuthorizationCodeRequestUrl
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.util.store.FileDataStoreFactory
import com.my.eventtablemerger.core.utils.FileLogger
import io.ktor.util.reflect.instanceOf
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import java.awt.Desktop
import java.io.File
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.URI
import java.nio.file.Paths
import java.time.LocalDateTime


fun authorizeUser(
    logger: FileLogger
) {
    val httpTransport: NetHttpTransport = GoogleNetHttpTransport.newTrustedTransport()
    val jsonFactory = GsonFactory.getDefaultInstance()

    // Читай client_secret.json из ресурсов
    val clientSecrets = GoogleClientSecrets.load(
        jsonFactory,
        InputStreamReader(ClassLoader.getSystemResourceAsStream("credentials.json"))
    )

    val scopes = listOf("https://www.googleapis.com/auth/drive.file")

    val dir = Paths.get(System.getProperty("user.home"), ".eventtablemerger", "tokens").toString()

    val flow = GoogleAuthorizationCodeFlow.Builder(
        httpTransport,
        jsonFactory,
        clientSecrets,
        scopes
    ).setDataStoreFactory(FileDataStoreFactory(File(dir)))
        .setAccessType("offline")
        .build()

    val receiver = LocalServerReceiver.Builder().setPort(8888).build()

    val authApp = object : AuthorizationCodeInstalledApp(flow, receiver) {
        override fun onAuthorization(authorizationUrl: AuthorizationCodeRequestUrl?) {
            val url = authorizationUrl?.build()
            logger.log("Попытка открыть браузер: $url")
            println("Если браузер не открылся, перейди по ссылке вручную: $url")

            try {
                val uri = URI(url)
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().browse(uri)
                    logger.log("Браузер открыт.")
                } else {
                    logger.log("Desktop API не поддерживается.")
                }
            } catch (e: Exception) {
                logger.log("Ошибка при открытии браузера.")
                logger.log(e)
            }
        }
    }

    try {
        val credential = authApp.authorize("user")
        logger.log("Авторизация прошла успешно.")
    } catch (e: Exception) {
        logger.log("Ошибка авторизации.")
        logger.log(e)
    }
}

class DesktopFileLogger: FileLogger {
    private val logFile: File

    init {
        val logDir = Paths.get(System.getProperty("user.home"), ".eventtablemerger").toFile()
        if (!logDir.exists()) {
            logDir.mkdirs()
        }
        logFile = File(logDir, "myapp-log.txt")
    }

    override fun log(message: String) {
        logFile.appendText("${LocalDateTime.now()} | $message\n")
    }

    override fun log(e: Exception) {
        logFile.appendText("${LocalDateTime.now()} | ERROR: ${e.message}\n")
        e.printStackTrace(PrintWriter(logFile))
    }
}

val desktopFileLoggerModule = module {
    singleOf(::DesktopFileLogger) {bind<FileLogger>()}
}