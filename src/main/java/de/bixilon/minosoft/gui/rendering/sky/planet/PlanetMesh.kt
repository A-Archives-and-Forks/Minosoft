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

package de.bixilon.minosoft.gui.rendering.sky.planet

import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.system.base.MeshUtil.buffer
import de.bixilon.minosoft.gui.rendering.system.base.buffer.vertex.PrimitiveTypes
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.Texture
import de.bixilon.minosoft.gui.rendering.util.mesh.Mesh
import de.bixilon.minosoft.gui.rendering.util.mesh.MeshStruct
import de.bixilon.minosoft.gui.rendering.util.mesh.uv.UnpackedUV

open class PlanetMesh(context: RenderContext, primitiveType: PrimitiveTypes = context.system.quadType) : Mesh(context, SunMeshStruct, primitiveType, initialCacheSize = 2 * 3 * SunMeshStruct.FLOATS_PER_VERTEX) {

    fun addVertex(position: Vec3, texture: Texture, uv: Vec2) {
        data.add(position.array)
        data.add(uv.array)
        data.add(texture.renderData.shaderTextureId.buffer())
    }


    data class SunMeshStruct(
        val position: Vec3,
        val uv: UnpackedUV,
        val indexLayerAnimation: Int,
    ) {
        companion object : MeshStruct(SunMeshStruct::class)
    }
}
