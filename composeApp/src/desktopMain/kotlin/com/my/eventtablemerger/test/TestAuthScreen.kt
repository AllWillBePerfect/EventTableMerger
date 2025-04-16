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
import java.awt.Desktop
import java.io.File
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.URI
import java.nio.file.Paths
import java.time.LocalDateTime


fun authorizeUser() {
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
            FileLogger.log("Попытка открыть браузер: $url")
            println("Если браузер не открылся, перейди по ссылке вручную: $url")

            try {
                val uri = URI(url)
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().browse(uri)
                    FileLogger.log("Браузер открыт.")
                } else {
                    FileLogger.log("Desktop API не поддерживается.")
                }
            } catch (e: Exception) {
                FileLogger.log("Ошибка при открытии браузера.")
                FileLogger.log(e)
            }
        }
    }

    try {
        val credential = authApp.authorize("user")
        FileLogger.log("Авторизация прошла успешно.")
    } catch (e: Exception) {
        FileLogger.log("Ошибка авторизации.")
        FileLogger.log(e)
    }
}

object FileLogger {
    private val logFile = File(
        Paths.get(System.getProperty("user.home"), ".eventtablemerger", "myapp-log.txt").toString()
    )

    fun log(message: String) {
        logFile.appendText("${LocalDateTime.now()} | $message\n")
    }

    fun log(e: Exception) {
        logFile.appendText("${LocalDateTime.now()} | ERROR: ${e.message}\n")
        e.printStackTrace(PrintWriter(logFile))
    }
}