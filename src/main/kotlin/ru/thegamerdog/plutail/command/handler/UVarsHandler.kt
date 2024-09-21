package ru.thegamerdog.plutail.command.handler

import org.koin.core.component.KoinComponent
import ru.thegamerdog.plutail.command.ISysCommandHandler
import ru.thegamerdog.plutail.command.SysCommandHandler
import ru.thegamerdog.plutail.command.SysCommandName
import ru.thegamerdog.plutail.command.VarsXml
import ru.thegamerdog.plutail.event.sys.SetUVarsEvent
import ru.thegamerdog.plutail.user.IUser

class UVarsHandler : ISysCommandHandler, KoinComponent {
    @SysCommandHandler(SysCommandName.SetUserVars)
    fun setUserVars(user: IUser, roomId: Int, xmlData: VarsXml) {
        val event = SetUVarsEvent(user, xmlData)
        if (!event.call()) return

        val list: ArrayList<Pair<String, Any?>> = arrayListOf()
        xmlData.`var`!!.forEach {
            val value = when(it.t) {
                "s" -> it.value
                "n" -> it.value.toInt()
                "b" -> it.value == "1" || it.value == "true"
                else -> null
            }

            list.add(Pair(it.n, value))
        }

        user.setUserVars(*list.toTypedArray())
        user.updateUserVars()
    }
}