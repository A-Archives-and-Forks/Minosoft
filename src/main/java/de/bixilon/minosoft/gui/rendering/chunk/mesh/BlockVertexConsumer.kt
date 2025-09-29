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

package de.bixilon.minosoft.gui.rendering.chunk.mesh

import glm_.vec2.Vec2
import glm_.vec3.Vec3
import de.bixilon.minosoft.data.text.formatting.color.RGBColor
import de.bixilon.minosoft.gui.rendering.models.block.element.FaceVertexData
import de.bixilon.minosoft.gui.rendering.system.base.texture.TextureTransparencies
import de.bixilon.minosoft.gui.rendering.system.base.texture.shader.ShaderTexture
import de.bixilon.minosoft.gui.rendering.util.mesh.AbstractVertexConsumer
import de.bixilon.minosoft.gui.rendering.util.mesh.uv.PackedUV
import de.bixilon.minosoft.gui.rendering.util.mesh.uv.UnpackedUV

interface BlockVertexConsumer : AbstractVertexConsumer {

    fun addVertex(position: FloatArray, uv: Vec2, texture: ShaderTexture, tintColor: RGBColor, lightIndex: Int)
    fun addVertex(x: Float, y: Float, z: Float, u: Float, v: Float, textureId: Float, lightTint: Float)
    fun addVertex(x: Float, y: Float, z: Float, uv: Float, textureId: Float, lightTint: Float)


    fun addQuad(offset: FloatArray, positions: FaceVertexData, uvData: PackedUV, textureId: Float, lightTint: Float, ao: IntArray) {
        ensureSize(ChunkMesh.ChunkMeshStruct.FLOATS_PER_VERTEX * order.size)

        order.iterate { position, uv ->
            val vertexOffset = position * Vec3.length


            val aUV = Float.fromBits(uvData.raw[uv].toBits() or (ao[position] shl 24))
            addVertex(
                x = offset[0] + positions[vertexOffset], y = offset[1] + positions[vertexOffset + 1], z = offset[2] + positions[vertexOffset + 2],
                uv = aUV,
                textureId = textureId,
                lightTint = lightTint,
            )
        }
    }

    fun addQuad(positions: FaceVertexData, uvData: UnpackedUV, textureId: Float, lightTint: Float) {
        ensureSize(ChunkMesh.ChunkMeshStruct.FLOATS_PER_VERTEX * order.size)

        order.iterate { position, uv ->
            val vertexOffset = position * Vec3.length
            val uvOffset = uv * Vec2.length
            addVertex(
                x = positions[vertexOffset], y = positions[vertexOffset + 1], z = positions[vertexOffset + 2],
                u = uvData.raw[uvOffset],
                v = uvData.raw[uvOffset + 1],
                textureId = textureId,
                lightTint = lightTint,
            )
        }
    }

    operator fun get(transparency: TextureTransparencies): BlockVertexConsumer = this
}
