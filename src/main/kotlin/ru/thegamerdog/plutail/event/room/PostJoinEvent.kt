package ru.thegamerdog.plutail.event.room

import ru.thegamerdog.plutail.event.Event
import ru.thegamerdog.plutail.user.IUser
import ru.thegamerdog.plutail.zone.Room

class PostJoinEvent(val user: IUser, val room: Room) : Event()