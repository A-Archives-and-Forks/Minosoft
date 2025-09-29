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
package de.bixilon.minosoft.protocol.packets.s2c.play.entity.move

import glm_.vec3.Vec3d
import de.bixilon.kutil.bit.BitByte.isBitMask
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.protocol.network.session.play.PlaySession
import de.bixilon.minosoft.protocol.network.session.play.PlaySessionStates
import de.bixilon.minosoft.protocol.packets.c2s.play.entity.move.ConfirmTeleportC2SP
import de.bixilon.minosoft.protocol.packets.c2s.play.entity.move.PositionRotationC2SP
import de.bixilon.minosoft.protocol.packets.s2c.PlayS2CPacket
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions
import de.bixilon.minosoft.protocol.protocol.buffers.play.PlayInByteBuffer
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType

class PositionRotationS2CP(buffer: PlayInByteBuffer) : PlayS2CPacket {
    val position: Vec3d = Vec3d(buffer.readDoubleArray(3))
    val rotation: EntityRotation
    var onGround = false
    private var flags: Int = 0
    var teleportId = 0
    private var dismountVehicle = true

    init {
        rotation = EntityRotation(buffer.readFloat(), buffer.readFloat())
        if (buffer.versionId < ProtocolVersions.V_14W03B) {
            onGround = buffer.readBoolean()
        } else {
            flags = buffer.readUnsignedByte()

            if (buffer.versionId >= ProtocolVersions.V_15W42A) {
                teleportId = buffer.readVarInt()
            }
            if (buffer.versionId >= ProtocolVersions.V_21W05A && buffer.versionId < ProtocolVersions.V_1_19_4_PRE1) {
                dismountVehicle = buffer.readBoolean()
            }
        }
    }

    override fun handle(session: PlaySession) {
        val entity = session.player
        // correct position with flags (relative position possible)
        val position = Vec3d(this.position)
        val velocity = Vec3d(entity.physics.velocity)
        if (flags.isBitMask(0x01)) {
            position.x += entity.physics.position.x
        } else {
            velocity.x = 0.0
        }
        if (flags.isBitMask(0x02)) {
            position.y += entity.physics.position.y
        } else {
            velocity.y = 0.0
        }
        if (flags.isBitMask(0x04)) {
            position.z += entity.physics.position.z
        } else {
            velocity.z = 0.0
        }

        var yaw = rotation.yaw
        if (flags.isBitMask(0x08)) {
            yaw += entity.physics.rotation.yaw
        }

        var pitch = rotation.pitch
        if (flags.isBitMask(0x10)) {
            pitch += entity.physics.rotation.pitch
        }

        entity.physics.velocity = velocity
        entity.forceTeleport(position)
        entity.forceRotate(EntityRotation(yaw, pitch))

        if (session.version.versionId >= ProtocolVersions.V_15W42A) {
            session.connection.send(ConfirmTeleportC2SP(teleportId))
        }
        session.connection.send(PositionRotationC2SP(position, position.y + entity.physics.eyeHeight, rotation, onGround))

        if (session.state == PlaySessionStates.SPAWNING) {
            session.state = PlaySessionStates.PLAYING
        }
    }

    override fun log(reducedLog: Boolean) {
        Log.log(LogMessageType.NETWORK_IN, level = LogLevels.VERBOSE) { "Position + rotation (position=$position, rotation=$rotation, onGround=$onGround, flags=$flags, teleportId=$teleportId, dismountVehicle=$dismountVehicle)" }
    }
}
