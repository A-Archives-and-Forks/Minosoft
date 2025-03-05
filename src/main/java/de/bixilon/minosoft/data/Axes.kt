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
package de.bixilon.minosoft.data

import de.bixilon.kutil.enums.EnumUtil
import de.bixilon.kutil.enums.ValuesEnum

enum class Axes {
    X {
        override fun next() = Y
        override fun previous() = Z
    },
    Y {
        override fun next() = Z
        override fun previous() = X
    },
    Z {
        override fun next() = X
        override fun previous() = Y
    },
    ;

    abstract fun next(): Axes
    abstract fun previous(): Axes

    companion object : ValuesEnum<Axes> {
        override val VALUES: Array<Axes> = values()
        override val NAME_MAP: Map<String, Axes> = EnumUtil.getEnumValues(VALUES)
    }
}
