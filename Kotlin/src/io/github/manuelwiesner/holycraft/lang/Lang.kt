package io.github.manuelwiesner.holycraft.lang

import org.bukkit.command.CommandSender

/**
 * Turns a language-key into an actual translation and uses the provided arguments to format it.
 */
interface Lang {
    /**
     * Returns the translation of key with formatting args.
     */
    fun getTranslation(key: String, args: Array<out Any>): List<String>?

    /**
     * Sends the message specified with key to receiver.
     */
    fun sendTranslation(key: String, args: Array<out Any>, receiver: CommandSender)

    /**
     * Returns the name of this language.
     */
    fun getLanguageName(): String
}