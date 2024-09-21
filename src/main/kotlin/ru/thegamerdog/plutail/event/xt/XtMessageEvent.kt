package ru.thegamerdog.plutail.event.xt

import ru.thegamerdog.plutail.event.Event
import ru.thegamerdog.plutail.user.IUser

class XtMessageEvent(val zoneName: String, val user: IUser, val message: String) : Event()