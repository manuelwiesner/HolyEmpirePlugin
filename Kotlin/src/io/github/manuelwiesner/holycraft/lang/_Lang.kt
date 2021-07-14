package io.github.manuelwiesner.holycraft.lang

import com.google.gson.stream.JsonReader
import io.github.manuelwiesner.holycraft.load.LoadableBase
import io.github.manuelwiesner.holycraft.store.*
import org.bukkit.command.CommandSender
import java.io.IOException
import java.io.InputStream

/**
 * Implementation of Lang
 * @see Lang
 */
class _Lang(manager: _LangManager, private val langId: String) :
    LoadableBase<InputStream, _LangManager>(manager, langId, { manager.getPlugin().getResource("lang-$langId.json")!! }), Lang {

    private val langTranslations: MutableMap<String, TranslatedMessage> = hashMapOf()
    private lateinit var languageName: String

    override fun onLoad() {
        this.langTranslations.clear()
        this.loadTranslations(getItem())
    }

    override fun onUnload() {
        this.langTranslations.clear()
    }

    override fun saveToDisk() {
        // nothing to be done
    }

    override fun getTranslation(key: String, args: Array<out Any>): List<String>? {
        postLoad()
        return this.langTranslations[key]?.getTranslation(args)
            ?: let { getLogger().warn("Translation for $key is missing in language $langId!"); null }
    }

    override fun sendTranslation(key: String, args: Array<out Any>, receiver: CommandSender) {
        postLoad()

        val langTranslation = this.langTranslations[key]

        // translation for key missing
        if (langTranslation == null) {
            getLogger().warn("Translation for $key is missing in language $langId!")
            val missingTranslation = this.langTranslations["err.translation"]

            // translation for missing translation missing
            if (missingTranslation == null) {
                getLogger().warn("Translation for err.translation is missing in language $langId!")
                val msg = ("Translation for $key missing in language $languageName!\nPlease contact the server owner!")
                receiver.sendMessage(msg)
                return
            }

            // send missing translation
            missingTranslation.sendTranslation(receiver, arrayOf(key))
            return
        }

        // send normal message
        langTranslation.sendTranslation(receiver, args)
    }

    override fun getLanguageName(): String {
        return this.languageName
    }

    /**
     * Actually loads a language from a json file. Replacments will be applied.
     */
    private fun loadTranslations(input: InputStream) {
        runCatching {
            // resource will no longer be unpacked
//            if (!file.exists()) {
//                getPlugin().saveResource(file.name, false)
//                if (!file.exists()) throw NoSuchFileException(file)
//            }

            JsonReader(input.bufferedReader()).use { json ->
                json.nextObject {
                    // language name
                    this@_Lang.languageName = getString("language-name")

                    // prefixes
                    val prefixes = getMap("prefixes", hashMapOf()) {
                        nextList(arrayListOf()) { nextTranslatedMessagePart() }
                    }

                    // load translations
                    getMap("translations", this@_Lang.langTranslations) { nextTranslatedMessage(prefixes) }
                }
            }

        }.onSuccess {

            // Successfully read and interpreted this language file, notify console.
            getLogger().info("Read from resource: lang-$langId.json")

        }.onFailure {

            // Failed to read/interpret this language file, clear 'garbage' and notify console.
            this.langTranslations.clear()
            throw IOException("Failed to load from resource: lang-$langId.json", it)

        }
    }
}