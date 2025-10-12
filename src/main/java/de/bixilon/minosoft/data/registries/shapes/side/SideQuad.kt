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

package de.bixilon.minosoft.data.registries.shapes.side

import de.bixilon.kmath.vec.vec2.f.Vec2f
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet

data class SideQuad(
    val min: Vec2f,
    val max: Vec2f,
) {
    private var hashCode = 0 // lazy

    constructor(minX: Float, minZ: Float, maxX: Float, maxZ: Float) : this(Vec2f(minOf(minX, maxX), minOf(minZ, maxZ)), Vec2f(maxOf(minX, maxX), maxOf(minZ, maxZ)))
    constructor(minX: Double, minZ: Double, maxX: Double, maxZ: Double) : this(Vec2f(minOf(minX, maxX).toFloat(), minOf(minZ, maxZ).toFloat()), Vec2f(maxOf(minX, maxX).toFloat(), maxOf(minZ, maxZ).toFloat()))


    fun touches(other: SideQuad): Boolean {
        return !(this.min.x > other.max.x || other.min.x > this.max.x || this.min.y > other.max.y || other.min.y > this.max.y)
    }

    infix operator fun minus(set: VoxelSide): VoxelSide {
        val result: MutableSet<SideQuad> = ObjectOpenHashSet()

        for (side in set.sides) {
            result += (this minus side).sides
        }

        return VoxelSide(result)
    }

    infix operator fun minus(other: SideQuad): VoxelSide {
        val result: MutableSet<SideQuad> = ObjectOpenHashSet()


        if (other.min.x > min.x && other.min.x < max.x) {
            result += SideQuad(min.x, min.y, other.min.x, max.y)
        }
        if (other.min.y > min.y && other.min.y < max.y) {
            result += SideQuad(min.x, min.y, max.x, other.min.y)
        }

        if (max.x > other.max.x) {
            result += SideQuad(other.max.x, min.y, max.x, max.y)
        }
        if (max.y > other.max.y) {
            result += SideQuad(min.x, other.max.y, max.x, max.y)
        }


        return VoxelSide(result)
    }

    private fun _hashCode(): Int {
        var result = 1
        result = 31 * result + min.hashCode()
        result = 31 * result + max.hashCode()
        return result
    }

    override fun hashCode(): Int {
        if (hashCode == 0) {
            hashCode = _hashCode()
        }
        return hashCode
    }

    override fun equals(other: Any?): Boolean {
        if (other !is SideQuad) return false
        if (hashCode != other.hashCode) return false
        return min == other.min && max == other.max
    }

    fun surfaceArea(): Float {
        val surface = (max.x - min.x) * (max.y - min.y)
        if (surface <= 0.0f) return 0.0f
        return surface
    }
}
