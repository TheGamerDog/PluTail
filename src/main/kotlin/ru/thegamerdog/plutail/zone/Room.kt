package ru.thegamerdog.plutail.zone

import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XML
import nl.adaptivity.xmlutil.serialization.XmlSerialName
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.slf4j.Logger
import ru.thegamerdog.plutail.command.UserDataXml
import ru.thegamerdog.plutail.command.UserIdXml
import ru.thegamerdog.plutail.command.VarsXml
import ru.thegamerdog.plutail.config.RoomData
import ru.thegamerdog.plutail.exception.room.FullRoomException
import ru.thegamerdog.plutail.user.IUser
import ru.thegamerdog.plutail.util.DataObjTransform

@Serializable
@XmlSerialName(value = "body")
data class UserCountBodyXml(
    val action: String,
    val r: Int,
    val u: Int,
    val s: Int? = null
)

@Serializable
@XmlSerialName(value = "msg")
data class UserCountMsgXml(
    val t: String,
    val body: UserCountBodyXml
)

@Serializable
@XmlSerialName(value = "body")
data class UserEnterBodyXml(
    val action: String,
    val r: Int,
    val u: UserDataXml
)

@Serializable
@XmlSerialName(value = "msg")
data class UserEnterMsgXml(
    val t: String,
    val body: UserEnterBodyXml
)

@Serializable
@XmlSerialName(value = "uLs")
data class UserDataListXml(
    val u: ArrayList<UserDataXml>
)

@Serializable
@XmlSerialName(value = "pid")
data class PlayerIdXml(
    val id: Int
)

@Serializable
@XmlSerialName(value = "body")
data class UserGoneBodyXml(
    val action: String,
    val r: Int,
    val user: UserIdXml
)

@Serializable
@XmlSerialName(value = "msg")
data class UserGoneMsgXml(
    val t: String,
    val body: UserGoneBodyXml
)

@Serializable
@XmlSerialName(value = "body")
data class JoinOkBodyXml(
    val action: String,
    val r: Int,
    val uLs: UserDataListXml,

    val vars: VarsXml,
    val pid: PlayerIdXml
)

@Serializable
@XmlSerialName(value = "msg")
data class JoinOkMsgXml(
    val t: String,
    val body: JoinOkBodyXml
)

class Room(private val zone: Zone, val roomData: RoomData, val roomId: Int) : KoinComponent {
    private val logger by inject<Logger>()
    private val users: ArrayList<IUser> = arrayListOf()

    val roomVars: MutableMap<String, Any?> = mutableMapOf()

    init {
        roomData.roomVars.forEach {
            val value: Any? = when (it.value.type) {
                "s" -> it.value.value
                "n" -> it.value.value.toInt()
                "b" -> it.value.value == "1" || it.value.value == "true"
                else -> null
            }

            roomVars[it.key] = value
        }
    }

    fun addUser(user: IUser) {
        if (roomData.maxUsers <= users.size) throw FullRoomException()

        val userCountMsg = XML.encodeToString(
            UserCountMsgXml(
                t = "sys",
                body = UserCountBodyXml(
                    action = "uCount",
                    r = roomId,
                    u = users.size + 1
                )
            )
        )

        val userEnterMsg = XML.encodeToString(
            UserEnterMsgXml(
                t = "sys",
                body = UserEnterBodyXml(
                    action = "uER",
                    r = roomId,
                    u = UserDataXml(
                        i = user.userModel!!.id.value,

                        // TODO: Check default behaviour for
                        //  p = user.userModel!!.id.value,

                        m = if (user.userModel!!.isMod) 1 else 0,
                        s = 0,

                        n = user.userModel!!.username,
                        vars = VarsXml(
                            `var` = DataObjTransform.paramsToDataObj(user.userVars)
                        )
                    )
                )
            )
        )

        zone.getAllUsers().forEach {
            it.sendMessage(userCountMsg)
        }

        users.forEach {
            it.sendMessage(userEnterMsg)
        }

        user.roomName = roomData.name
        users.add(user)

        val usersList: ArrayList<UserDataXml> = arrayListOf()
        users.forEach {
            usersList.add(
                UserDataXml(
                    i = it.userModel!!.id.value,
                    //p = it.userModel!!.id.value,

                    m = if (it.userModel!!.isMod) 1 else 0,
                    s = 0,

                    n = it.userModel!!.username,
                    vars = VarsXml(
                        `var` = DataObjTransform.paramsToDataObj(it.userVars)
                    )
                )
            )
        }

        val joinOkMsg = XML.encodeToString(
            JoinOkMsgXml(
                t = "sys",
                body = JoinOkBodyXml(
                    action = "joinOK",
                    r = roomId,
                    uLs = UserDataListXml(usersList),
                    pid = PlayerIdXml(0),
                    vars = VarsXml(DataObjTransform.paramsToDataObj(roomVars))
                )
            )
        )

        user.sendMessage(joinOkMsg)
    }

    fun removeUser(user: IUser) {
        users.remove(user)

        val userCountMsg = XML.encodeToString(
            UserCountMsgXml(
                t = "sys",
                body = UserCountBodyXml(
                    action = "uCount",
                    r = roomId,
                    u = users.size
                )
            )
        )

        val userEnterMsg = XML.encodeToString(
            UserGoneMsgXml(
                t = "sys",
                body = UserGoneBodyXml(
                    action = "userGone",
                    r = roomId,
                    user = UserIdXml(
                        id = user.userModel!!.id.value
                    )
                )
            )
        )

        zone.getAllUsers().forEach {
            it.sendMessage(userCountMsg)
        }

        users.forEach {
            it.sendMessage(userEnterMsg)
        }
    }

    fun getUserCount(): Int {
        return users.size
    }

    fun getAllUsers(): ArrayList<IUser> {
        return users
    }
}