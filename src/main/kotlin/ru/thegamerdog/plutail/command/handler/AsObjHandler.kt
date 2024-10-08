package ru.thegamerdog.plutail.command.handler

import org.koin.core.component.KoinComponent
import ru.thegamerdog.plutail.command.DataObj
import ru.thegamerdog.plutail.command.ISysCommandHandler
import ru.thegamerdog.plutail.command.SysCommandHandler
import ru.thegamerdog.plutail.command.SysCommandName
import ru.thegamerdog.plutail.event.sys.AsObjEvent
import ru.thegamerdog.plutail.user.IUser

class AsObjHandler : ISysCommandHandler, KoinComponent {
    @SysCommandHandler(SysCommandName.AsObj)
    fun onAsObj(user: IUser, roomId: Int, xmlData: DataObj) = AsObjEvent(user, roomId, xmlData.`var`!!).call()
}