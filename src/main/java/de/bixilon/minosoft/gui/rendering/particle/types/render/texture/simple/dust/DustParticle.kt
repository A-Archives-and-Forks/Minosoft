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

package de.bixilon.minosoft.gui.rendering.particle.types.render.texture.simple.dust

import de.bixilon.kutil.cast.CastUtil.nullCast
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.registries.particle.ParticleType
import de.bixilon.minosoft.data.registries.particle.data.DustParticleData
import de.bixilon.minosoft.data.registries.particle.data.ParticleData
import de.bixilon.minosoft.data.text.formatting.color.ChatColors
import de.bixilon.minosoft.gui.rendering.particle.ParticleFactory
import de.bixilon.minosoft.protocol.network.session.Session
import de.bixilon.minosoft.protocol.network.session.play.PlaySession
import de.bixilon.minosoft.protocol.protocol.buffers.play.PlayInByteBuffer
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import glm_.vec3.Vec3d

class DustParticle(session: PlaySession, position: Vec3d, velocity: Vec3d, data: DustParticleData) : AbstractDustParticle(session, position, velocity, data) {

    companion object : ParticleFactory<DustParticle> {
        override val identifier: ResourceLocation = "minecraft:dust".toResourceLocation()

        override fun build(session: PlaySession, position: Vec3d, velocity: Vec3d, data: ParticleData): DustParticle {
            return DustParticle(session, position, velocity, data.nullCast<DustParticleData>() ?: DustParticleData(ChatColors.WHITE, 1.0f, data.type))
        }

        override fun read(session: Session, buffer: PlayInByteBuffer, type: ParticleType): ParticleData {
            return DustParticleData.read(buffer, type)
        }
    }
}
