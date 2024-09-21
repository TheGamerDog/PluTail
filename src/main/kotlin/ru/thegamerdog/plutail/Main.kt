package ru.thegamerdog.plutail

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.versionOption
import kotlinx.coroutines.runBlocking
import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.koin.logger.slf4jLogger
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import ru.thegamerdog.plutail.command.ISysCommandRegistry
import ru.thegamerdog.plutail.command.SysCommandRegistry
import ru.thegamerdog.plutail.config.Configuration
import ru.thegamerdog.plutail.config.IConfiguration
import ru.thegamerdog.plutail.constant.BuildInfo
import ru.thegamerdog.plutail.engine.http.HttpEngine
import ru.thegamerdog.plutail.engine.http.IHttpEngine
import ru.thegamerdog.plutail.engine.tcp.ITcpEngine
import ru.thegamerdog.plutail.engine.tcp.TcpEngine
import ru.thegamerdog.plutail.plugin.IPluginManager
import ru.thegamerdog.plutail.plugin.PluginManager
import ru.thegamerdog.plutail.zone.IZoneManager
import ru.thegamerdog.plutail.zone.ZoneManager
import java.nio.file.Paths
import kotlin.io.path.absolute

fun main(args: Array<String>) = object : CliktCommand() {
    init {
        versionOption(BuildInfo.VERSION)
    }

    override fun run() = runBlocking {
        val logger = LoggerFactory.getLogger("PluTail")

        logger.info("Let's pluck the tail of this fox!")
        logger.info("Version: ${BuildInfo.VERSION}")
        logger.info("Root path: ${Paths.get("").absolute()}")

        val module = module {
            single<IConfiguration> { Configuration() }
            single<ITcpEngine> { TcpEngine() }
            single<IHttpEngine> { HttpEngine() }
            single<ISysCommandRegistry> { SysCommandRegistry() }
            single<IZoneManager> { ZoneManager() }
            single<ISysCommandRegistry> { SysCommandRegistry() }
            single<IPluginManager> { PluginManager() }
            single<Logger> { logger }
        }

        startKoin {
            slf4jLogger()

            modules(module)
        }

        val pluTail = PluTail()
        pluTail.run()
    }
}.main(args)