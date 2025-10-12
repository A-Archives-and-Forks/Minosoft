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
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.registries.particle.data.ParticleData
import de.bixilon.kmath.vec.vec3.d.MVec3d
import de.bixilon.minosoft.gui.rendering.particle.ParticleFactory
import de.bixilon.minosoft.protocol.network.session.play.PlaySession
import de.bixilon.minosoft.util.KUtil.toResourceLocation

class SoulFireFlameParticle(session: PlaySession, position: Vec3d, velocity: MVec3d, data: ParticleData? = null) : FlameParticle(session, position, velocity, data) {

    companion object : ParticleFactory<SoulFireFlameParticle> {
        override val identifier: ResourceLocation = "minecraft:soul_fire_flame".toResourceLocation()

        override fun build(session: PlaySession, position: Vec3d, velocity: MVec3d, data: ParticleData): SoulFireFlameParticle {
            return SoulFireFlameParticle(session, position, velocity, data)
        }
    }
}
