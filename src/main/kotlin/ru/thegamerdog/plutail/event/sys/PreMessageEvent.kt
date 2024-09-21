package ru.thegamerdog.plutail.event.sys

import ru.thegamerdog.plutail.event.Event
import ru.thegamerdog.plutail.user.IUser

class PreMessageEvent(val user: IUser, val text: String) : Event()