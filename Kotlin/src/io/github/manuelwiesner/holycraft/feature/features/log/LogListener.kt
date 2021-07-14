package io.github.manuelwiesner.holycraft.feature.features.log

import io.github.manuelwiesner.holycraft.feature.event.ListenerBase
import org.bukkit.entity.Animals
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.EntityDeathEvent

class LogListener(feature: LogFeature) : ListenerBase<LogFeature>(feature) {
    @EventHandler
    fun onEntityDeathEvent(event: EntityDeathEvent) {
//        event.entity.killer
    }
}