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

package de.bixilon.minosoft.gui.rendering.skeletal.mesh

import de.bixilon.kmath.vec.vec3.f.Vec3f

object SkeletalMeshUtil {
    private const val ADD = +1.001f

    private fun encode(part: Float): Int {
        if (part <= -1.0f) return 0
        if (part >= 1.0f) return 0x0F
        val value = part * ADD
        if (value < 0.0f) return ((value + 1.0f) * 8.0f).toInt()
        return 8 + (value * 7.0f).toInt()
    }

    fun encodeNormal(normal: Vec3f): Int {
        val x = encode(normal.x) and 0x0F
        val y = encode(normal.y * ADD) and 0x0F
        val z = encode(normal.z) and 0x0F

        return (y shl 8) or (z shl 4) or (x)
    }
}
