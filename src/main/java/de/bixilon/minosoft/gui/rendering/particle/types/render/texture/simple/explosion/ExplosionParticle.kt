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

package de.bixilon.minosoft.gui.rendering.particle.types.render.texture.simple.explosion

import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.registries.particle.data.ParticleData
import de.bixilon.minosoft.data.text.formatting.color.RGBColor.Companion.asGray
import de.bixilon.minosoft.gui.rendering.particle.ParticleFactory
import de.bixilon.minosoft.gui.rendering.particle.types.render.texture.simple.SimpleTextureParticle
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3dUtil.EMPTY
import de.bixilon.minosoft.protocol.network.session.play.PlaySession
import de.bixilon.minosoft.util.KUtil.toResourceLocation

class ExplosionParticle(session: PlaySession, position: Vec3d, data: ParticleData? = null, power: Float = 1.0f) : SimpleTextureParticle(session, position, Vec3d.EMPTY, data) {

    init {
        movement = false
        maxAge = 6 + random.nextInt(4)
        val gray = random.nextFloat() * 0.6f + 0.4f
        color = gray.asGray().rgba()
        scale = 2.0f * (power - gray * 0.5f)
    }

    companion object : ParticleFactory<ExplosionParticle> {
        override val identifier: ResourceLocation = "minecraft:explosion".toResourceLocation()

        override fun build(session: PlaySession, position: Vec3d, velocity: Vec3d, data: ParticleData): ExplosionParticle {
            return ExplosionParticle(session, position, data, velocity.x.toFloat())
        }
    }
}
