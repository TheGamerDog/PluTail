package ru.thegamerdog.plutail.user

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

object Users : IntIdTable() {
    val username = varchar("username", 255)
    val token = varchar("token", 255)
    val isMod = bool("isMod")
}

class UserModel(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<UserModel>(Users)

    var username by Users.username
    var token by Users.token
    var isMod by Users.isMod
}