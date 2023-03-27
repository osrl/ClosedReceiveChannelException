package com.example.closedreceivechannelexception

import kotlinx.serialization.Serializable

/**
 * Created by Osman Saral on 21.03.2023
 */

@Serializable
sealed class Outgoing(val msg: String) {
    @Serializable data class Pong(val id: String?): Outgoing("pong")
}

@Serializable
sealed class Incoming(val msg: String) {
    @Serializable data class Ping(val id: String?): Incoming("ping")
}