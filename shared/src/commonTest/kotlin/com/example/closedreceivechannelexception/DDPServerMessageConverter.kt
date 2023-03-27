package com.example.closedreceivechannelexception

import io.ktor.serialization.WebsocketContentConverter
import io.ktor.util.reflect.TypeInfo
import io.ktor.utils.io.charsets.Charset
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class DDPServerMessageConverter(
    private val json: Json,
): WebsocketContentConverter {
    override suspend fun serializeNullable(
        charset: Charset,
        typeInfo: TypeInfo,
        value: Any?
    ): Frame {
        if (value !is Incoming) throw IllegalArgumentException("Can't send non Incoming message")
        val message = encodeToDDPMessage(value)

        return Frame.Text(message)
    }

    override suspend fun deserialize(charset: Charset, typeInfo: TypeInfo, content: Frame): Any? {
        if (typeInfo.type != Outgoing::class) {
            throw IllegalArgumentException("Can't deserialize types other than Outgoing")
        }
        if (content !is Frame.Text) {
            throw IllegalStateException("Can't deserialize non Text Frames")
        }

        val message = content.readText()

        val jsonString = message
            .replace("\\\"", "\"")
            .removeSurrounding("[\"", "\"]")

        val jsonObject = json.parseToJsonElement(jsonString).jsonObject
        return when(val msg = jsonObject["msg"]?.jsonPrimitive?.content) {
            "pong" -> json.decodeFromString<Outgoing.Pong>(jsonString)
            else -> throw IllegalStateException("Can't deserialize message $msg type yet")
        }
    }

    override fun isApplicable(frame: Frame): Boolean {
        return frame is Frame.Text
    }

    private fun encodeToDDPMessage(message: Incoming): String {
        val array = arrayOf(json.encodeToString(message))
        val jsonString = json.encodeToString(array)
        return "a$jsonString"
    }
}