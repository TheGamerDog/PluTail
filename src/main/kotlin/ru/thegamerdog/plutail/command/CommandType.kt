package ru.thegamerdog.plutail.command

enum class CommandType(val key: Char) {
    STR('%'),
    XML('<'),
    JSON('{');

    companion object {
        private val map = entries.associateBy(CommandType::key)

        fun get(key: Char) = map[key]
    }
}