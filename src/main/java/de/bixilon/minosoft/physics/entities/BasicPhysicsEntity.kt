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

package de.bixilon.minosoft.physics.entities

import de.bixilon.minosoft.data.world.vec.vec3.d.Vec3d
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3dUtil.EMPTY

abstract class BasicPhysicsEntity {
    var position = Vec3d.EMPTY
        protected set
    open var velocity = Vec3d.EMPTY

    open fun preTick() {}
    open fun tick() {}
    open fun postTick() {}

    open fun forceTeleport(position: Vec3d) {
        this.position = position
    }

    open fun forceMove(delta: Vec3d) {
        if (delta.length2() < 1.0E-7) return

        forceTeleport(position + delta)
    }
}
