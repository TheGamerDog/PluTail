package ru.thegamerdog.plutail.zone

import ru.thegamerdog.plutail.config.ZoneData

interface IZoneManager {
    fun createZone(zoneData: ZoneData)
    fun getZoneByName(zoneName: String): Zone?
}

class ZoneManager : IZoneManager {
    private val zones: MutableMap<String, Zone> = mutableMapOf()

    override fun createZone(zoneData: ZoneData) {
        zones[zoneData.name] = Zone(zoneData)
    }

    override fun getZoneByName(zoneName: String): Zone? {
        return zones[zoneName]
    }
}