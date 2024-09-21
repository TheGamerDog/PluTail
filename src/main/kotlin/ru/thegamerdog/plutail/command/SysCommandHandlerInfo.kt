package ru.thegamerdog.plutail.command

import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter

data class SysCommandHandlerInfo(
    val type: KClass<out ISysCommandHandler>,
    val function: KFunction<*>,
    val name: SysCommandName,
    val args: KParameter?
)