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

package de.bixilon.minosoft.gui.rendering.font.renderer.element

import de.bixilon.minosoft.data.world.vec.vec2.f.Vec2f
import de.bixilon.minosoft.gui.rendering.font.renderer.CodePointAddResult
import de.bixilon.minosoft.gui.rendering.gui.elements.HorizontalAlignments
import de.bixilon.minosoft.gui.rendering.gui.elements.HorizontalAlignments.Companion.getOffset
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2Util.EMPTY

class TextOffset(
    val initial: Vec2f = Vec2f.EMPTY,
) {
    var offset = Vec2f(initial)


    fun align(alignment: HorizontalAlignments, width: Float, size: Vec2f) {
        this.offset.x = initial.x + alignment.getOffset(size.x, width)
    }

    private fun fits(offset: Float, initial: Float, max: Float, value: Float): Boolean {
        val size = offset - initial
        val remaining = max - size

        return remaining >= value
    }


    fun fitsX(info: TextRenderInfo, width: Float): Boolean {
        return fits(offset.x, initial.x, info.maxSize.x, width)
    }

    fun fitsY(info: TextRenderInfo, offset: Float, height: Float): Boolean {
        return fits(this.offset.y + offset, initial.y, info.maxSize.y, height)
    }

    fun canEverFit(info: TextRenderInfo, width: Float): Boolean {
        return info.maxSize.x >= width
    }

    fun fitsInLine(properties: TextRenderProperties, info: TextRenderInfo, width: Float): Boolean {
        return fitsX(info, width) && fitsY(info, 0.0f, properties.lineHeight)
    }

    fun getNextLineHeight(properties: TextRenderProperties): Float {
        var height = properties.lineHeight
        height += properties.lineSpacing * properties.scale

        return height
    }

    fun addLine(properties: TextRenderProperties, info: TextRenderInfo, offset: Float, height: Float, consuming: Boolean): Boolean {
        if (!fitsY(info, offset, height)) return false
        if (!properties.allowNewLine) return false

        info.lineIndex++

        this.offset.y += height
        if (consuming) {
            align(properties.alignment, info.lines[info.lineIndex].width, info.size)
        } else {
            info.lines += LineRenderInfo()
            this.offset.x = this.initial.x
        }


        return true
    }


    fun canAdd(properties: TextRenderProperties, info: TextRenderInfo, width: Float, height: Float, consuming: Boolean): CodePointAddResult {
        if (!canEverFit(info, width)) {
            info.cutOff = true
            return CodePointAddResult.BREAK
        }
        if (fitsInLine(properties, info, width)) return CodePointAddResult.FINE
        if (addLine(properties, info, 0.0f, height, consuming) && fitsInLine(properties, info, width)) return CodePointAddResult.NEW_LINE

        info.cutOff = true
        return CodePointAddResult.BREAK
    }
}
