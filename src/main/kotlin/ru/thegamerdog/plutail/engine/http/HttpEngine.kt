package ru.thegamerdog.plutail.engine.http

import ru.thegamerdog.plutail.user.HttpUser

interface IHttpEngine {
    val users: MutableList<HttpUser>
}

// TODO: Add BlueBox emulation
class HttpEngine : IHttpEngine {
    override val users: MutableList<HttpUser> = arrayListOf()
}