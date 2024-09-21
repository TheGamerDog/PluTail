package ru.thegamerdog.plutail.command.handler

import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import ru.thegamerdog.plutail.command.ISysCommandHandler
import ru.thegamerdog.plutail.command.SysCommandHandler
import ru.thegamerdog.plutail.command.SysCommandName
import ru.thegamerdog.plutail.command.VarsXml
import ru.thegamerdog.plutail.user.IUser
import ru.thegamerdog.plutail.util.DataObjTransform
import ru.thegamerdog.plutail.zone.IZoneManager

@Serializable
@XmlSerialName(value = "rm")
data class RoomXml(
    @XmlSerialName(value = "id")
    val roomId: Int,

    val priv: Int,
    val temp: Int,
    val game: Int,
    val ucnt: Int,
    val maxu: Int,
    val maxs: Int,

    @XmlCData
    @XmlElement
    val n: String,
    val vars: VarsXml
)

@Serializable
@XmlSerialName(value = "rmList")
data class RoomListXml(
    val rm: ArrayList<RoomXml>
)

@Serializable
@XmlSerialName(value = "body")
data class RoomBodyXml(
    val action: String,
    val r: Int,
    val rmList: RoomListXml
)

@Serializable
@XmlSerialName(value = "msg")
data class RoomMsgXml(
    val t: String,
    val body: RoomBodyXml
)

class RoomHandler : ISysCommandHandler, KoinComponent {
    private val zoneManager by inject<IZoneManager>()

    @SysCommandHandler(SysCommandName.GetRoomList)
    fun getRoomList(user: IUser, roomId: Int) {
        val zone = zoneManager.getZoneByName(user.zoneName!!)!!
        val roomList: ArrayList<RoomXml> = arrayListOf()

        zone.getAllRooms().forEachIndexed { index, value ->
            roomList.add(RoomXml(
                roomId = index + 1,
                priv = if(value.roomData.isPrivate) 1 else 0,
                temp = if(value.roomData.isTemp) 1 else 0,
                game = if(value.roomData.isGame) 1 else 0,
                ucnt = value.getUserCount(),
                maxu = value.roomData.maxUsers,
                maxs = value.roomData.maxSpectators,
                n = value.roomData.name,
                vars = VarsXml(DataObjTransform.paramsToDataObj(value.roomVars))
            ))
        }

        val res = RoomMsgXml(
            t = "sys",
            body = RoomBodyXml(
                action = "rmList",
                r = 0,
                rmList = RoomListXml(roomList)
            )
        )

        user.sendMessage(XML.encodeToString(res))
    }
}