package ru.thegamerdog.plutail.command

enum class CommandCategory(val key: String) {
    SYSTEM("sys"),
    EXTENSION("xt");

    companion object {
        private val map = entries.associateBy(CommandCategory::key)

        fun get(key: String) = map[key]
    }
}