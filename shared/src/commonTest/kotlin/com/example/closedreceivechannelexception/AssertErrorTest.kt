package com.example.closedreceivechannelexception

import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.receiveDeserialized
import io.ktor.client.plugins.websocket.sendSerialized
import io.ktor.client.plugins.websocket.wss
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import io.ktor.server.websocket.DefaultWebSocketServerSession
import io.ktor.server.websocket.receiveDeserialized
import io.ktor.server.websocket.sendSerialized
import io.ktor.server.websocket.webSocket
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

/**
 * Created by Osman Saral on 27.03.2023
 */
class AssertErrorTest {
    @Test
    fun test() = testApplication {
        testServer {
            sendAndReceiveOnce(Incoming.Ping("123")) {
                println("Assertion began")
                assertIs<Outgoing.Pong>(it)
                println("1 Assertion succeeded")
                assertEquals("13", it.id)
                println("2 Assertion succeeded")
            }
        }

        createClient {
            install(WebSockets) {
                contentConverter = DDPMessageConverter(
                    Json {
                        ignoreUnknownKeys = true
                    }
                )
            }
        }.wss("/") {
            val received = receiveDeserialized<Incoming>()
            assertIs<Incoming.Ping>(received)
            sendSerialized(Outgoing.Pong(received.id))
        }
    }
}


suspend inline fun DefaultWebSocketServerSession.sendAndReceiveOnce(
    message: Incoming,
    crossinline receiveBlock: suspend (Outgoing) -> Unit
) {
    sendSerialized(message)
    val outgoing = receiveDeserialized<Outgoing>()
    receiveBlock(outgoing)
}


inline fun ApplicationTestBuilder.testServer(crossinline block: suspend DefaultWebSocketServerSession.() -> Unit) {
    install(io.ktor.server.websocket.WebSockets) {
        contentConverter = DDPServerMessageConverter(
            Json {
                ignoreUnknownKeys = true
            }
        )
    }
    routing {
        webSocket("/") {
            block()
        }
    }
}