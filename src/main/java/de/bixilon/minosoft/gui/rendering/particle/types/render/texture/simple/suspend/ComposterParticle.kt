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

package de.bixilon.minosoft.gui.rendering.particle.types.render.texture.simple.suspend

import de.bixilon.kmath.vec.vec3.d.MVec3d
import de.bixilon.kmath.vec.vec3.d.Vec3d
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.registries.particle.data.ParticleData
import de.bixilon.minosoft.data.text.formatting.color.ChatColors
import de.bixilon.minosoft.gui.rendering.particle.ParticleFactory
import de.bixilon.minosoft.protocol.network.session.play.PlaySession
import de.bixilon.minosoft.util.KUtil.toResourceLocation

class ComposterParticle(session: PlaySession, position: Vec3d, velocity: MVec3d, data: ParticleData? = null) : SuspendParticle(session, position, velocity, data) {

    init {
        color = ChatColors.WHITE
        maxAge = (3 + random.nextInt(5))
    }

    companion object : ParticleFactory<ComposterParticle> {
        override val identifier: ResourceLocation = "minecraft:composter".toResourceLocation()

        override fun build(session: PlaySession, position: Vec3d, velocity: MVec3d, data: ParticleData): ComposterParticle {
            return ComposterParticle(session, position, velocity, data)
        }
    }
}
