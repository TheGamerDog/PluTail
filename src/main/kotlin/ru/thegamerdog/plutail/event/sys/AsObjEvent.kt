package ru.thegamerdog.plutail.event.sys

import ru.thegamerdog.plutail.command.Var
import ru.thegamerdog.plutail.event.Event
import ru.thegamerdog.plutail.user.IUser

class AsObjEvent(user: IUser, roomId: Int, vars: ArrayList<Var>) : Event()