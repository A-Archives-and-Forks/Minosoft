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

package de.bixilon.minosoft.data.registries.shapes

import de.bixilon.kutil.array.ArrayUtil.cast
import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.kutil.json.JsonObject
import de.bixilon.minosoft.data.registries.shapes.aabb.AABB
import de.bixilon.minosoft.data.registries.shapes.shape.Shape

class ShapeRegistry {
    private var shapes: Array<Shape?> = emptyArray()

    fun load(data: JsonObject?) {
        if (data == null) {
            return
        }
        val aabbs = loadAABBs(data["aabbs"].unsafeCast())
        loadShapes(data["shapes"].unsafeCast(), aabbs)
    }

    private fun loadShapes(data: Collection<Any>, aabbs: Array<AABB>) {
        this.shapes = arrayOfNulls(data.size)

        for ((index, shape) in data.withIndex()) {
            this.shapes[index] = Shape.deserialize(shape, aabbs)
        }
    }

    private fun loadAABBs(data: Collection<Map<String, Any>>): Array<AABB> {
        val aabbs: Array<AABB?> = arrayOfNulls(data.size)

        for ((index, aabb) in data.withIndex()) {
            aabbs[index] = AABB(aabb)
        }
        return aabbs.cast()
    }

    fun cleanup() {
        this.shapes = emptyArray()
    }


    operator fun get(index: Int): Shape? {
        return shapes[index]
    }
}
