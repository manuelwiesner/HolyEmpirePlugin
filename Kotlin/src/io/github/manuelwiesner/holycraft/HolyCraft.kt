package io.github.manuelwiesner.holycraft

import io.github.manuelwiesner.holycraft.discord.DiscordManager
import io.github.manuelwiesner.holycraft.discord._DiscordManager
import io.github.manuelwiesner.holycraft.feature.FeatureManager
import io.github.manuelwiesner.holycraft.feature._FeatureManager
import io.github.manuelwiesner.holycraft.lang.LangManager
import io.github.manuelwiesner.holycraft.lang._LangManager
import io.github.manuelwiesner.holycraft.load.Load
import io.github.manuelwiesner.holycraft.load.Loadable
import io.github.manuelwiesner.holycraft.logger.Logger
import io.github.manuelwiesner.holycraft.player.PlayerManager
import io.github.manuelwiesner.holycraft.player._PlayerManager
import io.github.manuelwiesner.holycraft.store.StoreManager
import io.github.manuelwiesner.holycraft.store._StoreManager
import io.github.manuelwiesner.holycraft.yaml.YamlManager
import io.github.manuelwiesner.holycraft.yaml._YamlManager
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin

class HolyCraft(private val plugin: HolyCraftPlugin) : Load {

    private val yamlManager: YamlManager = _YamlManager(this)
    private val storeManager: StoreManager = _StoreManager(this)
    private val playerManager: PlayerManager = _PlayerManager(this)
    private val langManager: LangManager = _LangManager(this)
    private val featureManager: FeatureManager = _FeatureManager(this)
    private val discordManager: DiscordManager = _DiscordManager(this)


    override fun load() {
        this.yamlManager.load()
        this.storeManager.load()
        this.playerManager.load()
        this.langManager.load()
        this.featureManager.load()
        this.discordManager.load()
    }

    override fun unload() {
        this.discordManager.unload()
        this.featureManager.unload()
        this.langManager.unload()
        this.playerManager.unload()
        this.storeManager.unload()
        this.yamlManager.unload()
    }

    fun saveToDisk() {
        this.discordManager.saveToDisk()
        this.featureManager.saveToDisk()
        this.langManager.saveToDisk()
        this.playerManager.saveToDisk()
        this.storeManager.saveToDisk()
        this.yamlManager.saveToDisk()
    }

    fun getPlugin(): Plugin {
        return this.plugin
    }

    fun getYamlManager(): YamlManager {
        return this.yamlManager
    }

    fun getStoreManager(): StoreManager {
        return this.storeManager
    }

    fun getPlayerManager(): PlayerManager {
        return this.playerManager
    }

    fun getLangManager(): LangManager {
        return this.langManager
    }

    fun getFeatureManager(): FeatureManager {
        return this.featureManager
    }

    fun getDiscordManager(): DiscordManager {
        return this.discordManager
    }
}