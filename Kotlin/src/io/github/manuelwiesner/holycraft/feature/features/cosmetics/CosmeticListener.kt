package io.github.manuelwiesner.holycraft.feature.features.cosmetics

import io.github.manuelwiesner.holycraft.feature.event.ListenerBase
import org.bukkit.event.EventHandler
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

class CosmeticListener(feature: CosmeticFeature) : ListenerBase<CosmeticFeature>(feature) {

    @EventHandler(ignoreCancelled = true)
    fun onAsyncPlayerChatEvent(event: AsyncPlayerChatEvent) {
        event.format = this.feature.getChatFormat()
        val formatted = event.message.replace(Regex("&([0-9a-fk-orx])"), "§\$1")
        event.message = formatted
    }

    @EventHandler(ignoreCancelled = true)
    fun onPlayerJoinEvent(event: PlayerJoinEvent) {
        event.joinMessage = this.feature.getJoinMessage(event.player)
    }

    @EventHandler(ignoreCancelled = true)
    fun onPlayerQuitEvent(event: PlayerQuitEvent) {
        event.quitMessage = this.feature.getQuitMessage(event.player)
    }
}