package ru.thegamerdog.plutail.event.sys

import ru.thegamerdog.plutail.event.Event
import ru.thegamerdog.plutail.user.IUser

class PostAuthEvent(val user: IUser) : Event()