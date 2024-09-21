package ru.thegamerdog.plutail.user

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.slf4j.Logger

// TODO: Add BlueBox emulation
class HttpUser : IUser, KoinComponent {
    private val logger by inject<Logger>()

    override var zoneName: String? = null
    override var roomName: String? = null
    override val userVars: MutableMap<String, Any?> = mutableMapOf()
    override var userModel: UserModel? = null
    override var xtUserModel: Any? = null
    override var isSpec: Boolean = false

    override fun destroy() {

    }

    override fun sendMessage(message: String) {

    }

    override fun setUserVars(vararg args: Pair<String, Any?>) {

    }

    override fun updateUserVars() {

    }
}