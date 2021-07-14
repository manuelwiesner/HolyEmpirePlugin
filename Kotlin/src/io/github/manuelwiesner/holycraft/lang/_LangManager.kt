package io.github.manuelwiesner.holycraft.lang

import io.github.manuelwiesner.holycraft.HolyCraft
import io.github.manuelwiesner.holycraft.load.LoadableManager
import io.github.manuelwiesner.holycraft.player.View
import io.github.manuelwiesner.holycraft.store.StoreConverter
import io.github.manuelwiesner.holycraft.yaml.SafeYaml
import io.github.manuelwiesner.holycraft.yaml.Yaml
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.io.File

/**
 * Implementation of LangManager.
 * @see LangManager
 */
class _LangManager(holyCraft: HolyCraft) : LoadableManager<Unit, Unit>(holyCraft, "LANG"), LangManager {

    /**
     * The default language to use if no language is selected.
     */
    private val defaultLanguage: SafeYaml<String> = getHolyCraft().getYamlManager()
        .getStringWrapper("lang.default").makeSafe("en")

    /**
     * The selected language of the console.
     */
    private val consoleLanguage: SafeYaml<String> = getHolyCraft().getYamlManager()
        .getStringWrapper("lang.console").makeSafe("en")

    /**
     * All available languages.
     */
    private val availableLanguages: Yaml<MutableList<String>> = getHolyCraft().getYamlManager()
        .getStringListWrapper("lang.languages")

    /**
     * View of the language key in player store. Used to store the selected language of a player.
     */
    private val selectedLanguageView: View<String> = getHolyCraft().getPlayerManager()
        .getView("language", StoreConverter.TEXT)

    /**
     * The cached user names of all loaded languages.
     */
    private val languageUserNames: MutableMap<String, String> = hashMapOf()

    /**
     * All available languages loaded/cached.
     */
    private val languageCache: MutableMap<String, Lang> = hashMapOf()

    /**
     * The default language loaded/cached.
     */
    private var defaultLanguageCache: Lang? = null

    /**
     * Loads all available languages and makes sure there is a default language.
     */
    override fun onLoad() {
        this.languageCache.clear()

        val defaultLanguage = this.defaultLanguage.get()
        val availableLanguages = this.availableLanguages.get(arrayListOf()).also { it += defaultLanguage }.distinct().toMutableList()

        this.availableLanguages.set(availableLanguages)

        availableLanguages.forEach { id ->
            runCatching {
                this.languageCache[id] = _Lang(this, id).also { it.load(); this.languageUserNames[it.getLanguageName().toLowerCase()] = id }
            }.onFailure { getLogger().warn("Failed to load language $id!", it) }
        }

        this.defaultLanguageCache = this.languageCache[defaultLanguage]
            ?: throw IllegalArgumentException("Default language lang-$defaultLanguage.json not found!")
    }

    /**
     * Clears caches.
     */
    override fun onUnload() {
        this.languageCache.clear()
        this.defaultLanguageCache = null
    }

    /**
     * Saves to the disk
     */
    override fun saveToDisk() {
        // nothing to be done
    }

    override fun getAvailableLanguages(): Collection<String> {
        postLoad()
        return this.languageUserNames.values
    }

    override fun getLanguage(user: CommandSender): Lang {
        postLoad()
        return if (user is Player) this.selectedLanguageView[user.uniqueId] else {
            this.consoleLanguage.get()
        }?.let { this.languageCache[it] } ?: this.defaultLanguageCache!!
    }

    override fun setLanguage(user: CommandSender, lang: String): Boolean {
        postLoad()
        val languageId = this.languageUserNames[lang.toLowerCase()] ?: return false

        if (user is Player) {
            this.selectedLanguageView[user.uniqueId] = languageId
        } else {
            this.consoleLanguage.set(languageId)
        }

        return true
    }
}