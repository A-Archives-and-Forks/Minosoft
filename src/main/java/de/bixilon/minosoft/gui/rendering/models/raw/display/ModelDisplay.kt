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

package de.bixilon.minosoft.gui.rendering.models.raw.display

import glm_.mat4x4.Mat4
import glm_.vec3.Vec3
import de.bixilon.kutil.json.JsonObject
import de.bixilon.minosoft.gui.rendering.models.block.element.ModelElement.Companion.BLOCK_SIZE
import de.bixilon.minosoft.gui.rendering.util.mat.mat4.Mat4Util.rotateRadAssign
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3Util.EMPTY_INSTANCE
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3Util.ONE
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3Util.rad
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3Util.toVec3

data class ModelDisplay(
    val rotation: Vec3 = Vec3.EMPTY_INSTANCE,
    val translation: Vec3 = Vec3.EMPTY_INSTANCE,
    val scale: Vec3 = Vec3.ONE,
) {
    val matrix = Mat4()
        .scaleAssign(scale)
        .translateAssign(translation)
        .translateAssign(CENTER)
        .rotateRadAssign(rotation)
        .translateAssign(N_CENTER)

    companion object {
        val CENTER = Vec3(0.5f)
        val N_CENTER = -CENTER
        val DEFAULT = ModelDisplay()

        fun deserialize(data: JsonObject): ModelDisplay {
            return ModelDisplay(
                rotation = data["rotation"]?.toVec3()?.rad ?: Vec3.EMPTY_INSTANCE,
                translation = data["translation"]?.toVec3()?.apply { this /= BLOCK_SIZE } ?: Vec3.EMPTY_INSTANCE,
                scale = data["scale"]?.toVec3() ?: Vec3.ONE,
            )
        }
    }
}
