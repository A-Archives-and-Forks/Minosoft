/*
 * Minosoft
 * Copyright (C) 2020-2024 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */
package de.bixilon.minosoft.protocol.packets.s2c.play.entity.spawn

import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.entities.data.EntityData
import de.bixilon.minosoft.data.entities.entities.decoration.Painting
import de.bixilon.minosoft.datafixer.rls.MotifFixer
import de.bixilon.minosoft.protocol.network.session.play.PlaySession
import de.bixilon.minosoft.protocol.packets.s2c.PlayS2CPacket
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions
import de.bixilon.minosoft.protocol.protocol.buffers.play.PlayInByteBuffer
import de.bixilon.minosoft.util.KUtil.startInit
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import java.util.*

class EntityPaintingS2CP(buffer: PlayInByteBuffer) : PlayS2CPacket {
    private val entityId: Int = buffer.readVarInt()
    private var entityUUID: UUID? = if (buffer.versionId >= ProtocolVersions.V_16W02A) {
        buffer.readUUID()
    } else {
        null
    }
    val entity: Painting


    init {
        val motif = if (buffer.versionId < ProtocolVersions.V_18W02A) {
            buffer.readLegacyRegistryItem(buffer.session.registries.motif, MotifFixer)!!
        } else {
            buffer.readRegistryItem(buffer.session.registries.motif)
        }
        val position: Vec3i
        val direction: Directions
        if (buffer.versionId < ProtocolVersions.V_14W04B) {
            position = buffer.readIntBlockPosition()
            direction = Directions[buffer.readInt()]
        } else {
            position = buffer.readBlockPosition()
            direction = Directions[buffer.readUnsignedByte()]
        }
        val type = buffer.session.registries.entityType[Painting.identifier]!!
        entity = Painting(buffer.session, type, EntityData(buffer.session), position, direction, motif)
        entity.startInit()
    }

    override fun handle(session: PlaySession) {
        session.world.entities.add(entityId, entityUUID, entity)
    }

    override fun log(reducedLog: Boolean) {
        Log.log(LogMessageType.NETWORK_IN, level = LogLevels.VERBOSE) { "Entity painting (entityId=$entityId, motif=${entity.motif}, direction=${entity.direction})" }
    }
}
