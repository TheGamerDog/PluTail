package ru.thegamerdog.plutail.command

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class BodyJson(
    val c: String,
    val x: String,
    val r: Int,
    val p: JsonObject
)

@Serializable
data class MsgJson(
    val t: String,
    val b: BodyJson
)