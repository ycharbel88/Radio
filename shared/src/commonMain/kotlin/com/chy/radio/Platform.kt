package com.chy.radio

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform