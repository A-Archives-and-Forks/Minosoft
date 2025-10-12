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

package de.bixilon.minosoft.gui.rendering.particle.types.render.texture.simple.slowing

import de.bixilon.kmath.vec.vec3.d.Vec3d
import de.bixilon.minosoft.data.registries.particle.data.ParticleData
import de.bixilon.kmath.vec.vec3.d.MVec3d
import de.bixilon.minosoft.gui.rendering.particle.types.render.texture.simple.SimpleTextureParticle
import de.bixilon.minosoft.protocol.network.session.play.PlaySession

abstract class SlowingParticle(session: PlaySession, position: Vec3d, velocity: MVec3d, data: ParticleData? = null) : SimpleTextureParticle(session, position, velocity, data) {

    init {
        friction = 0.96f
        this.velocity(this.velocity * 0.009999999776482582 + velocity)
        forceMove { (random.nextDouble() - random.nextDouble()) * 0.05 }
        maxAge = (8.0 / (random.nextDouble() * 0.8 + 0.2)).toInt() + 4
    }
}
