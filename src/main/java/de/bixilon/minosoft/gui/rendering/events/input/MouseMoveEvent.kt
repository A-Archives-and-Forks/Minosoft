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

package de.bixilon.minosoft.gui.rendering.events.input

import glm_.vec2.Vec2d
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.events.RenderEvent

class MouseMoveEvent(
    context: RenderContext,
    position: Vec2d,
    previous: Vec2d,
    delta: Vec2d,
) : RenderEvent(context) {
    val position: Vec2d = position
        get() = Vec2d(field)

    val previous: Vec2d = previous
        get() = Vec2d(field)

    val delta: Vec2d = delta
        get() = Vec2d(field)
}
