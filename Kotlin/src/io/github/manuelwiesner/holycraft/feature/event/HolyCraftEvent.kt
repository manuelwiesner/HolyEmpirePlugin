package io.github.manuelwiesner.holycraft.feature.event

import org.bukkit.Bukkit
import org.bukkit.event.Cancellable
import org.bukkit.event.Event

abstract class HolyCraftEvent(isAsync: Boolean) : Event(isAsync) {
    fun callThisEvent() {
        Bukkit.getServer().pluginManager.callEvent(this)
    }
}

abstract class CancellableHolyCraftEvent(isAsync: Boolean) : HolyCraftEvent(isAsync), Cancellable {
    private var cancelled: Boolean = false

    override fun isCancelled(): Boolean {
        return this.cancelled
    }

    override fun setCancelled(cancel: Boolean) {
        this.cancelled = cancel
    }
}

fun <T : HolyCraftEvent, R> T.callThisEvent(action: (T) -> R): R {
    callThisEvent()
    return action(this)
}

fun <T : CancellableHolyCraftEvent, R> T.ifProceeded(action: (T) -> R): R? {
    callThisEvent()
    return if (!isCancelled) action(this) else null
}

fun <T : CancellableHolyCraftEvent, R> T.ifCancelled(action: (T) -> R): R? {
    callThisEvent()
    return if (isCancelled) action(this) else null
}

fun <T : CancellableHolyCraftEvent, R> T.callThisEvent(proceeded: (T) -> R, cancelled: (T) -> R): R {
    callThisEvent()
    return if (isCancelled) cancelled(this) else proceeded(this)
}