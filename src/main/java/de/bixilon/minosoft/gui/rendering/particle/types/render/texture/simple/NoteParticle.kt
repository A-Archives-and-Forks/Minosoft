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

package de.bixilon.minosoft.gui.rendering.particle.types.render.texture.simple

import de.bixilon.kmath.vec.vec3.d.MVec3d
import de.bixilon.kmath.vec.vec3.d.Vec3d
import de.bixilon.kutil.math.MathConstants.PIf
import de.bixilon.kutil.math.Trigonometry.sin
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.registries.particle.data.ParticleData
import de.bixilon.minosoft.data.text.formatting.color.RGBAColor
import de.bixilon.minosoft.gui.rendering.particle.ParticleFactory
import de.bixilon.minosoft.protocol.network.session.play.PlaySession
import de.bixilon.minosoft.util.KUtil.toResourceLocation

class NoteParticle(session: PlaySession, position: Vec3d, colorModifier: Float, data: ParticleData? = null) : SimpleTextureParticle(session, position, MVec3d.EMPTY, data) {

    init {
        this.friction = 0.66f
        this.velocity *= 0.009999999776482582
        accelerateIfYBlocked = true
        this.velocity.y += 0.2

        fun getColor(offset: Float): Float {
            return maxOf(0.0f, sin((colorModifier + offset) * 2.0f * PIf) * 0.65f + 0.35f)
        }

        this.color = RGBAColor(
            red = getColor(0.0f),
            green = getColor(ONE_THIRD),
            blue = getColor(TWO_THIRD),
        )

        this.scale *= 1.5f
        this.maxAge = 6
    }


    companion object : ParticleFactory<NoteParticle> {
        override val identifier: ResourceLocation = "minecraft:note".toResourceLocation()
        private const val ONE_THIRD = 1.0f / 3
        private const val TWO_THIRD = ONE_THIRD * 2

        override fun build(session: PlaySession, position: Vec3d, velocity: MVec3d, data: ParticleData): NoteParticle {
            return NoteParticle(session, position, velocity.x.toFloat(), data)
        }
    }
}
