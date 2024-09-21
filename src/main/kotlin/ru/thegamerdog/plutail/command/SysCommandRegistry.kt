package ru.thegamerdog.plutail.command

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.slf4j.Logger
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.declaredMemberFunctions
import kotlin.reflect.full.findAnnotation

interface ISysCommandRegistry {
    fun <T : ISysCommandHandler> registerHandlers(type: KClass<T>)
    fun getHandler(name: SysCommandName): SysCommandHandlerInfo?
}

class SysCommandRegistry : ISysCommandRegistry, KoinComponent {
    private val logger by inject<Logger>()
    private val commands: MutableList<SysCommandHandlerInfo> = mutableListOf()

    override fun getHandler(name: SysCommandName): SysCommandHandlerInfo? {
        return commands.singleOrNull { command -> command.name == name }
    }

    override fun <T : ISysCommandHandler> registerHandlers(type: KClass<T>) {
        type.declaredMemberFunctions.forEach { function ->
            val commandHandler = function.findAnnotation<SysCommandHandler>() ?: return@forEach

            val args = function.parameters.filter { parameter -> parameter.kind == KParameter.Kind.VALUE }
            val parameter = if (args.size == 3) args[2] else null
            val description = SysCommandHandlerInfo(type, function, commandHandler.name, parameter)

            commands.add(description)

            logger.debug(
                "Discovered command handler: {} -> {}::{}",
                commandHandler.name,
                type.qualifiedName,
                function.name
            )
        }
    }
}