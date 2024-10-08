package ru.thegamerdog.plutail.command

enum class SysCommandName(
    val key: String
) {
    VerCheck("verChk"),
    Login("login"),
    GetRoomList("getRmList"),
    SetUserVars("setUvars"),
    PublicMessage("pubMsg"),
    AsObj("asObj");

    companion object {
        fun get(key: String) = entries
            .singleOrNull { command -> command.key == key }
    }

    override fun toString(): String = "sys::$name"
}