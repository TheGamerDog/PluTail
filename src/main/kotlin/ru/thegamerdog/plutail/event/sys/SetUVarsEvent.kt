package ru.thegamerdog.plutail.event.sys

import ru.thegamerdog.plutail.command.VarsXml
import ru.thegamerdog.plutail.event.Event
import ru.thegamerdog.plutail.user.IUser

class SetUVarsEvent(val user: IUser, val vars: VarsXml) : Event()