package ru.thegamerdog.plutail.plugin

import kotlinx.serialization.json.Json
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.slf4j.Logger
import ru.thegamerdog.plutail.event.Event
import ru.thegamerdog.plutail.event.EventHandler
import java.io.File
import java.io.FileNotFoundException
import java.net.URL
import java.net.URLClassLoader
import java.nio.file.Paths
import java.util.jar.JarFile

internal data class PluginLater(
    val plugin: Plugin,
    val loadAfter: ArrayList<String>
)

interface IPluginManager {
    fun loadPlugin(plugin: Plugin)

    fun invokeEvent(event: Event)

    fun registerListener(listener: EventHandler<out Event>)

    fun disablePlugins()

    fun enablePlugins()
}

class PluginManager : IPluginManager, KoinComponent {
    private val logger by inject<Logger>()

    private val plugins: MutableMap<String, Plugin> = mutableMapOf()
    private val handlers: MutableMap<Class<out Event>, ArrayList<EventHandler<out Event>>> = mutableMapOf()

    init {
        initPlugins()
    }

    private fun initPlugins() {
        val pluginsDir: File = Paths.get("./plugins").toFile()
        if (!pluginsDir.exists() && !pluginsDir.mkdirs()) {
            logger.error("Plugin download error: /plugins folder not found")
            return
        }

        val files = pluginsDir.listFiles() ?: return

        val plugins = files.filter { file: File ->
            file.name.endsWith(".jar")
        }.toList()

        val pluginNames: Array<URL?> = arrayOfNulls(plugins.size)
        plugins.forEach { plugin: File ->
            pluginNames[plugins.indexOf(plugin)] = plugin.toURI().toURL()
        }

        val classLoader = URLClassLoader(pluginNames)
        val dependencies: MutableList<PluginLater> = arrayListOf()

        for (plugin in plugins) {
            try {
                val url: URL = plugin.toURI().toURL()
                try {
                    URLClassLoader(arrayOf(url)).use { loader ->
                        val configFile: URL = loader.findResource("plugin.json")
                        val pluginConfig: PluginConfig = Json.decodeFromString(configFile.readText())

                        val jarFile = JarFile(plugin)
                        val entries = jarFile.entries()

                        while (entries.hasMoreElements()) {
                            val entry = entries.nextElement()
                            if (entry.isDirectory
                                || !entry.name.endsWith(".class")
                                || entry.name.contains("module-info")
                            ) continue
                            val className = entry.name.replace(".class", "").replace("/", ".")
                            classLoader.loadClass(className)
                        }

                        val pluginClass = classLoader.loadClass(pluginConfig.mainClass)
                        val pluginInstance =
                            pluginClass.getDeclaredConstructor(PluginConfig::class.java, URLClassLoader::class.java)
                                .newInstance(pluginConfig, loader) as Plugin

                        if (pluginConfig.loadAfter.size > 0) {
                            dependencies.add(
                                PluginLater(
                                    pluginInstance,
                                    pluginConfig.loadAfter
                                )
                            )
                            return@use
                        }

                        loadPlugin(pluginInstance)
                    }
                } catch (_: ClassNotFoundException) {
                    logger.warn("Error loading the ${plugin.name} class")
                } catch (_: FileNotFoundException) {
                    logger.warn("${plugin.name} configuration was not found")
                }
            } catch (e: Exception) {
                logger.error("${plugin.name} loading error: $e")
            }
        }

        while (dependencies.isNotEmpty()) {
            try {
                val pluginLater: PluginLater = dependencies[0]

                if (!this.plugins.keys.containsAll(pluginLater.loadAfter)) continue
                dependencies.remove(pluginLater)

                loadPlugin(pluginLater.plugin)
            } catch (e: Exception) {
                logger.error("Plugin loading error: $e")
            }
        }
    }

    override fun loadPlugin(plugin: Plugin) {
        plugins[plugin.name] = plugin

        try {
            plugin.onLoad()
        } catch (e: Throwable) {
            logger.error("Error during plugin launch: $e")
        }
    }

    override fun enablePlugins() {
        plugins.forEach { (name: String, plugin: Plugin) ->
            plugin.logger.info("Enabling $name v${plugin.version}")

            try {
                plugin.onEnable()
                return@forEach
            } catch (e: Throwable) {
                plugin.logger.error("Plugin enabling error: $e")
                disablePlugin(plugin)
            }
        }
    }

    override fun disablePlugins() {
        plugins.forEach { (name: String, plugin: Plugin) ->
            plugin.logger.info("Disabling $name v${plugin.version}")
            disablePlugin(plugin)
        }
    }

    private fun disablePlugin(plugin: Plugin) {
        try {
            plugin.onDisable()
        } catch (e: Exception) {
            plugin.logger.error("Plugin disabling error: $e")
        }

        removeListeners(plugin)
    }

    override fun registerListener(listener: EventHandler<out Event>) {
        if (!handlers.containsKey(listener.handles())) handlers[listener.handles()] = arrayListOf()
        handlers[listener.handles()]!!.add(listener)

        this.sortListeners()
    }

    fun removeListeners(plugin: Plugin) {
        val newMap: MutableMap<Class<out Event>, ArrayList<EventHandler<out Event>>> = mutableMapOf()

        handlers.forEach { (event, handlers) ->
            newMap[event] = arrayListOf()
            handlers.forEach { handler -> if (handler.plugin != plugin) newMap[event]!!.add(handler) }
        }

        handlers.clear()
        handlers.putAll(newMap)
    }

    override fun invokeEvent(event: Event) {
        val handlers = handlers[event.javaClass] ?: return
        handlers.forEach { handler -> invokeHandler(event, handler) }
    }

    private fun <T : Event> invokeHandler(event: Event, handler: EventHandler<T>) {
        handler.listener.onEvent(event as T)
    }

    private fun sortListeners() {
        val newMap: MutableMap<Class<out Event>, ArrayList<EventHandler<out Event>>> = mutableMapOf()

        handlers.forEach { (event, handlers) ->
            newMap[event] = arrayListOf()

            val sorted: List<EventHandler<out Event>> =
                handlers.sortedBy { it.getPriority().ordinal }.toList()
            newMap[event]!!.addAll(sorted)
        }

        handlers.clear()
        handlers.putAll(newMap)
    }
}