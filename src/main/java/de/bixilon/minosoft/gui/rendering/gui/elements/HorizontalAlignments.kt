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

package de.bixilon.minosoft.gui.rendering.gui.elements

import de.bixilon.minosoft.data.world.vec.vec2.f.Vec2f
import de.bixilon.kutil.enums.EnumUtil
import de.bixilon.kutil.enums.ValuesEnum

enum class HorizontalAlignments {
    LEFT,
    CENTER,
    RIGHT,
    ;

    companion object : ValuesEnum<HorizontalAlignments> {
        override val VALUES = values()
        override val NAME_MAP = EnumUtil.getEnumValues(VALUES)

        fun HorizontalAlignments.getOffset(width: Float, childWidth: Float): Float {
            return when (this) {
                LEFT -> 0.0f
                RIGHT -> width - childWidth
                CENTER -> (width - childWidth) / 2
            }
        }

        fun HorizontalAlignments.getOffset(size: Vec2f, childSize: Vec2f): Vec2f {
            return Vec2f(getOffset(size.x, childSize.x), 0f)
        }
    }
}
