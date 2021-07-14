package io.github.manuelwiesner.holycraft.lang

import io.github.manuelwiesner.holycraft.load.Loadable
import org.bukkit.command.CommandSender
import java.io.File

/**
 * Allows localization of messages by turning language-keys to actual messages, also stores which language each user
 * has selected.
 */
interface LangManager : Loadable<Unit> {
    /**
     * Returns all available languages.
     */
    fun getAvailableLanguages(): Collection<String>

    /**
     * Gets the selected language of a user.
     */
    fun getLanguage(user: CommandSender): Lang

    /**
     * Sets the selected language of a user.
     */
    fun setLanguage(user: CommandSender, lang: String): Boolean
}