package ru.thegamerdog.plutail.config

import kotlinx.serialization.Serializable

@Serializable
data class RoomVar(
    val type: String,
    val value: String
)

@Serializable
data class RoomData(
    val name: String,
    val maxUsers: Int = 150,
    val maxSpectators: Int = 0,
    val isPrivate: Boolean = false,
    val isTemp: Boolean = false,
    val autoJoin: Boolean = false,
    val isGame: Boolean = false,
    val isLimbo: Boolean = false,
    val password: String? = null,
    val roomVars: MutableMap<String, RoomVar>
)

@Serializable
data class DbSettings(
    val provider: String = "SQLITE",
    val dbName: String,

    val ip: String? = null,
    val username: String? = null,
    val password: String? = null
)

@Serializable
data class ZoneData(
    val name: String,
    val rooms: ArrayList<RoomData>,

    val dbSettings: DbSettings
)

@Serializable
data class ConfigData(
    // TODO: Update config, if !=
    //  Add debug level
    val configVersion: Int = 0,
    val tcpPort: Int = 9339,
    val httpPort: Int = 443,

    val zones: ArrayList<ZoneData> = arrayListOf()
)
