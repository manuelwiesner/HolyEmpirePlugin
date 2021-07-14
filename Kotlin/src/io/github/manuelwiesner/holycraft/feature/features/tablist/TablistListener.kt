package io.github.manuelwiesner.holycraft.feature.features.tablist

import io.github.manuelwiesner.holycraft.feature.event.ListenerBase
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerJoinEvent

class TablistListener(feature: TablistFeature) : ListenerBase<TablistFeature>(feature) {
    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        this.feature.updateTablist(event.player)
    }

    @EventHandler
    fun onPlayerDeath(event: PlayerDeathEvent) {
        this.feature.getDeathCount(event.entity).getAndIncrement()
        this.feature.updateTablist(event.entity)
    }
}