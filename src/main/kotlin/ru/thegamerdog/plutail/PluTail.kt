package ru.thegamerdog.plutail

import kotlinx.coroutines.coroutineScope
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.reflections.Reflections
import org.reflections.scanners.Scanners
import org.slf4j.Logger
import ru.thegamerdog.plutail.command.ISysCommandHandler
import ru.thegamerdog.plutail.command.ISysCommandRegistry
import ru.thegamerdog.plutail.config.IConfiguration
import ru.thegamerdog.plutail.engine.tcp.ITcpEngine
import ru.thegamerdog.plutail.plugin.IPluginManager
import ru.thegamerdog.plutail.zone.IZoneManager
import kotlin.reflect.KClass

class PluTail : KoinComponent {
    private val logger by inject<Logger>()
    private val config by inject<IConfiguration>()

    private val commandRegistry by inject<ISysCommandRegistry>()
    private val zoneManager by inject<IZoneManager>()
    private val pluginManager by inject<IPluginManager>()
    private val tcpEngine by inject<ITcpEngine>()

    suspend fun run() {
        logger.info("Loading PluTail...")

        val reflections = Reflections("ru.thegamerdog.plutail.command.handler")

        reflections.get(Scanners.SubTypes.of(ISysCommandHandler::class.java).asClass<ISysCommandHandler>())
            .forEach { type ->
                val handlerType = type.kotlin as KClass<ISysCommandHandler>

                commandRegistry.registerHandlers(handlerType)
                logger.debug("Registered command handler: ${handlerType.simpleName}")
            }

        coroutineScope {
            config.load()
            pluginManager.enablePlugins()
        }

        config.configData.zones.forEach {
            zoneManager.createZone(it)
        }

        coroutineScope {
            tcpEngine.run(this)
        }
    }
}