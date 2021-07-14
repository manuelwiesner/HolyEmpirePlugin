package io.github.manuelwiesner.holycraft.lang

import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import io.github.manuelwiesner.holycraft.store.*
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.*
import net.md_5.bungee.api.chat.hover.content.Content
import net.md_5.bungee.api.chat.hover.content.Entity
import net.md_5.bungee.api.chat.hover.content.Item
import net.md_5.bungee.api.chat.hover.content.Text
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.*

interface TranslatedMessage {
    fun getTranslation(args: Array<out Any>): List<String>
    fun sendTranslation(receiver: CommandSender, args: Array<out Any>)
}

interface TranslatedMessagePart {
    fun getTranslation(args: Array<out Any>): String
    fun getComponents(args: Array<out Any>): List<BaseComponent>
}

// ---------------------------------------------------------------------------------------------------------------------

/**
 * TranslatedMessage containing multiple lines which contain multiple parts each.
 */
private class _TranslatedMessage(
    private val position: TMPosition,
    private val lines: Array<out Array<out TranslatedMessagePart>>
) : TranslatedMessage {

    /**
     * Returns the 'raw' translation with minecraft formatting codes applied
     */
    override fun getTranslation(args: Array<out Any>): List<String> {
        return this.lines.map { line -> line.joinToString("") { it.getTranslation(args) } }
    }

    /**
     * Sends the translation to receiver via BaseComponents
     */
    override fun sendTranslation(receiver: CommandSender, args: Array<out Any>) {
        if (receiver is Player) {

            this.lines.forEach { line ->
                val partList = line.flatMap { it.getComponents(args) }
                if (partList.isNotEmpty()) receiver.spigot().sendMessage(this.position.spigotType, *partList.toTypedArray())
            }

            return
        }

        for (line in getTranslation(args)) {
            if (line.isNotBlank()) receiver.sendMessage(line)
        }
    }
}

// ---------------------------------------------------------------------------------------------------------------------

/**
 * A single part of a message containing text.
 */
private class _TranslatedMessageTextPart(
    private val color: TMColor,
    private val format: TMFormat?,
    private val clickEvent: TMClickEvent?,
    private val hoverEvent: TMHoverEvent?,
    private val text: String
) : TranslatedMessagePart {

    override fun getTranslation(args: Array<out Any>): String {
        return StringBuilder().also { sb ->
            this.color.colorChar.let { sb.append("§$it") }
            this.format?.formatChar?.let { sb.append("§$it") }

            sb.append(this.text)
        }.toString()
    }

    override fun getComponents(args: Array<out Any>): List<BaseComponent> {
        val component = TextComponent().also { it.text = this.text }
        this.color.apply(component, args)
        this.format?.apply(component, args)
        this.clickEvent?.apply(component, args)
        this.hoverEvent?.apply(component, args)
        return listOf(component)
    }

}

/**
 * A single part of a message containing an argument.
 */
private class _TranslatedMessageArgumentPart(
    private val color: TMColor,
    private val format: TMFormat?,
    private val clickEvent: TMClickEvent?,
    private val hoverEvent: TMHoverEvent?,
    private val argumentIndex: Int
) : TranslatedMessagePart {

    override fun getTranslation(args: Array<out Any>): String {
        return StringBuilder().also { sb ->
            this.color.colorChar.let { sb.append("§$it") }
            this.format?.formatChar?.let { sb.append("§$it") }

            sb.append(args.getOrNull(this.argumentIndex))
        }.toString()
    }

    override fun getComponents(args: Array<out Any>): List<BaseComponent> {
        val component = TextComponent().also { it.text = args.getOrNull(this.argumentIndex).toString() }
        this.color.apply(component, args)
        this.format?.apply(component, args)
        this.clickEvent?.apply(component, args)
        this.hoverEvent?.apply(component, args)
        return listOf(component)
    }

}

// ---------------------------------------------------------------------------------------------------------------------

/**
 * Interface to apply a format to a component
 */
