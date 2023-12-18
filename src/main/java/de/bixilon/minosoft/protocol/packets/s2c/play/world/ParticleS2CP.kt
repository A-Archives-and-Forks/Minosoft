/*
 * Minosoft
 * Copyright (C) 2020-2023 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */
package de.bixilon.minosoft.protocol.packets.s2c.play.world

import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.minosoft.data.registries.particle.data.ParticleData
import de.bixilon.minosoft.gui.rendering.util.VecUtil.times
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.packets.s2c.PlayS2CPacket
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions
import de.bixilon.minosoft.protocol.protocol.buffers.play.PlayInByteBuffer
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType

class ParticleS2CP(buffer: PlayInByteBuffer) : PlayS2CPacket {
    val type = buffer.readParticleType()
    val longDistance = if (buffer.versionId >= ProtocolVersions.V_14W29A) buffer.readBoolean() else false
    val position: Vec3d = if (buffer.versionId < ProtocolVersions.V_1_15_PRE4) {
        Vec3d(buffer.readVec3f())
    } else {
        buffer.readVec3d()
    }
    val offset: Vec3 = buffer.readVec3f()
    val speed: Float = buffer.readFloat()
    val count: Int = buffer.readInt()
    val data: ParticleData = buffer.readParticleData(type)


    private fun PlayInByteBuffer.readParticleType() = when {
        versionId >= ProtocolVersions.V_22W17A -> readRegistryItem(connection.registries.particleType) // ToDo: maybe this was even earlier, should only differ some snapshots
        versionId >= ProtocolVersions.V_14W19A -> connection.registries.particleType[readInt()]
        else -> readLegacyRegistryItem(connection.registries.particleType)!!
    }

    override fun handle(connection: PlayConnection) {
        if (!connection.profiles.particle.types.packet) {
            return
        }
        val renderer = connection.world.particle ?: return

        fun spawn(position: Vec3d, velocity: Vec3d) {
            val factory = data.type.factory ?: return
            renderer += factory.build(connection, position, velocity, data) ?: return
        }

        if (count <= 1) {
            return spawn(position, Vec3d(offset * speed))
        }
        for (i in 0 until count) {
            val offset = Vec3d(offset) * { renderer.random.nextGaussian() }
            val velocity = Vec3d(speed) * { renderer.random.nextGaussian() }

            spawn(position + offset, velocity)
        }
    }

    override fun log(reducedLog: Boolean) {
        if (reducedLog) {
            return
        }
        Log.log(LogMessageType.NETWORK_IN, level = LogLevels.VERBOSE) { "Particle (type=$type, longDistance=$longDistance, position=$position, offset=$offset, speed=$speed, count=$count, data=$data)" }
    }
}
