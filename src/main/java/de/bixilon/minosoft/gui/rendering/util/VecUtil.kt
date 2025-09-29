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

package de.bixilon.minosoft.gui.rendering.util

import de.bixilon.kotlinglm.func.common.clamp
import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.kotlinglm.vec3.Vec3t
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.registries.blocks.types.properties.offset.RandomOffsetTypes
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import java.util.*

@Deprecated(message = "Use VecXUtil instead")
object VecUtil {

    @JvmName(name = "times2")
    infix operator fun Vec3d.times(lambda: () -> Double): Vec3d {
        return Vec3d(
            x = x * lambda(),
            y = y * lambda(),
            z = z * lambda(),
        )
    }

    infix operator fun Vec3.times(lambda: () -> Float): Vec3 {
        return Vec3(
            x = x * lambda(),
            y = y * lambda(),
            z = z * lambda(),
        )
    }

    infix fun Vec3.modify(lambda: (Float) -> Float): Vec3 {
        return Vec3(
            x = lambda(x),
            y = lambda(y),
            z = lambda(z),
        )
    }

    infix operator fun Vec3d.plus(lambda: () -> Double): Vec3d {
        return Vec3d(
            x = x + lambda(),
            y = y + lambda(),
            z = z + lambda(),
        )
    }

    fun Vec3d.Companion.of(lambda: () -> Double): Vec3d {
        return Vec3d(
            x = lambda(),
            y = lambda(),
            z = lambda(),
        )
    }

    infix operator fun Vec3i.plus(lambda: () -> Int): Vec3i {
        return Vec3i(
            x = x + lambda(),
            y = y + lambda(),
            z = z + lambda(),
        )
    }


    infix operator fun Vec3i.minus(lambda: () -> Int): Vec3i {
        return Vec3i(
            x = x - lambda(),
            y = y - lambda(),
            z = z - lambda(),
        )
    }

    infix operator fun Vec3d.plusAssign(lambda: () -> Double) {
        this(this + lambda)
    }

    infix operator fun Vec3d.timesAssign(lambda: () -> Double) {
        this(this * lambda)
    }

    val Float.sqr: Float
        get() = this * this

    val Vec3.ticks: Vec3
        get() = this / ProtocolDefinition.TICKS_PER_SECOND

    fun Vec3.rotate(axis: Vec3, sin: Float, cos: Float): Vec3 {
        return this * cos + (axis cross this) * sin + axis * (axis dot this) * (1 - cos)
    }

    inline val Int.inSectionHeight: Int
        get() = this and 0x0F

    inline val Int.sectionHeight: Int
        get() = this shr 4

    val Vec3i.centerf: Vec3
        get() = Vec3(x + 0.5f, y + 0.5f, z + 0.5f)

    val Vec3i.center: Vec3d
        get() = Vec3d(x + 0.5, y + 0.5, z + 0.5)


    inline infix operator fun Vec3i.plus(vec3: Vec3i?): Vec3i {
        if (vec3 == null) {
            return this
        }
        return Vec3i((x + vec3.x), (y + vec3.y), (z + vec3.z))
    }

    inline infix operator fun Vec3i.plus(vec2: Vec2i?): Vec3i {
        if (vec2 == null) {
            return this
        }
        return Vec3i((x + vec2.x), y, (z + vec2.y))
    }

    inline infix operator fun Vec3i.plus(direction: Directions?): Vec3i {
        return this + direction?.vectori
    }

    inline infix operator fun Vec3i.plusAssign(direction: Directions?) {
        this += direction?.vectori ?: return
    }

    inline infix operator fun Vec3i.plus(input: Vec3): Vec3 {
        return Vec3(input.x + x, input.y + y, input.z + z)
    }

    inline infix operator fun Vec2i.plus(vec3: Vec3i): Vec2i {
        return Vec2i(x + vec3.x, y + vec3.z)
    }

    inline infix operator fun Vec2i.plus(direction: Directions): Vec2i {
        return this + direction.vectori
    }

    fun BlockPosition.getWorldOffset(offsetType: RandomOffsetTypes): Vec3 {
        val positionHash = hash
        val maxModelOffset = 0.25f // ToDo: PixLyzer: use block.model.max_model_offset

        fun horizontal(axisHash: Long): Float {
            return (((axisHash and 0xF) / 15.0f) - 0.5f) / 2.0f
        }

        return Vec3(
            x = horizontal(positionHash),
            y = if (offsetType === RandomOffsetTypes.XYZ) {
                (((positionHash shr 4 and 0xF) / 15.0f) - 1.0f) / 5.0f
            } else {
                0.0f
            },
            z = horizontal(positionHash shr 8)).clamp(-maxModelOffset, maxModelOffset)
    }

    fun Vec3.clampAssign(min: Float, max: Float) {
        this.x = x.clamp(min, max)
        this.y = y.clamp(min, max)
        this.z = z.clamp(min, max)
    }
    fun Vec3.clamp(min: Float, max: Float): Vec3 {
        return Vec3(
            x = x.clamp(min, max),
            y = y.clamp(min, max),
            z = z.clamp(min, max),
        )
    }

    fun Vec3d.clamp(min: Double, max: Double): Vec3d {
        return Vec3d(
            x = x.clamp(min, max),
            y = y.clamp(min, max),
            z = z.clamp(min, max),
        )
    }

    val <T : Number> Vec3t<T>.toVec3: Vec3
        get() = Vec3(this)
    val Vec3d.toVec3: Vec3
        get() = Vec3(this)

    val <T : Number> Vec3t<T>.toVec3d: Vec3d
        get() = Vec3d(this)
    val Vec3.toVec3d: Vec3d
        get() = Vec3d(this)


    fun Vec3d.Companion.horizontal(xz: () -> Double, y: Double): Vec3d {
        return Vec3d(xz(), y, xz())
    }

    fun Vec3d.horizontalPlus(xz: () -> Double, y: Double): Vec3d {
        return Vec3d(this.x + xz(), this.y + y, this.z + xz())
    }

    fun Double.noised(random: Random): Double {
        return random.nextDouble() / this * if (random.nextBoolean()) 1.0 else -1.0
    }
}
