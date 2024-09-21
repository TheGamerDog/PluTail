package ru.thegamerdog.plutail.event.packet

import ru.thegamerdog.plutail.command.SysCommandName
import ru.thegamerdog.plutail.event.Event
import ru.thegamerdog.plutail.user.IUser

class PrePacketEvent(val user: IUser, val commandName: SysCommandName, val packet: Any?) : Event()