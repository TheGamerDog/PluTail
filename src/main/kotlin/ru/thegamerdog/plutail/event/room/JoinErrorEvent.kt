package ru.thegamerdog.plutail.event.room

import ru.thegamerdog.plutail.event.Event
import ru.thegamerdog.plutail.user.IUser

class JoinErrorEvent(val user: IUser, val error: Exception) : Event()