package com.my.eventtablemerger.core.drive

import com.my.eventtablemerger.features.screens.login.presentation.TOKENS_DIRECTORY_PATH
import java.io.File

fun clearCredentials() {
    val tokensDir = File(TOKENS_DIRECTORY_PATH)
    if (tokensDir.exists()) {
        tokensDir.deleteRecursively()
    }
}