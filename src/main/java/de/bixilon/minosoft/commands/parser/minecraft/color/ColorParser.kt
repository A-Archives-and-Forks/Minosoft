/*
 * Minosoft
 * Copyright (C) 2020-2025 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.commands.parser.minecraft.color

import de.bixilon.minosoft.commands.parser.ArgumentParser
import de.bixilon.minosoft.commands.parser.factory.ArgumentParserFactory
import de.bixilon.minosoft.commands.suggestion.Suggestion
import de.bixilon.minosoft.commands.suggestion.util.SuggestionUtil
import de.bixilon.minosoft.commands.util.CommandReader
import de.bixilon.minosoft.commands.util.ReadResult
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.text.TextComponent
import de.bixilon.minosoft.data.text.formatting.TextFormattable
import de.bixilon.minosoft.data.text.formatting.color.ChatColors
import de.bixilon.minosoft.data.text.formatting.color.RGBAColor
import de.bixilon.minosoft.data.text.formatting.color.RGBAColor.Companion.rgba
import de.bixilon.minosoft.data.text.formatting.color.RGBColor.Companion.rgb
import de.bixilon.minosoft.protocol.protocol.buffers.play.PlayInByteBuffer
import de.bixilon.minosoft.util.KUtil.toResourceLocation

class ColorParser(
    val allowRGB: Boolean = true,
) : ArgumentParser<RGBAColor> {
    private val suggestions = ChatColors.NAME_MAP.map { ColorSuggestion(it.key, it.value) }
    override val examples: List<Any> = listOf("red", "yellow", "#FFFFFF")

    override fun parse(reader: CommandReader): RGBAColor {
        reader.readResult { reader.readColor() }.let { return it.result ?: throw ColorParseError(reader, it) }
    }

    fun CommandReader.readColor(): RGBAColor? {
        val peek = peek() ?: return null
        if (peek == '#'.code) {
            if (!allowRGB) {
                throw HexNotSupportedError(this, readResult { read()!!.toChar() })
            }
            read()
            val colorString = readWord(false) ?: return null
            return try {
                colorString.rgba()
            } catch (ignored: NumberFormatException) {
                null
            }
        }
        val string = readString(false) ?: return null
        if (string == "reset") {
            return ChatColors.WHITE // ToDo
        }
        return ChatColors.NAME_MAP[string.lowercase()]
    }

    override fun getSuggestions(reader: CommandReader): Collection<Suggestion> {
        if (reader.peek() == '#'.code) {
            reader.read()
            if (!allowRGB) {
                throw HexNotSupportedError(reader, reader.readResult { reader.read()!!.toChar() })
            }
            val pointer = reader.pointer
            val hex = reader.readWord(false) ?: return emptyList()
            try {
                hex.rgb()
            } catch (exception: NumberFormatException) {
                throw ColorParseError(reader, ReadResult(pointer, reader.pointer, hex, null))
            }
            return emptyList()
        }
        val pointer = reader.pointer
        val string = reader.readWord()
        return SuggestionUtil.suggest(suggestions, pointer, string, false) ?: throw ColorParseError(reader, ReadResult(pointer, reader.pointer, string ?: "", null))
    }

    data class ColorSuggestion(val name: String, val color: RGBAColor) : TextFormattable {
        override fun toText(): TextComponent {
            return TextComponent(name).color(color)
        }

        override fun toString(): String {
            return name
        }
    }


    companion object : ArgumentParserFactory<ColorParser> {
        override val identifier: ResourceLocation = "minecraft:color".toResourceLocation()

        override fun read(buffer: PlayInByteBuffer): ColorParser {
            return ColorParser(buffer.session.version.supportsRGBChat)
        }
    }
}
