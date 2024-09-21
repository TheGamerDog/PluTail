package ru.thegamerdog.plutail.plugin

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import ru.thegamerdog.plutail.event.EventHandler
import ru.thegamerdog.plutail.event.IEventConsumer
import ru.thegamerdog.plutail.zone.IZoneManager
import java.net.URLClassLoader
import java.nio.file.Paths

abstract class Plugin(
    private val data: PluginConfig,
    val classLoader: URLClassLoader?
) : KoinComponent {
    val logger: Logger = LoggerFactory.getLogger(data.name)
    val dataFolder = Paths.get("./plugins").resolve(data.name).toFile()
    val zoneManager by inject<IZoneManager>()

    // Plugin Info
    val name: String
        get() = data.name
    val description: String
        get() = data.description
    val version: String
        get() = data.version
    val authors: ArrayList<String>
        get() = data.authors

    open fun onLoad() {}

    open fun onEnable() {}

    open fun onDisable() {}

    fun registerEvents(vararg listeners: IEventConsumer<*>) {
        listeners.forEach { EventHandler(this, it) }
    }
}