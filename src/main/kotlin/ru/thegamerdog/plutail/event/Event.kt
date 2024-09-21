package ru.thegamerdog.plutail.event

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import ru.thegamerdog.plutail.plugin.IPluginManager

abstract class Event: KoinComponent {
    private val pluginManager by inject<IPluginManager>()

    var isCanceled = false

    fun call(): Boolean {
        pluginManager.invokeEvent(this)
        return !isCanceled
    }
}