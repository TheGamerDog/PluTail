package ru.thegamerdog.plutail.event

interface IEventConsumer<T : Event> {
    val priority: HandlerPriority
        get() = HandlerPriority.NORMAL
    val eventClass: Class<T>

    fun onEvent(event: T)
}