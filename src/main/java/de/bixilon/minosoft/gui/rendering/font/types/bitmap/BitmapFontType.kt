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

package de.bixilon.minosoft.gui.rendering.font.types.bitmap

import de.bixilon.minosoft.data.world.vec.vec2.f.Vec2f
import de.bixilon.kutil.json.JsonObject
import de.bixilon.kutil.latch.AbstractLatch
import de.bixilon.kutil.primitive.IntUtil.toInt
import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.RenderUtil.fixUVEnd
import de.bixilon.minosoft.gui.rendering.RenderUtil.fixUVStart
import de.bixilon.minosoft.gui.rendering.font.manager.FontManager
import de.bixilon.minosoft.gui.rendering.font.renderer.code.AscentedCodePointRenderer.Companion.DEFAULT_ASCENT
import de.bixilon.minosoft.gui.rendering.font.renderer.code.CodePointRenderer
import de.bixilon.minosoft.gui.rendering.font.renderer.properties.FontProperties.CHAR_BASE_HEIGHT
import de.bixilon.minosoft.gui.rendering.font.types.PostInitFontType
import de.bixilon.minosoft.gui.rendering.font.types.empty.EmptyCodeRenderer
import de.bixilon.minosoft.gui.rendering.font.types.factory.FontTypeFactory
import de.bixilon.minosoft.gui.rendering.system.base.texture.data.buffer.TextureBuffer
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.Texture
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.file.PNGTexture
import de.bixilon.minosoft.gui.rendering.textures.TextureUtil.isBlack
import de.bixilon.minosoft.gui.rendering.textures.TextureUtil.texture
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import de.bixilon.minosoft.util.nbt.tag.NBTUtil.listCast
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import java.util.stream.IntStream

class BitmapFontType(
    val chars: Int2ObjectOpenHashMap<CodePointRenderer>,
) : PostInitFontType {
    private var postInit = false

    init {
        chars.trim()
    }

    override fun get(codePoint: Int): CodePointRenderer? {
        return chars[codePoint]
    }

    override fun postInit(latch: AbstractLatch) {
        if (postInit) return
        for (char in chars.values) {
            if (char !is BitmapCodeRenderer) continue
            char.updateArray()
        }
        postInit = true
    }


    companion object : FontTypeFactory<BitmapFontType> {
        private const val ROW = 16
        override val identifier = minecraft("bitmap")

        override fun build(context: RenderContext, manager: FontManager, data: JsonObject): BitmapFontType? {
            val file = data["file"]?.toString()?.let { it.toResourceLocation().texture() } ?: throw IllegalArgumentException("Missing file!")
            val height = data["height"]?.toInt() ?: 8
            val ascent = data["ascent"]?.toInt() ?: DEFAULT_ASCENT.toInt()
            val chars = data["chars"]?.listCast<String>() ?: throw IllegalArgumentException("Missing chars!")
            return load(file, height, ascent, chars, context)
        }

        private fun List<String>.codePoints(): Array<IntStream> {
            return this.map { it.codePoints() }.toTypedArray()
        }

        private fun load(file: ResourceLocation, height: Int, ascent: Int, chars: List<String>, context: RenderContext): BitmapFontType? {
            if (chars.isEmpty() || height <= 0) return null
            val texture = PNGTexture(file, 0)
            texture.load(context) // force load it, we need to calculate the width of every char
            context.textures.font += texture // TODO: convert to font array size and remove empty lines

            return load(texture, texture.size.y / chars.size, ascent, chars.codePoints())
        }

        private fun TextureBuffer.scanLine(y: Int, width: Int, start: IntArray, end: IntArray) {
            for (index in 0 until (width * ROW)) {
                val rgba = this[index, y]
                if (rgba.isBlack()) continue

                val char = index / width
                val pixel = index % width

                start[char] = minOf(start[char], pixel)
                end[char] = maxOf(end[char], pixel)
            }
        }

        private fun createRenderer(texture: Texture, offset: Vec2f, pixel: Vec2f, start: Int, end: Int, height: Int, ascent: Int): CodePointRenderer {
            if (end < start) return EmptyCodeRenderer()

            val width = end - start + 1

            val uvStart = Vec2f(offset)
            uvStart.x += start * pixel.x
            uvStart.fixUVStart()

            val uvEnd = Vec2f(offset)
            uvEnd.x += width * pixel.x
            uvEnd.y += height * pixel.y
            uvEnd.fixUVEnd()

            val scale = if (height < CHAR_BASE_HEIGHT) 1 else height / CHAR_BASE_HEIGHT
            val scaledWidth = width / scale

            return BitmapCodeRenderer(texture, uvStart, uvEnd, scaledWidth.toFloat(), (height / scale).toFloat(), ascent.toFloat())
        }

        private fun load(texture: Texture, height: Int, ascent: Int, chars: Array<IntStream>): BitmapFontType? {
            val rows = chars.size
            val width = texture.size.x / ROW

            val start = IntArray(ROW) { width }
            val end = IntArray(ROW)

            val renderer = Int2ObjectOpenHashMap<CodePointRenderer>()

            val pixel = Vec2f(1.0f / texture.size.x, 1.0f / texture.size.y)
            val offset = Vec2f()
            for (row in 0 until rows) {
                val iterator = chars[row].iterator()

                for (y in 0 until height) {
                    texture.data.buffer.scanLine((row * height) + y, width, start, end)
                }

                var column = 0
                while (iterator.hasNext()) {
                    val codePoint = iterator.nextInt()
                    renderer[codePoint] = createRenderer(texture, offset, pixel, start[column], end[column], height, ascent)
                    column++
                    offset.x += pixel.x * width
                }

                start.fill(width); end.fill(0) // fill with maximum values again
                offset.x = 0.0f; offset.y += height * pixel.y
            }

            if (renderer.isEmpty()) return null
            renderer.trim()
            return BitmapFontType(renderer)
        }
    }
}
