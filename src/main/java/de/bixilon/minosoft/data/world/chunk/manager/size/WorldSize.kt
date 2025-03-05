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

package de.bixilon.minosoft.data.world.chunk.manager.size

import de.bixilon.kotlinglm.vec2.Vec2i

data class WorldSize(
    var min: Vec2i = Vec2i(Int.MAX_VALUE, Int.MAX_VALUE),
    var max: Vec2i = Vec2i(Int.MIN_VALUE, Int.MIN_VALUE),
    var size: Vec2i = Vec2i(0, 0),
) {

    fun clear() {
        min = Vec2i(Int.MAX_VALUE, Int.MAX_VALUE)
        max = Vec2i(Int.MIN_VALUE, Int.MIN_VALUE)
        size = Vec2i(0, 0)
    }
}