private interface _TMFormatter {
    fun apply(component: BaseComponent, args: Array<out Any>)
}

/**
 * Position of the sent message
 */
@Suppress("unused")
private enum class TMPosition(val spigotType: ChatMessageType) {
    CHAT(ChatMessageType.CHAT),
    ACTION_BAR(ChatMessageType.ACTION_BAR),
    SYSTEM(ChatMessageType.SYSTEM)
}

/**
 * Color of a message part
 */
@Suppress("unused")
private enum class TMColor(val colorChar: Char, val chatColor: ChatColor) : _TMFormatter {
    BLACK('0', ChatColor.BLACK),
    DARK_BLUE('1', ChatColor.DARK_BLUE),
    DARK_GREEN('2', ChatColor.DARK_GREEN),
    DARK_AQUA('3', ChatColor.DARK_AQUA),
    DARK_RED('4', ChatColor.DARK_RED),
    DARK_PURPLE('5', ChatColor.DARK_PURPLE),
    GOLD('6', ChatColor.GOLD),
    GRAY('7', ChatColor.GRAY),
    DARK_GRAY('8', ChatColor.DARK_GRAY),
    BLUE('9', ChatColor.BLUE),
    GREEN('a', ChatColor.GREEN),
    AQUA('b', ChatColor.AQUA),
    RED('c', ChatColor.RED),
    LIGHT_PURPLE('d', ChatColor.LIGHT_PURPLE),
    YELLOW('e', ChatColor.YELLOW),
    WHITE('f', ChatColor.WHITE);

    override fun apply(component: BaseComponent, args: Array<out Any>) {
        component.color = this.chatColor
    }
}

/**
 * Format of a message part
 */
@Suppress("unused")
private enum class TMFormat(val formatChar: Char) : _TMFormatter {
    OBFUSCATED('k') {
        override fun apply(component: BaseComponent, args: Array<out Any>) {
            component.isObfuscated = true
        }
    },
    BOLD('l') {
        override fun apply(component: BaseComponent, args: Array<out Any>) {
            component.isBold = true
        }
    },
    STRIKETHROUGH('m') {
        override fun apply(component: BaseComponent, args: Array<out Any>) {
            component.isStrikethrough = true
        }
    },
    UNDERLINE('n') {
        override fun apply(component: BaseComponent, args: Array<out Any>) {
            component.isUnderlined = true
        }
    },
    ITALIC('o') {
        override fun apply(component: BaseComponent, args: Array<out Any>) {
            component.isItalic = true
        }
    },
    RESET('r') {
        override fun apply(component: BaseComponent, args: Array<out Any>) {}
    }
}

// ---------------------------------------------------------------------------------------------------------------------

/**
 * Click event of a message part
 */
private class TMClickEvent(private val action: TMClickEventAction, private val value: String) : _TMFormatter {

    private val index: Int? = value.takeIf { value.length == 3 && value[0] == '{' && value[2] == '}' && value[1] >= '0' && value[1] <= '9' }?.get(1)
        ?.minus('0')

    override fun apply(component: BaseComponent, args: Array<out Any>) {
        component.clickEvent =
            ClickEvent(this.action.spigotType, if (this.index != null) args.getOrElse(this.index) { this.value }.toString() else this.value)
    }
}

/**
 * Types of click events
 */
@Suppress("unused")
private enum class TMClickEventAction(val spigotType: ClickEvent.Action) {
    OPEN_URL(ClickEvent.Action.OPEN_URL),
    OPEN_FILE(ClickEvent.Action.OPEN_FILE),
    RUN_COMMAND(ClickEvent.Action.RUN_COMMAND),
    SUGGEST_COMMAND(ClickEvent.Action.SUGGEST_COMMAND),
    CHANGE_PAGE(ClickEvent.Action.CHANGE_PAGE),
    COPY_TO_CLIPBOARD(ClickEvent.Action.COPY_TO_CLIPBOARD)
}

