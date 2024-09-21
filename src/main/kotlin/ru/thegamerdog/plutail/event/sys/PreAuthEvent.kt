package ru.thegamerdog.plutail.event.sys

import ru.thegamerdog.plutail.event.Event
import ru.thegamerdog.plutail.user.IUser

class PreAuthEvent(val user: IUser, val username: String, val password: String) : Event()