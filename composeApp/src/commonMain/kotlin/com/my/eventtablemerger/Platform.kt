package com.my.eventtablemerger

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform