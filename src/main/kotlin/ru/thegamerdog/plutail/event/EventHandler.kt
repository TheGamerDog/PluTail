package ru.thegamerdog.plutail.event

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import ru.thegamerdog.plutail.plugin.Plugin
import ru.thegamerdog.plutail.plugin.IPluginManager

class EventHandler<T : Event>(val plugin: Plugin, val listener: IEventConsumer<T>) : KoinComponent {
    private val pluginManager by inject<IPluginManager>()
    private var priority: HandlerPriority = listener.priority

    init {
        pluginManager.registerListener(this)
    }

    fun handles(): Class<T> {
        return listener.eventClass
    }

    fun getPriority(): HandlerPriority {
        return priority
    }
}