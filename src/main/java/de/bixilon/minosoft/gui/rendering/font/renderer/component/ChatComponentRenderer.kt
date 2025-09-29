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

package de.bixilon.minosoft.gui.rendering.font.renderer.component

import glm_.mat4x4.Mat4
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import de.bixilon.minosoft.data.text.BaseComponent
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.data.text.EmptyComponent
import de.bixilon.minosoft.data.text.TextComponent
import de.bixilon.minosoft.data.text.formatting.color.RGBAColor
import de.bixilon.minosoft.gui.rendering.RenderConstants
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.chunk.mesh.ChunkMesh
import de.bixilon.minosoft.gui.rendering.font.WorldGUIConsumer
import de.bixilon.minosoft.gui.rendering.font.manager.FontManager
import de.bixilon.minosoft.gui.rendering.font.renderer.element.TextOffset
import de.bixilon.minosoft.gui.rendering.font.renderer.element.TextRenderInfo
import de.bixilon.minosoft.gui.rendering.font.renderer.element.TextRenderProperties
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions
import de.bixilon.minosoft.gui.rendering.util.mat.mat4.Mat4Util.rotateRadAssign

interface ChatComponentRenderer<T : ChatComponent> {

    /**
     * Returns true if the text exceeded the maximum size
     */
    fun render(offset: TextOffset, fontManager: FontManager, properties: TextRenderProperties, info: TextRenderInfo, consumer: GUIVertexConsumer?, options: GUIVertexOptions?, text: T): Boolean

    fun calculatePrimitiveCount(text: T): Int

    companion object : ChatComponentRenderer<ChatComponent> {
        const val TEXT_BLOCK_RESOLUTION = 128

        override fun render(offset: TextOffset, fontManager: FontManager, properties: TextRenderProperties, info: TextRenderInfo, consumer: GUIVertexConsumer?, options: GUIVertexOptions?, text: ChatComponent): Boolean {
            return when (text) {
                is BaseComponent -> BaseComponentRenderer.render(offset, fontManager, properties, info, consumer, options, text)
                is TextComponent -> TextComponentRenderer.render(offset, fontManager, properties, info, consumer, options, text)
                is EmptyComponent -> return false
                else -> TODO("Don't know how to render ${text::class.java}")
            }
        }

        fun render3d(context: RenderContext, position: Vec3, properties: TextRenderProperties, rotation: Vec3, maxSize: Vec2, mesh: ChunkMesh, text: ChatComponent, light: Int): TextRenderInfo {
            val matrix = Mat4()
                .translateAssign(position)
                .rotateRadAssign(rotation)
                .translateAssign(Vec3(0, 0, -1))

            val primitives = calculatePrimitiveCount(text)
            mesh.ensureSize(primitives * mesh.order.size * ChunkMesh.ChunkMeshStruct.FLOATS_PER_VERTEX)

            val consumer = WorldGUIConsumer(mesh, matrix, light)
            return render3d(context, properties, maxSize, consumer, text, null)
        }

        fun render3d(context: RenderContext, properties: TextRenderProperties, maxSize: Vec2, mesh: GUIVertexConsumer, text: ChatComponent, background: RGBAColor? = RenderConstants.TEXT_BACKGROUND_COLOR): TextRenderInfo {
            val primitives = calculatePrimitiveCount(text)
            mesh.ensureSize(primitives)

            val info = TextRenderInfo(maxSize)
            render(TextOffset(), context.font, properties, info, null, null, text)
            info.rewind()
            if (background != null) {
                mesh.addQuad(Vec2(-1, 0), info.size + Vec2(1, 0), background, null)
            }
            val size = info.size.x
            info.size.x = maxSize.x // this allows font aligning

            render(TextOffset(), context.font, properties, info, mesh, null, text)
            info.size.x = size

            return info
        }

        override fun calculatePrimitiveCount(text: ChatComponent): Int {
            return when (text) {
                is BaseComponent -> BaseComponentRenderer.calculatePrimitiveCount(text)
                is TextComponent -> TextComponentRenderer.calculatePrimitiveCount(text)
                is EmptyComponent -> 0
                else -> TODO("Don't know how to render ${text::class.java}")
            }
        }
    }
}
