package io.github.manuelwiesner.holycraft.feature.features.tablist

import io.github.manuelwiesner.holycraft.feature.FeatureBase
import io.github.manuelwiesner.holycraft.feature._FeatureManager
import io.github.manuelwiesner.holycraft.player.View
import io.github.manuelwiesner.holycraft.store.StoreConverter
import io.github.manuelwiesner.holycraft.yaml.SafeYaml
import io.github.manuelwiesner.holycraft.yaml.Yaml
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import java.util.concurrent.atomic.AtomicInteger

class TablistFeature(manager: _FeatureManager) : FeatureBase<Unit>(manager, "TABLIST") {

    // -----------------------------------------------------------------------------------------------------------------------------------------------
    // prefix (clan-tag)
    private val prefixMaxLength: SafeYaml<Int> = getHolyCraft().getYamlManager()
        .getIntWrapper("feature.tablist.prefix.length").makeSafe(3)

    private val prefixFormatting: SafeYaml<String> = getHolyCraft().getYamlManager()
        .getStringWrapper("feature.tablist.prefix.format").makeSafe("[PREFIX]")

    private val prefixView: View<String> = getHolyCraft().getPlayerManager()
        .getView("tablist.prefix", StoreConverter.TEXT)

    // -----------------------------------------------------------------------------------------------------------------------------------------------
    // postfix (death-count)
    private val postfixFormatting: SafeYaml<String> = getHolyCraft().getYamlManager()
        .getStringWrapper("feature.tablist.postfix.format").makeSafe("[POSTFIX]")

    private val postfixView: View<AtomicInteger> = getHolyCraft().getPlayerManager()
        .getView("tablist.postfix", StoreConverter.ATOMIC_NUMBER)

    // -----------------------------------------------------------------------------------------------------------------------------------------------
    // header / footer
    private val tablistHeader: Yaml<String> = getHolyCraft().getYamlManager()
        .getStringWrapper("feature.tablist.header")

    private val tablistFooter: Yaml<String> = getHolyCraft().getYamlManager()
        .getStringWrapper("feature.tablist.footer")

    init {
        this.children += ClanTagCmd(this)
        this.children += DeathCounterCmd(this)
        this.children += TablistListener(this)
    }

    fun getMaxPrefixLength(): Int {
        return this.prefixMaxLength.get()
    }

    fun setClanTag(player: OfflinePlayer, clanTag: String) {
        this.prefixView[player.uniqueId] = clanTag
        if (player is Player) updateTablist(player)
    }

    fun removeClanTag(player: Player): String? {
        val oldTag = this.prefixView.remove(player.uniqueId)
        updateTablist(player)
        return oldTag
    }

    fun resetAllDeathCounts() {
        this.postfixView.clear()
        Bukkit.getOnlinePlayers().forEach { updateTablist(it) }
    }

    fun getDeathCount(player: OfflinePlayer): AtomicInteger {
        return this.postfixView.computeIfAbsent(player.uniqueId) { AtomicInteger(0) }
    }

    fun updateTablist(player: Player) {
        val prefix = this.prefixView[player.uniqueId]
        val postfix = this.postfixView[player.uniqueId]
        var playerName = ""

        if (prefix != null) {
            val prefixColor = ChatColor.values()[prefix.chars().reduce { v1, v2 -> v1 + v2 }.asInt % 16]
            playerName += this.prefixFormatting.get().replace("PREFIX", prefixColor.toString() + prefix)
            playerName += " §r"
        }

        playerName += player.name

        if (postfix != null) {
            playerName += " "
            playerName += this.postfixFormatting.get().replace("POSTFIX", postfix.toString())
        }

        player.setPlayerListHeaderFooter(this.tablistHeader.get(), this.tablistFooter.get())
        player.setPlayerListName(playerName)
    }
}