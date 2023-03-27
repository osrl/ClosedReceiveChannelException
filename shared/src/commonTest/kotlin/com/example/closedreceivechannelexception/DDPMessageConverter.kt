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

/**
 * Created by Osman Saral on 27.03.2023
 */
class DDPMessageConverter(
    private val json: Json,
): WebsocketContentConverter {
    override suspend fun serializeNullable(
        charset: Charset,
        typeInfo: TypeInfo,
        value: Any?
    ): Frame {
        if (value !is Outgoing) return Frame.Text("")
        val message = encodeToDDPMessage(value)

        return Frame.Text(message)
    }

    override suspend fun deserialize(charset: Charset, typeInfo: TypeInfo, content: Frame): Any? {
        if (typeInfo.type != Incoming::class) {
            throw IllegalArgumentException("Can't deserialize types other than Incoming")
        }
        if (content !is Frame.Text) {
            throw IllegalStateException("Can't deserialize non Text Frames")
        }

        val message = content.readText()

        if (!message.startsWith("a[") && !message.startsWith("b[") && !message.startsWith("c[")) {
            throw IllegalStateException("Can't deserialize message $message")
        }

        val jsonString = message
            .drop(1)
            .replace("\\\"", "\"")
            .removeSurrounding("[\"", "\"]")

        val jsonObject = json.parseToJsonElement(jsonString).jsonObject
        return when(val msg = jsonObject["msg"]?.jsonPrimitive?.content) {
            "ping" -> json.decodeFromString<Incoming.Ping>(jsonString)
            else -> throw IllegalStateException("Can't deserialize message $msg type yet")
        }
    }

    override fun isApplicable(frame: Frame): Boolean {
        return frame is Frame.Text
    }


    private fun encodeToDDPMessage(message: Outgoing): String {
        val array = arrayOf(json.encodeToString(message))
        return json.encodeToString(array)
    }
}
