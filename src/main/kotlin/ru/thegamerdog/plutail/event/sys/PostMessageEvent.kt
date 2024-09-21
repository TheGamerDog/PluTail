package ru.thegamerdog.plutail.event.sys

import ru.thegamerdog.plutail.event.Event
import ru.thegamerdog.plutail.user.IUser

class PostMessageEvent(val user: IUser, val text: String, val success: Boolean) : Event()