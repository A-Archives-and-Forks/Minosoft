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

package de.bixilon.minosoft.gui.rendering.particle.types.norender.emitter

import de.bixilon.minosoft.data.world.vec.vec3.f.Vec3f
import de.bixilon.minosoft.data.world.vec.vec3.d.Vec3d
import de.bixilon.kutil.random.RandomUtil.nextFloat
import de.bixilon.minosoft.data.entities.entities.Entity
import de.bixilon.minosoft.gui.rendering.particle.ParticleFactory
import de.bixilon.minosoft.gui.rendering.particle.types.norender.NoRenderParticle
import de.bixilon.minosoft.protocol.network.session.play.PlaySession

class EntityEmitterParticle(
    session: PlaySession,
    val entity: Entity,
    val particleFactory: ParticleFactory<*>,
    velocity: Vec3d = Vec3d.EMPTY,
    maxAge: Int = 3,
) : NoRenderParticle(session, entity.physics.velocity, velocity, null) {
    private val particleData = session.registries.particleType[particleFactory.identifier]!!.default()


    init {
        val cameraPosition = entity.renderInfo.position
        this.position(Vec3d(cameraPosition.x, cameraPosition.y + entity.type.height * 0.5, cameraPosition.z))
        this.maxAge = maxAge
        movement = false
        tick()
    }


    fun emitParticles() {
        val particle = session.world.particle ?: return
        val position = entity.physics.position
        for (i in 0 until 16) {
            val scale = Vec3f(random.nextFloat(-1.0f, 1.0f), random.nextFloat(-1.0f, 1.0f), random.nextFloat(-1.0f, 1.0f))

            if (scale.length2() < 1.0f) {
                continue
            }

            val particlePosition = Vec3d(
                position.x + (entity.type.width * (scale.x / 4.0f)),
                position.y + (entity.type.height * (0.5f + scale.y / 4.0f)),
                position.z + (entity.type.width * (scale.z / 4.0f)),
            )
            particle += particleFactory.build(session, particlePosition, Vec3d(scale.x, scale.y, scale.z), particleData) ?: continue// ToDo: Velocity.y is getting added with 0.2
        }
    }

    override fun tick() {
        super.tick()
        if (dead) {
            return
        }
        emitParticles()
    }
}