/**
 * Hover event of a message part
 */
private class TMHoverEvent(private val action: TMHoverEventAction, private val value: List<Content>) : _TMFormatter {
    override fun apply(component: BaseComponent, args: Array<out Any>) {
        component.hoverEvent = HoverEvent(this.action.spigotType, this.value)
    }
}

/**
 * Types of hover events
 */
private enum class TMHoverEventAction(val spigotType: HoverEvent.Action) {
    SHOW_TEXT(HoverEvent.Action.SHOW_TEXT),
    SHOW_ITEM(HoverEvent.Action.SHOW_ITEM),
    SHOW_ENTITY(HoverEvent.Action.SHOW_ENTITY);
}

// ---------------------------------------------------------------------------------------------------------------------

/**
 * Converter for TranslatedMessage
 */
fun JsonReader.nextTranslatedMessage(prefixes: Map<String, List<TranslatedMessagePart>>? = null): TranslatedMessage {
    return nextObject {
        val position = getEnum<TMPosition>("position")
        val prefix = getNullOrString("prefix")

        nextName("parts")
        beginArray()

        if (peek() == JsonToken.BEGIN_ARRAY) {

            // multiple lines (begin array)
            val listOfPartLists: MutableList<Array<out TranslatedMessagePart>> = arrayListOf()

            while (hasNext()) {
                val partList = arrayListOf<TranslatedMessagePart>().also { list ->
                    prefix?.let { prefixes?.get(it) }?.let { list.addAll(it) }
                }

                listOfPartLists.add(nextList(partList) { nextTranslatedMessagePart() }.toTypedArray())
            }

            endArray()
            _TranslatedMessage(position, listOfPartLists.toTypedArray())

        } else {

            // single line (instant begin object)
            val partList = arrayListOf<TranslatedMessagePart>().also { list ->
                prefix?.let { prefixes?.get(it) }?.let { list.addAll(it) }
            }

            while (hasNext()) {
                partList.add(nextTranslatedMessagePart())
            }

            endArray()
            _TranslatedMessage(position, arrayOf(partList.toTypedArray()))
        }
    }
}

/**
 * Converter for TranslatedMessagePart
 */
fun JsonReader.nextTranslatedMessagePart(): TranslatedMessagePart {
    return nextObject {
        val pair = when (nextName()) {
            "text" -> -1 to nextString()
            "index" -> nextInt() to ""
            else -> throw IllegalStateException("Illegal message part text/index!")
        }
        val color = getEnum<TMColor>("color")

        if (peek() == JsonToken.END_OBJECT) {
            return@nextObject if (pair.first < 0) {
                _TranslatedMessageTextPart(color, null, null, null, pair.second)
            } else {
                _TranslatedMessageArgumentPart(color, null, null, null, pair.first)
            }
        }

        val format = getNullOrEnum<TMFormat>("format")
        val clickEvent = getNullOrObject("click-event") { TMClickEvent(getEnum("action"), getString("value")) }
        val hoverEntity = getNullOrObject("hover-event") {
            val action = getEnum<TMHoverEventAction>("action")
            val content = getList("content", arrayListOf()) {
                when (action) {
                    TMHoverEventAction.SHOW_TEXT -> Text(nextString())
                    TMHoverEventAction.SHOW_ENTITY -> nextObject {
                        val type = getString("type")
                        val id = getString("id")
                        Entity(type, id, null)
                    }
                    TMHoverEventAction.SHOW_ITEM -> nextObject {
                        val id = getString("id")
                        val count = getInt("count")
                        val tag = ItemTag.ofNbt(getString("tag-nbt"))
                        Item(id, count, tag)
                    }
                }
            }

            TMHoverEvent(action, content)
        }

        if (pair.first < 0) {
            _TranslatedMessageTextPart(color, format, clickEvent, hoverEntity, pair.second)
        } else {
            _TranslatedMessageArgumentPart(color, format, clickEvent, hoverEntity, pair.first)
        }
    }
}