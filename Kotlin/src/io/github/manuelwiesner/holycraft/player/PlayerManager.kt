package io.github.manuelwiesner.holycraft.player

import io.github.manuelwiesner.holycraft.load.Loadable
import io.github.manuelwiesner.holycraft.store.StoreConverter

/**
 * Allows persistent storage of player related data, the underlying store uses a UUID-Map<String,String> layout.
 * Also View object allow easy access to specific data.
 */
interface PlayerManager : Loadable<Unit> {
    /**
     * Returns a View object accessing a single key on any player-object.
     * Also transforms the String values via a converter and caches them.
     */
    fun <T : Any> getView(key: String, converter: StoreConverter<T>): View<T>
}