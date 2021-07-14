package io.github.manuelwiesner.holycraft.feature.features.update

import io.github.manuelwiesner.holycraft.feature.event.ListenerBase
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerJoinEvent

class UpdateListener(feature: UpdateFeature) : ListenerBase<UpdateFeature>(feature) {

    @EventHandler
    fun onPlayerJoinEvent(event: PlayerJoinEvent) {
        if (!this.feature.hasJoined(event.player.uniqueId)) {
            this.feature.sendUpdateMessage(event.player)
        }
    }
}