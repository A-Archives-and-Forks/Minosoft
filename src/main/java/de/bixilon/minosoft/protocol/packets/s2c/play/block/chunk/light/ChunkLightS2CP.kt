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
package de.bixilon.minosoft.protocol.packets.s2c.play.block.chunk.light


import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.minosoft.config.StaticConfiguration
import de.bixilon.minosoft.data.world.chunk.chunk.ChunkPrototype
import de.bixilon.minosoft.protocol.network.session.Session
import de.bixilon.minosoft.protocol.network.session.play.PlaySession
import de.bixilon.minosoft.protocol.packets.registry.PacketExtraHandler
import de.bixilon.minosoft.protocol.packets.s2c.PlayS2CPacket
import de.bixilon.minosoft.protocol.packets.s2c.play.block.chunk.light.LightUtil.readLightPacket
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions
import de.bixilon.minosoft.protocol.protocol.buffers.play.PlayInByteBuffer
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType

class ChunkLightS2CP(
    buffer: PlayInByteBuffer,
    val position: Vec2i = Vec2i(buffer.readVarInt(), buffer.readVarInt()),
) : PlayS2CPacket {
    var recalculateNeighbours: Boolean = false
        private set
    val prototype: ChunkPrototype

    init {
        if (buffer.versionId >= ProtocolVersions.V_1_16_PRE3 && buffer.versionId < ProtocolVersions.V_23W17A) {
            recalculateNeighbours = buffer.readBoolean()
        }

        val skyLightMask = buffer.readBitSet()
        val blockLightMask = buffer.readBitSet()
        val emptySkyLightMask = buffer.readBitSet()
        val emptyBlockLightMask = buffer.readBitSet()

        prototype = readLightPacket(buffer, skyLightMask, emptySkyLightMask, blockLightMask, emptyBlockLightMask, buffer.session.world.dimension)
    }

    override fun log(reducedLog: Boolean) {
        if (reducedLog) {
            return
        }
        Log.log(LogMessageType.NETWORK_IN, level = LogLevels.VERBOSE) { "Chunk light (position=$position)" }
    }

    override fun handle(session: PlaySession) {
        session.world.chunks[position] = this.prototype
    }

    companion object : PacketExtraHandler {
        const val LIGHT_SIZE = ProtocolDefinition.BLOCKS_PER_SECTION / 2

        override fun skip(session: Session) = StaticConfiguration.IGNORE_SERVER_LIGHT
    }
}
