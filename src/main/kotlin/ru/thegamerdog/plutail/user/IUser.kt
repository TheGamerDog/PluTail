package ru.thegamerdog.plutail.user

interface IUser {
    var zoneName: String?
    var roomName: String?
    val userVars: MutableMap<String, Any?>
    var userModel: UserModel?
    var xtUserModel: Any?
    var isSpec: Boolean

    fun destroy()
    fun sendMessage(message: String)
    fun setUserVars(vararg args: Pair<String, Any?>)
    fun updateUserVars()
}