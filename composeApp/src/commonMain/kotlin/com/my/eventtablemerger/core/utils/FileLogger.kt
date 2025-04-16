package com.my.eventtablemerger.core.utils

interface FileLogger {
    fun log(message: String)
    fun log(e: Exception)
}