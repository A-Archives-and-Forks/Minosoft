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

package de.bixilon.minosoft.gui.rendering.font.renderer.code

import de.bixilon.kmath.vec.vec2.f.Vec2f
import de.bixilon.kutil.enums.BitEnumSet
import de.bixilon.minosoft.data.text.formatting.FormattingCodes
import de.bixilon.minosoft.data.text.formatting.color.RGBAColor
import de.bixilon.minosoft.gui.rendering.font.renderer.CodePointAddResult
import de.bixilon.minosoft.gui.rendering.font.renderer.element.TextOffset
import de.bixilon.minosoft.gui.rendering.font.renderer.element.TextRenderInfo
import de.bixilon.minosoft.gui.rendering.font.renderer.element.TextRenderProperties
import de.bixilon.minosoft.gui.rendering.font.renderer.properties.FormattingProperties.SHADOW_OFFSET
import de.bixilon.minosoft.gui.rendering.gui.elements.HorizontalAlignments.Companion.getOffset
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions

interface CodePointRenderer {

    fun calculateWidth(scale: Float, shadow: Boolean): Float

    fun render(position: Vec2f, properties: TextRenderProperties, color: RGBAColor, shadow: Boolean, bold: Boolean, italic: Boolean, scale: Float, consumer: GUIVertexConsumer, options: GUIVertexOptions?)

    private fun getVerticalSpacing(offset: TextOffset, properties: TextRenderProperties, info: TextRenderInfo, align: Boolean): Float {
        var lineStart = offset.initial.x
        if (align && info.lines.isNotEmpty()) {
            lineStart += properties.alignment.getOffset(info.size.x, info.lines[info.lineIndex].width)
        }
        if (offset.offset.x == lineStart) return 0.0f
        // not at line start
        var spacing = properties.charSpacing.horizontal
        if (properties.shadow) {
            spacing = maxOf(spacing - SHADOW_OFFSET, 0.0f)
        }

        return spacing * properties.scale
    }


    fun render(offset: TextOffset, color: RGBAColor, properties: TextRenderProperties, info: TextRenderInfo, formatting: BitEnumSet<FormattingCodes>, codePoint: Int, consumer: GUIVertexConsumer?, options: GUIVertexOptions?): CodePointAddResult {
        val width = calculateWidth(properties.scale, properties.shadow)
        var spacing = getVerticalSpacing(offset, properties, info, consumer != null)
        val height = offset.getNextLineHeight(properties)

        val canAdd = offset.canAdd(properties, info, width + spacing, height, consumer != null)
        when (canAdd) {
            CodePointAddResult.FINE -> {
                offset.offset.x += spacing
            }

            CodePointAddResult.NEW_LINE -> {
                spacing = 0.0f
                info.size.y += height
            }

            CodePointAddResult.BREAK -> return CodePointAddResult.BREAK
        }


        if (consumer != null) {
            render(offset.offset.unsafe, properties, color, properties.shadow, FormattingCodes.BOLD in formatting, FormattingCodes.ITALIC in formatting, properties.scale, consumer, options)
        } else {
            info.update(offset, properties, width, spacing, false) // info should only be updated when we determinate text properties, we know all that already when actually rendering it        }
        }
        offset.offset.x += width

        return canAdd
    }
}
