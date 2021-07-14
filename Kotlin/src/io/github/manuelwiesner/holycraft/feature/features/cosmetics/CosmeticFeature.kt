package io.github.manuelwiesner.holycraft.feature.features.cosmetics

import io.github.manuelwiesner.holycraft.feature.FeatureBase
import io.github.manuelwiesner.holycraft.feature._FeatureManager
import io.github.manuelwiesner.holycraft.yaml.SafeYaml
import org.bukkit.entity.Player

class CosmeticFeature(manager: _FeatureManager) : FeatureBase<Unit>(manager, "COSMETIC") {

    companion object {
        const val PLAYER_NAME_REPLACE: String = "<PLAYER>"
        const val OTHER_NAME_REPLACE: String = "<OTHER>"
    }

    /**
     * An array of different messages when a player joins
     */
    private val joinMessages: SafeYaml<MutableList<String>> = getHolyCraft().getYamlManager()
        .getStringListWrapper("feature.cosmetic.joinMessages").makeSafe(arrayListOf())

    /**
     * An array of different messages when a player quits
     */
    private val quitMessages: SafeYaml<MutableList<String>> = getHolyCraft().getYamlManager()
        .getStringListWrapper("feature.cosmetic.quitMessages").makeSafe(arrayListOf())

    /**
     * The format of chat messages
     */
    private val chatFormat: SafeYaml<String> = getHolyCraft().getYamlManager()
        .getStringWrapper("feature.cosmetic.chatFormat").makeSafe("<%1\$s> %2\$s")

    init {
        this.children += CosmeticListener(this)
    }

    fun getJoinMessage(player: Player): String? {
        val messages = this.joinMessages.get()
        if (messages.isEmpty()) return null

        return messages[(Math.random() * messages.size).toInt()].replaceNames(player.name)
    }

    fun getQuitMessage(player: Player): String? {
        val messages = this.quitMessages.get()
        if (messages.isEmpty()) return null

        return messages[(Math.random() * messages.size).toInt()].replaceNames(player.name)
    }

    fun getChatFormat(): String {
        return this.chatFormat.get()
    }

    private fun String.replaceNames(player: String): String {
        var players = getPlugin().server.onlinePlayers.map { it.name }
        if (players.isEmpty()) {
            players = getPlugin().server.offlinePlayers.mapNotNull { it.name }
        }
        val otherName = players[(Math.random() * players.size).toInt()]
        return this.replace(PLAYER_NAME_REPLACE, player).replace(OTHER_NAME_REPLACE, otherName)
    }
}