package ru.thegamerdog.plutail.command.handler

import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XML
import nl.adaptivity.xmlutil.serialization.XmlCData
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import ru.thegamerdog.plutail.command.*
import ru.thegamerdog.plutail.user.UserModel
import ru.thegamerdog.plutail.user.Users
import ru.thegamerdog.plutail.event.sys.PostAuthEvent
import ru.thegamerdog.plutail.event.sys.PreAuthEvent
import ru.thegamerdog.plutail.user.IUser
import ru.thegamerdog.plutail.zone.IZoneManager

@Serializable
@XmlSerialName(value = "ver")
data class VerXml(
    val v: String
)

@Serializable
@XmlSerialName(value = "login")
data class LoginXml(
    val z: String,

    @XmlCData
    @XmlElement
    val nick: String,

    @XmlCData
    @XmlElement
    val pword: String,
)

@Serializable
@XmlSerialName(value = "body")
data class LoginBodyXml(
    val action: String,
    val r: Int,
    val login: LoginResXml
)

@Serializable
@XmlSerialName(value = "msg")
data class LoginMsgXml(
    val t: String,
    val body: LoginBodyXml
)

@Serializable
@XmlSerialName(value = "login")
data class LoginResXml(
    val n: String? = null,
    val id: Int? = null,
    val mod: Int? = null,
    val e: String? = null
)

class AuthHandler : ISysCommandHandler, KoinComponent {
    private val zoneManager by inject<IZoneManager>()

    @SysCommandHandler(SysCommandName.VerCheck)
    fun verCheck(user: IUser, roomId: Int, xmlData: VerXml) {
        val res = MsgXml(
            t = "sys",
            body = BodyXml(
                action = "apiOK",
                r = 0
            )
        ) // We need support for all api versions, bruh

        user.sendMessage(XML.encodeToString(res))
    }

    @SysCommandHandler(SysCommandName.Login)
    fun login(user: IUser, roomId: Int, xmlData: LoginXml) {
        user.zoneName = xmlData.z

        val zone = zoneManager.getZoneByName(xmlData.z) ?: return rejectLogin(user)
        val userModel = getUserModel(zone.db, xmlData.nick, xmlData.pword) ?: return rejectLogin(user)

        val authEvent = PreAuthEvent(user, xmlData.nick, xmlData.pword)
        if (!authEvent.call()) return

        user.userModel = userModel

        zone.getAllUsers().find { it.userModel?.username == user.userModel!!.username }?.destroy()
        zone.addUser(user)

        val res = LoginMsgXml(
            t = "sys",
            body = LoginBodyXml(
                action = "logOK",
                r = 0,
                login = LoginResXml(
                    n = xmlData.nick,
                    id = userModel.id.value,
                    mod = if (userModel.isMod) 1 else 0
                )
            )
        )

        user.sendMessage(XML.encodeToString(res))
        PostAuthEvent(user).call()
    }

    private fun rejectLogin(user: IUser) {
        // TODO: Add a detailed description of the errors
        val res = LoginMsgXml(
            t = "sys",
            body = LoginBodyXml(
                action = "logKO",
                r = 0,
                login = LoginResXml(
                    e = "Error when login"
                )
            )
        )

        user.sendMessage(XML.encodeToString(res))
        PostAuthEvent(user).call()
    }

    // TODO: Add jwt-token method
    private fun getUserModel(db: Database, name: String, token: String): UserModel? {
        return transaction(db) {
            val userModel = UserModel.find { (Users.username eq name) and (Users.token eq token) }.firstOrNull()
            return@transaction userModel
        }
    }
}