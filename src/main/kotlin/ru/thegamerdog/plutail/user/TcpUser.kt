package ru.thegamerdog.plutail.user

import io.netty.channel.ChannelHandlerContext
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XML
import nl.adaptivity.xmlutil.serialization.XmlSerialName
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.slf4j.Logger
import ru.thegamerdog.plutail.command.UserIdXml
import ru.thegamerdog.plutail.command.VarsXml
import ru.thegamerdog.plutail.util.DataObjTransform
import ru.thegamerdog.plutail.zone.IZoneManager

@Serializable
@XmlSerialName(value = "body")
data class UpdateVarsBodyXml(
    val action: String,
    val r: Int,
    val vars: VarsXml,
    val user: UserIdXml
)

@Serializable
@XmlSerialName(value = "msg")
data class UpdateVarsMsgXml (
    val t: String,
    val body: UpdateVarsBodyXml
)

class TcpUser(
    private val ctx: ChannelHandlerContext
) : IUser, KoinComponent {
    private val logger by inject<Logger>()
    private val zoneManager by inject<IZoneManager>()

    private val updatedList: ArrayList<String> = arrayListOf()

    override var zoneName: String? = null
    override var roomName: String? = null
    override val userVars: MutableMap<String, Any?> = mutableMapOf()
    override var userModel: UserModel? = null
    override var xtUserModel: Any? = null
    override var isSpec: Boolean = false

    override fun sendMessage(message: String) {
        logger.trace("Send message: $message")

        ctx.writeAndFlush(message)
    }

    override fun setUserVars(vararg args: Pair<String, Any?>) {
        args.forEach {
            updatedList.add(it.first)
            userVars[it.first] = it.second
        }
    }

    override fun updateUserVars() {
        val params: MutableMap<String, Any?> = mutableMapOf()

        updatedList.forEach {
            params[it] = userVars[it]
        }

        val res = XML.encodeToString(UpdateVarsMsgXml(
            t = "sys",
            body = UpdateVarsBodyXml(
                action = "uVarsUpdate",
                r = -1,
                vars = VarsXml(
                    `var` = DataObjTransform.paramsToDataObj(params)
                ),
                user = UserIdXml(userModel!!.id.value)
            )
        ))

        updatedList.clear()
        zoneManager.getZoneByName(zoneName!!)!!.getAllUsers().forEach {
            it.sendMessage(res)
        }
    }

    override fun destroy() {
        if (zoneName != null) zoneManager.getZoneByName(zoneName!!)?.removeUser(this)

        ctx.close()
    }
}