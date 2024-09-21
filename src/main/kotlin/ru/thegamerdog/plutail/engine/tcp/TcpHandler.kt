package ru.thegamerdog.plutail.engine.tcp

import io.ktor.util.logging.*
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import kotlinx.serialization.DeserializationStrategy
import nl.adaptivity.xmlutil.serialization.XML
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import ru.thegamerdog.plutail.command.*
import ru.thegamerdog.plutail.event.packet.PostPacketEvent
import ru.thegamerdog.plutail.event.packet.PrePacketEvent
import ru.thegamerdog.plutail.event.packet.RawPacketEvent
import ru.thegamerdog.plutail.user.TcpUser
import ru.thegamerdog.plutail.zone.IZoneManager
import java.lang.reflect.InvocationTargetException
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.companionObject
import kotlin.reflect.full.functions
import kotlin.reflect.full.primaryConstructor

class TcpHandler : ChannelInboundHandlerAdapter(), KoinComponent {
    private val logger by inject<Logger>()
    private val zoneManager by inject<IZoneManager>()
    private val commandRegistry by inject<ISysCommandRegistry>()

    private lateinit var user: TcpUser

    override fun channelActive(ctx: ChannelHandlerContext) {
        user = TcpUser(ctx)

        logger.info("Socket accepted: ${ctx.channel().remoteAddress()}")
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        if (user.zoneName != null) {
            val zone = zoneManager.getZoneByName(user.zoneName!!)
            zone?.removeUser(user)
        }

        logger.info("Socket disconnected: ${ctx.channel().remoteAddress()}")
    }

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        val message = msg as String

        val event = RawPacketEvent(user, message)
        if (!event.call()) return

        // Policy for Flash
        if (message == "<policy-file-request/>") {
            ctx.writeAndFlush("<cross-domain-policy><allow-access-from domain='*' to-ports='*' /></cross-domain-policy>")
            return
        }

        val commandType = CommandType.get(message[0])
        if (commandType != CommandType.XML) return sendMessageToXt(user, message)

        // Bypass for passing a string for sys and subsequent processing using special types
        val xmlArr = message.split("<")
        val outMessage = "<" + xmlArr[1] + "<" + xmlArr[2] + "</body></msg>"
        val inMessage = message
            .replace("<" + xmlArr[1] + "<" + xmlArr[2], "")
            .replace("</body></msg>", "")

        val xmlMessage = XML.decodeFromString(MsgXml.serializer(), outMessage)
        val commandCategory = CommandCategory.get(xmlMessage.t)
        if (commandCategory == CommandCategory.EXTENSION) return sendMessageToXt(user, message)

        logger.trace("Received sys raw message: $message")

        val commandName = SysCommandName.get(xmlMessage.body.action) ?: return

        val handler = commandRegistry.getHandler(commandName) ?: return
        val args = mutableMapOf<KParameter, Any?>()

        val instance = handler.type.primaryConstructor!!.call()
        args += mapOf(
            Pair(
                handler.function.parameters.single { parameter -> parameter.kind == KParameter.Kind.INSTANCE },
                instance
            ),
            Pair(
                handler.function.parameters.filter { parameter -> parameter.kind == KParameter.Kind.VALUE }[0],
                user
            ),
            Pair(
                handler.function.parameters.filter { parameter -> parameter.kind == KParameter.Kind.VALUE }[1],
                xmlMessage.body.r
            )
        )

        if (handler.args != null) {
            val clazz = handler.args.type.classifier as KClass<*>
            val staticObj = clazz.companionObject!!
            val serializer = staticObj.functions.find { it.name == "serializer" }!!
                .call(staticObj.objectInstance) as DeserializationStrategy<*>

            args[handler.function.parameters.filter { parameter -> parameter.kind == KParameter.Kind.VALUE }[2]] =
                XML.decodeFromString(serializer, inMessage)

            val prePacketEvent =
                PrePacketEvent(user, commandName, XML.decodeFromString(serializer, inMessage)!!)
            if (!prePacketEvent.call()) return
        } else {
            val prePacketEvent = PrePacketEvent(user, commandName, null)
            if (!prePacketEvent.call()) return
        }

        try {
            handler.function.callBy(args)
        } catch (exception: Throwable) {
            val targetException = if (exception is InvocationTargetException) exception.cause else exception
            logger.error("Failed to call $commandName handler:\n$targetException")
        }

        if (handler.args != null) PostPacketEvent(
            user,
            commandName,
            args[handler.function.parameters.filter { parameter -> parameter.kind == KParameter.Kind.VALUE }[2]]
        ).call()
        else PostPacketEvent(user, commandName, null).call()
    }

    private fun sendMessageToXt(user: TcpUser, message: String) {
        if (!this::user.isInitialized || user.zoneName == null)
            return logger.error("The zone has not been initialized yet")

        val zone = zoneManager.getZoneByName(user.zoneName!!)
        zone?.handleMessage(user, message)

        logger.trace("Received extension raw message: $message")
    }
}