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

package de.bixilon.minosoft.gui.rendering.particle.types.render.texture.simple.campfire

import de.bixilon.minosoft.data.world.vec.vec3.f.Vec3f
import de.bixilon.minosoft.data.world.vec.vec3.d.Vec3d
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.registries.particle.data.ParticleData
import de.bixilon.minosoft.gui.rendering.particle.ParticleFactory
import de.bixilon.minosoft.gui.rendering.particle.types.render.texture.simple.SimpleTextureParticle
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3dUtil.EMPTY
import de.bixilon.minosoft.protocol.network.session.play.PlaySession
import de.bixilon.minosoft.util.KUtil.toResourceLocation

class CampfireSmokeParticle(session: PlaySession, position: Vec3d, velocity: Vec3d, data: ParticleData? = null, signal: Boolean) : SimpleTextureParticle(session, position, Vec3d.EMPTY, data) {

    init {
        scale *= 3.0f
        spacing = Vec3f(0.25f)
        maxAge = random.nextInt(50)
        if (signal) {
            maxAge += 280
            color = color.with(alpha = 0.95f)
        } else {
            maxAge += 80
            color = color.with(alpha = 0.90f)
        }

        gravityStrength = 3.0E-6f

        this.velocity(Vec3d(velocity.x, velocity.y + (random.nextDouble() / 500.0), velocity.z))
        movement = false
        spriteDisabled = true
        setRandomSprite()
    }

    override fun tick() {
        super.tick()
        val horizontal = { (random.nextDouble() / 5000.0f * (if (random.nextBoolean()) 1.0f else -1.0f)) }
        velocity.x += horizontal()
        velocity.y -= gravityStrength
        velocity.z += horizontal()

        if (age >= maxAge - 60) {
            color = color.with(alpha = color.alphaf - 0.015f)
        }
        if (color.alpha == 0) {
            dead = true
        }
    }

    override fun postTick() {
        super.postTick()
        move(velocity)
    }


    object CosyFactory : ParticleFactory<CampfireSmokeParticle> {
        override val identifier: ResourceLocation = "minecraft:campfire_cosy_smoke".toResourceLocation()

        override fun build(session: PlaySession, position: Vec3d, velocity: Vec3d, data: ParticleData): CampfireSmokeParticle {
            return CampfireSmokeParticle(session, position, velocity, data, false)
        }
    }


    object SignalFactory : ParticleFactory<CampfireSmokeParticle> {
        override val identifier: ResourceLocation = "minecraft:campfire_signal_smoke".toResourceLocation()

        override fun build(session: PlaySession, position: Vec3d, velocity: Vec3d, data: ParticleData): CampfireSmokeParticle {
            return CampfireSmokeParticle(session, position, velocity, data, true)
        }
    }
}
