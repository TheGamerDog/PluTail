package ru.thegamerdog.plutail.event.packet

import io.netty.channel.ChannelHandlerContext
import ru.thegamerdog.plutail.command.SysCommandName
import ru.thegamerdog.plutail.event.Event
import ru.thegamerdog.plutail.user.IUser

class RawPacketEvent(val user: IUser, val rawPacket: String) : Event()