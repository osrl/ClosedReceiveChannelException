package com.example.closedreceivechannelexception

import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.receiveDeserialized
import io.ktor.client.plugins.websocket.sendSerialized
import io.ktor.client.plugins.websocket.ws
import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import io.ktor.server.testing.testApplication
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@Serializable
data class User(val name: String, val id: Int)

class CommonGreetingTest {

    @Test
    fun testJsonNullWithWebsocketsClient(): Unit = testApplication {
        install(io.ktor.server.websocket.WebSockets)
        routing {
            webSocket("/") {
                for (frame in incoming) {
                    assertEquals("error", (frame as Frame.Text).readText())
                    outgoing.send(frame)
                }
            }
        }

        createClient {
            install(WebSockets) {
                contentConverter = KotlinxWebsocketSerializationConverter(
                    Json {
                        ignoreUnknownKeys = true
                    }
                )
            }
        }.ws("/") {
            val user: User? = null
            sendSerialized(user)
            val received = receiveDeserialized<User?>()
            assertNull(received)
        }
    }
}