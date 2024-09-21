package ru.thegamerdog.plutail.plugin

import kotlinx.serialization.Serializable

@Serializable
data class PluginConfig(
    val name: String,
    val description: String,
    val version: String,
    val authors: ArrayList<String>,

    val mainClass: String,
    val loadAfter: ArrayList<String> = arrayListOf()
)
