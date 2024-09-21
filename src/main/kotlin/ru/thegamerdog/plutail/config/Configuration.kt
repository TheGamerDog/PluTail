package ru.thegamerdog.plutail.config

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.slf4j.Logger
import java.io.File
import java.io.FileWriter
import java.io.IOException
import kotlin.system.exitProcess

interface IConfiguration {
    val configData: ConfigData

    suspend fun load()
    suspend fun saveConfig(config: ConfigData)
}

class Configuration : IConfiguration, KoinComponent {
    private val logger by inject<Logger>()
    private val json = Json {
        prettyPrint = true
        encodeDefaults = true
    }

    private val configFile = File("./config.json")
    override lateinit var configData: ConfigData

    override suspend fun load() {
        if (!configFile.exists()) {
            logger.info("config.json could not be found. Generating a default configuration...")

            configData = ConfigData()
            saveConfig(configData)
            return
        }

        try {
            configData = json.decodeFromString(configFile.readText())
        } catch (exception: Exception) {
            logger.error(
                "An error occurred while loading config.json, make sure that all syntax errors are fixed. If this does not help, delete config.json"
            )
            exitProcess(1)
        }
    }

    override suspend fun saveConfig(config: ConfigData) {
        try {
            withContext(Dispatchers.IO) {
                FileWriter(configFile).use { file -> file.write(json.encodeToString(config)) }
            }
        } catch (ignored: IOException) {
            logger.error("Unable to write to config file")
        } catch (e: Exception) {
            logger.error("Unable to save config file", e)
        }
    }
}