package io.github.manuelwiesner.holycraft.feature.features.update

import io.github.manuelwiesner.holycraft.feature.FeatureBase
import io.github.manuelwiesner.holycraft.feature._FeatureManager
import io.github.manuelwiesner.holycraft.player.View
import io.github.manuelwiesner.holycraft.store.StoreConverter
import io.github.manuelwiesner.holycraft.yaml.SafeYaml
import org.bukkit.entity.Player
import java.util.*

class UpdateFeature(manager: _FeatureManager) : FeatureBase<Unit>(manager, "UPDATE") {

    private val hasJoinedView: View<Boolean> = getHolyCraft().getPlayerManager()
        .getView("update.hasJoined", StoreConverter.FLAG)

    private val hasResetYaml: SafeYaml<Boolean> = getHolyCraft().getYamlManager()
        .getBooleanWrapper("feature.update.hasReset").makeSafe(false)

    private val updateMessage: SafeYaml<String> = getHolyCraft().getYamlManager()
        .getStringWrapper("feature.update.message").makeSafe("§2A new version of the HolyCraft plugin is now installed!")

    init {
        this.children += UpdateListener(this)
    }

    override fun loadFeature() {
        if (!this.hasResetYaml.get()) {
            this.hasJoinedView.clear()
            this.hasResetYaml.set(true)
        }
    }

    fun hasJoined(id: UUID): Boolean {
        return this.hasJoinedView[id] ?: false
    }

    fun sendUpdateMessage(player: Player) {
        this.hasJoinedView[player.uniqueId] = true
        player.sendMessage(this.updateMessage.get())
    }
}