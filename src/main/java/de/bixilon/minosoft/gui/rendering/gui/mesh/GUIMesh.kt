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

package de.bixilon.minosoft.gui.rendering.gui.mesh

import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.kutil.collections.primitive.floats.AbstractFloatList
import de.bixilon.minosoft.data.text.formatting.color.RGBColor
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.system.base.MeshUtil.buffer
import de.bixilon.minosoft.gui.rendering.system.base.texture.shader.ShaderIdentifiable
import de.bixilon.minosoft.gui.rendering.system.base.texture.shader.ShaderTexture
import de.bixilon.minosoft.gui.rendering.util.mesh.Mesh
import de.bixilon.minosoft.gui.rendering.util.mesh.MeshStruct

class GUIMesh(
    context: RenderContext,
    val halfSize: Vec2,
    data: AbstractFloatList,
) : Mesh(context, GUIMeshStruct, initialCacheSize = 32768, data = data), GUIVertexConsumer {
    private val whiteTexture = context.textures.whiteTexture
    override val order = context.system.quadOrder

    override fun clear() = Unit


    override fun addVertex(x: Float, y: Float, texture: ShaderTexture?, u: Float, v: Float, tint: RGBColor, options: GUIVertexOptions?) {
        addVertex(data, halfSize, x, y, texture ?: whiteTexture.texture, u, v, tint, options)
    }

    override fun addVertex(x: Float, y: Float, textureId: Float, u: Float, v: Float, tint: Int, options: GUIVertexOptions?) {
        addVertex(data, halfSize, x, y, textureId, u, v, tint, options)
    }

    override fun addCache(cache: GUIMeshCache) {
        data.add(cache.data)
    }

    data class GUIMeshStruct(
        val position: Vec2,
        val uv: Vec2,
        val indexLayerAnimation: Int,
        val tintColor: RGBColor,
    ) {
        companion object : MeshStruct(GUIMeshStruct::class)
    }

    companion object {

        fun transformPosition(position: Vec2, halfSize: Vec2): Vec2 {
            val res = Vec2(position)
            res /= halfSize
            res.x -= 1.0f
            res.y = 1.0f - res.y
            return res
        }

        fun addVertex(data: AbstractFloatList, halfSize: Vec2, x: Float, y: Float, texture: ShaderIdentifiable, u: Float, v: Float, tint: RGBColor, options: GUIVertexOptions?) {
            addVertex(data, halfSize, x, y, texture.shaderId.buffer(), u, v, tint.rgba, options)
        }

        fun addVertex(data: AbstractFloatList, halfSize: Vec2, x: Float, y: Float, textureId: Float, u: Float, v: Float, tint: Int, options: GUIVertexOptions?) {
            val x = x / halfSize.x - 1.0f
            val y = 1.0f - y / halfSize.y


            var color = tint

            if (options != null) {
                options.tintColor?.let { color = RGBColor(tint).mix(it).rgba }

                if (options.alpha != 1.0f) {
                    val alpha = color and 0xFF
                    color = color and 0xFF.inv()

                    color = color or ((alpha * options.alpha).toInt() and 0xFF)
                }
            }

            data.add(x)
            data.add(y)
            data.add(u)
            data.add(v)
            data.add(textureId)
            data.add(color.buffer())
        }
    }

    override fun ensureSize(size: Int) {
        data.ensureSize(size)
    }
}
