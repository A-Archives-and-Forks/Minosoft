/*
 * Minosoft
 * Copyright (C) 2020-2024 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */
package de.bixilon.minosoft.data.direction

import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.kutil.cast.CastUtil.unsafeNull
import de.bixilon.kutil.enums.EnumUtil
import de.bixilon.kutil.enums.ValuesEnum
import de.bixilon.kutil.reflection.ReflectionUtil.forceSet
import de.bixilon.kutil.reflection.ReflectionUtil.jvmField
import de.bixilon.minosoft.data.Axes
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3iUtil.get

enum class Directions(
    val vector: Vec3i,
    val index: Vec3i,
) {
    DOWN(Vec3i(0, -1, 0), Vec3i(1, -1, 1)),
    UP(Vec3i(0, 1, 0), Vec3i(3, -1, 3)),
    NORTH(Vec3i(0, 0, -1), Vec3i(0, 0, -1)),
    SOUTH(Vec3i(0, 0, 1), Vec3i(2, 2, -1)),
    WEST(Vec3i(-1, 0, 0), Vec3i(-1, 3, 2)),
    EAST(Vec3i(1, 0, 0), Vec3i(-1, 1, 0)),
    ;

    val negative = ordinal % 2 == 0

    val vectorf = Vec3(vector)
    val vectord = Vec3d(vector)

    val axis: Axes = unsafeNull()
    val inverted: Directions = unsafeNull()

    private fun invert(): Directions {
        val ordinal = ordinal
        return if (ordinal % 2 == 0) {
            Directions[ordinal + 1]
        } else {
            Directions[ordinal - 1]
        }
    }

    operator fun get(axis: Axes): Int {
        return vector[axis]
    }

    companion object : ValuesEnum<Directions> {
        const val O_DOWN = 0 // Directions.DOWN.ordinal
        const val O_UP = 1 // Directions.UP.ordinal
        const val O_NORTH = 2 // Directions.NORTH.ordinal
        const val O_SOUTH = 3 // Directions.SOUTH.ordinal
        const val O_WEST = 4 // Directions.WEST.ordinal
        const val O_EAST = 5 // Directions.EAST.ordinal

        const val SIZE = 6
        const val SIZE_SIDES = 4
        const val SIDE_OFFSET = 2
        override val VALUES = values()
        override val NAME_MAP: Map<String, Directions> = EnumUtil.getEnumValues(VALUES)
        val SIDES = arrayOf(NORTH, SOUTH, WEST, EAST)

        val INDEXED = arrayOf(
            arrayOf(NORTH, DOWN, SOUTH, UP), // X
            arrayOf(NORTH, EAST, SOUTH, WEST), // y
            arrayOf(EAST, DOWN, WEST, UP), // z
        )

        val XYZ = arrayOf(WEST, EAST, DOWN, UP, NORTH, SOUTH)

        private const val MIN_ERROR = 0.0001f

        @Deprecated("outsource")
        fun byDirection(direction: Vec3): Directions {
            var minDirection = VALUES[0]
            var minError = 2.0f
            for (testDirection in VALUES) {
                val error = (testDirection.vectorf - direction).length()
                if (error < MIN_ERROR) {
                    return testDirection
                } else if (error < minError) {
                    minError = error
                    minDirection = testDirection
                }
            }
            return minDirection
        }

        init {
            val inverted = Directions::inverted.jvmField
            val axis = Directions::axis.jvmField
            for (direction in VALUES) {
                inverted.forceSet(direction, direction.invert())
                axis.forceSet(direction, Axes[direction])
            }
            NAME_MAP.unsafeCast<MutableMap<String, Directions>>()["bottom"] = DOWN
        }
    }
}
