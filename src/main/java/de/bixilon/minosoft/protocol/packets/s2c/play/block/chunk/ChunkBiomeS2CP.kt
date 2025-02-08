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
package de.bixilon.minosoft.protocol.packets.s2c.play.block.chunk

import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.kutil.cast.CastUtil.cast
import de.bixilon.kutil.cast.CastUtil.nullCast
import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.minosoft.data.registries.biomes.Biome
import de.bixilon.minosoft.data.world.biome.source.SpatialBiomeArray
import de.bixilon.minosoft.data.world.container.palette.PalettedContainerReader
import de.bixilon.minosoft.data.world.container.palette.palettes.BiomePaletteFactory
import de.bixilon.minosoft.protocol.network.session.play.PlaySession
import de.bixilon.minosoft.protocol.packets.s2c.PlayS2CPacket
import de.bixilon.minosoft.protocol.protocol.buffers.play.PlayInByteBuffer
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType

class ChunkBiomeS2CP(buffer: PlayInByteBuffer) : PlayS2CPacket {
    val data = buffer.readArray { ChunkBiomeData(buffer.readLongChunkPosition(), buffer.readByteArray()) }


    data class ChunkBiomeData(
        val position: Vec2i,
        val data: ByteArray,
    )

    override fun handle(session: PlaySession) {
        if (data.isEmpty()) return

        // TODO: Test implementation
        for ((position, data) in data) {
            val chunk = session.world.chunks[position] ?: continue
            val source = chunk.biomeSource.nullCast<SpatialBiomeArray>() ?: continue
            val buffer = PlayInByteBuffer(data, session)
            for (sectionIndex in (0 until chunk.sections.size)) {
                val data = PalettedContainerReader.unpack<Biome?>(buffer, buffer.session.registries.biome.unsafeCast(), factory = BiomePaletteFactory) ?: continue

                source.data[sectionIndex] = data.cast()
            }
        }
        session.world.biomes.resetCache()
    }

    override fun log(reducedLog: Boolean) {
        Log.log(LogMessageType.NETWORK_IN, level = LogLevels.VERBOSE) { "Chunk biome (data=$data)" }
    }
}
