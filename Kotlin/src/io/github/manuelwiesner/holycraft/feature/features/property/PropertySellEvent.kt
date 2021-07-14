package io.github.manuelwiesner.holycraft.feature.features.property

import io.github.manuelwiesner.holycraft.feature.event.CancellableHolyCraftEvent
import org.bukkit.entity.Player
import org.bukkit.event.HandlerList

class PropertySellEvent(val player: Player, val property: IProperty) : CancellableHolyCraftEvent(false) {
    companion object {
        private val HANDLER_LIST: HandlerList = HandlerList()
        @JvmStatic
        fun getHandlerList(): HandlerList = HANDLER_LIST
    }

    override fun getHandlers(): HandlerList = HANDLER_LIST
}