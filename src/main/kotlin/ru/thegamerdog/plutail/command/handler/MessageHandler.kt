package ru.thegamerdog.plutail.command.handler

import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlCData
import nl.adaptivity.xmlutil.serialization.XmlSerialName
import nl.adaptivity.xmlutil.serialization.XmlValue
import org.koin.core.component.KoinComponent
import ru.thegamerdog.plutail.command.ISysCommandHandler
import ru.thegamerdog.plutail.command.SysCommandHandler
import ru.thegamerdog.plutail.command.SysCommandName
import ru.thegamerdog.plutail.event.sys.PreMessageEvent
import ru.thegamerdog.plutail.user.IUser

@Serializable
@XmlSerialName(value = "txt")
data class TextXml(
    @XmlCData
    @XmlValue
    val value: String
)

class MessageHandler : ISysCommandHandler, KoinComponent {
    @SysCommandHandler(SysCommandName.PublicMessage)
    fun sendPublicMessage(user: IUser, roomId: Int, xmlData: TextXml) {
        PreMessageEvent(user, xmlData.value).call() // TODO: Logging, commands, filters, default behaviour
    }
}