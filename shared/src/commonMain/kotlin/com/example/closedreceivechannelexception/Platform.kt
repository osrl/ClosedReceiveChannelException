package com.example.closedreceivechannelexception

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform