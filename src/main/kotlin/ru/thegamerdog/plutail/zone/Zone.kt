package ru.thegamerdog.plutail.zone

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Slf4jSqlDebugLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.slf4j.Logger
import ru.thegamerdog.plutail.config.RoomData
import ru.thegamerdog.plutail.config.ZoneData
import ru.thegamerdog.plutail.user.Users
import ru.thegamerdog.plutail.event.room.JoinErrorEvent
import ru.thegamerdog.plutail.event.room.PostJoinEvent
import ru.thegamerdog.plutail.event.room.PreJoinEvent
import ru.thegamerdog.plutail.event.xt.XtMessageEvent
import ru.thegamerdog.plutail.exception.room.NotFoundRoomException
import ru.thegamerdog.plutail.plugin.IPluginManager
import ru.thegamerdog.plutail.user.IUser
import java.util.*
import kotlin.collections.ArrayList

class Zone(private val zoneData: ZoneData) : KoinComponent {
    private val pluginManager by inject<IPluginManager>()
    private val logger by inject<Logger>()

    private val users: ArrayList<IUser> = arrayListOf()
    private val rooms: MutableMap<String, Room> = mutableMapOf()

    val db by lazy {
        val dbSettings = zoneData.dbSettings

        when (dbSettings.provider.uppercase(Locale.getDefault())) {
            "MYSQL" -> {
                if (dbSettings.ip == null || dbSettings.username == null || dbSettings.password == null)
                    throw Error("Incorrect database configuration, check the correctness of the config")

                Database.connect(
                    "jdbc:mysql://localhost:3306/${dbSettings.dbName}", driver = "com.mysql.cj.jdbc.Driver",
                    user = dbSettings.username, password = dbSettings.password
                )
            }

            "SQLITE" -> {
                Database.connect("jdbc:sqlite:${dbSettings.dbName}.db", "org.sqlite.JDBC")
            }

            else -> {
                throw Error("Unknown provider, check config. Possible value: SQLITE, MYSQL")
            }
        }
    }

    init {
        logger.info("Zone ${zoneData.name} started")

        zoneData.rooms.forEachIndexed { i: Int, it: RoomData ->
            rooms[it.name] = Room(this, it, i + 1)
        }

        transaction(db) {
            addLogger(Slf4jSqlDebugLogger)

            SchemaUtils.create(Users)
        }
    }

    fun getAllUsers(): ArrayList<IUser> {
        return users
    }

    fun getAllRooms(): MutableCollection<Room> {
        return rooms.values
    }

    fun getRoom(name: String): Room? {
        return rooms[name]
    }

    fun getRoom(roomId: Int): Room? {
        return rooms.values.find { it.roomId == roomId }
    }

    fun addUser(user: IUser) {
        users.add(user)
    }

    fun removeUser(user: IUser) {
        val backRoom = rooms.values.find { it.roomData.name == user.roomName }

        backRoom?.removeUser(user)
        users.remove(user)
    }

    fun handleMessage(user: IUser, message: String) = XtMessageEvent(zoneData.name, user, message).call()

    fun joinToRoom(user: IUser, joinRoomId: Int) {
        try {
            val backRoom = rooms.values.find { it.roomData.name == user.roomName }
            val currentRoom =
                rooms.values.find { it.roomId == joinRoomId } ?: return joinError(user, NotFoundRoomException())

            val preJoinEvent = PreJoinEvent(user, currentRoom)
            if (!preJoinEvent.call()) return

            backRoom?.removeUser(user)
            currentRoom.addUser(user)
            PostJoinEvent(user, currentRoom).call()
        } catch (error: Exception) {
            logger.error(error.message)
            return joinError(user, error)
        }
    }

    fun joinToRoom(user: IUser, name: String) {
        try {
            val backRoom = rooms.values.find { it.roomData.name == user.roomName }
            val currentRoom = rooms[name] ?: return joinError(user, NotFoundRoomException())

            val preJoinEvent = PreJoinEvent(user, currentRoom)
            if (!preJoinEvent.call()) return

            backRoom?.removeUser(user)
            currentRoom.addUser(user)
            PostJoinEvent(user, currentRoom).call()
        } catch (error: Exception) {
            logger.error(error.message)
            return joinError(user, error)
        }
    }

    // TODO: Add default behaviour for errors
    private fun joinError(user: IUser, error: Exception) {
        val message = when (error) {
            is NotFoundRoomException -> {

            }

            else -> {

            }
        }

        JoinErrorEvent(user, error).call()
    }
}