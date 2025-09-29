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

import de.bixilon.kutil.array.ArrayUtil.cast
import de.bixilon.kutil.compression.zlib.ZlibUtil.decompress
import de.bixilon.kutil.exception.Broken
import de.bixilon.kutil.json.JsonObject
import de.bixilon.kutil.json.JsonUtil.asJsonObject
import de.bixilon.kutil.json.JsonUtil.toJsonObject
import de.bixilon.kutil.memory.allocator.ByteAllocator
import de.bixilon.kutil.primitive.IntUtil.toInt
import de.bixilon.minosoft.config.StaticConfiguration
import de.bixilon.minosoft.data.registries.biomes.Biome
import de.bixilon.minosoft.data.registries.dimension.DimensionProperties
import de.bixilon.minosoft.data.world.biome.source.SpatialBiomeArray
import de.bixilon.minosoft.data.world.chunk.chunk.ChunkPrototype
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.data.world.positions.ChunkPosition
import de.bixilon.minosoft.data.world.positions.InChunkPosition
import de.bixilon.minosoft.datafixer.rls.BlockEntityFixer.fixBlockEntity
import de.bixilon.minosoft.protocol.network.session.play.PlaySession
import de.bixilon.minosoft.protocol.packets.s2c.PlayS2CPacket
import de.bixilon.minosoft.protocol.packets.s2c.play.block.chunk.light.ChunkLightS2CP
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_14W26A
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_14W28A
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_15W34C
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_15W36D
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_18W44A
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_19W36A
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_1_16_2_PRE2
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_1_16_PRE7
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_1_9_4
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_20W45A
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_21W03A
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_21W37A
import de.bixilon.minosoft.protocol.protocol.buffers.play.PlayInByteBuffer
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import java.util.*

class ChunkS2CP(buffer: PlayInByteBuffer) : PlayS2CPacket {
    val position: ChunkPosition
    val prototype: ChunkPrototype = ChunkPrototype()
    var action: ChunkAction = ChunkAction.CREATE
        private set
    private lateinit var readingData: ChunkReadingData

    init {
        val dimension = buffer.session.world.dimension
        position = buffer.readChunkPosition()
        if (buffer.versionId < V_20W45A) {
            action = if (buffer.readBoolean()) ChunkAction.CREATE else ChunkAction.UPDATE
        }
        if (buffer.versionId < V_14W26A) { // ToDo
            val sectionBitMask = BitSet.valueOf(buffer.readByteArray(2))
            val addBitMask = BitSet.valueOf(buffer.readByteArray(2))

            // decompress chunk data
            val decompressed: PlayInByteBuffer = if (buffer.versionId < V_14W28A) {
                PlayInByteBuffer(buffer.readByteArray(buffer.readInt()).decompress(), buffer.session)
            } else {
                buffer
            }
            val chunkData = ChunkPacketUtil.readChunkPacket(decompressed, dimension, sectionBitMask, addBitMask, action == ChunkAction.CREATE, dimension.skyLight)
            if (chunkData == null) {
                action = ChunkAction.UNLOAD
            } else {
                this.prototype.update(chunkData)
            }
        } else {
            if (buffer.versionId in V_1_16_PRE7 until V_1_16_2_PRE2) {
                buffer.readBoolean() // ToDo: ignore old data???
            }
            val sectionBitMask = when {
                buffer.versionId < V_15W34C -> buffer.readLegacyBitSet(2)
                buffer.versionId < V_15W36D -> buffer.readLegacyBitSet(4)
                buffer.versionId < V_21W03A -> BitSet.valueOf(longArrayOf(buffer.readVarInt().toLong()))
                buffer.versionId < V_21W37A -> BitSet.valueOf(buffer.readLongArray())
                else -> null
            }
            if (buffer.versionId >= V_18W44A) {
                buffer.readNBT()?.toJsonObject() // heightmap
            }
            if (action == ChunkAction.CREATE && buffer.versionId >= V_19W36A && buffer.versionId < V_21W37A) {
                this.prototype.biomeSource = SpatialBiomeArray(buffer.readBiomeArray())
            }
            val length = buffer.readVarInt()
            val data = ALLOCATOR.allocate(length)
            buffer.readByteArray(data, 0, length)
            readingData = ChunkReadingData(data, PlayInByteBuffer(data, buffer.session), dimension, sectionBitMask)

            // set position to expected read positions; the server sometimes sends a bunch of useless zeros (~ 190k), thanks @pokechu22

            this.prototype.blockEntities = buffer.readBlockEntities(dimension)

            if (buffer.versionId >= V_21W37A) {
                if (StaticConfiguration.IGNORE_SERVER_LIGHT) {
                    buffer.pointer = buffer.size
                } else {
                    this.prototype.update(ChunkLightS2CP(buffer, position).prototype)
                }
            }
        }
    }

    private fun PlayInByteBuffer.readBlockEntities(dimension: DimensionProperties): Map<InChunkPosition, JsonObject>? {
        if (versionId < V_1_9_4) return null
        val count = readVarInt()
        if (count <= 0) return null
        val entities: MutableMap<InChunkPosition, JsonObject> = HashMap(count)

        when {
            versionId < V_21W37A -> {
                val positionOffset = BlockPosition.of(position, dimension.minSection)
                for (i in 0 until count) {
                    val nbt = readNBT()?.asJsonObject() ?: continue
                    val position = BlockPosition(nbt["x"]?.toInt() ?: continue, nbt["y"]?.toInt() ?: continue, nbt["z"]?.toInt() ?: continue) - positionOffset
                    val id = (nbt["id"]?.toResourceLocation() ?: continue).fixBlockEntity()
                    if (nbt.size <= 4) continue // no additional data
                    val type = session.registries.blockEntityType[id] ?: continue

                    entities[InChunkPosition(position.x, position.y, position.z)] = nbt
                }
            }

            else -> {
                for (i in 0 until count) {
                    val xz = readUnsignedByte()
                    val y = readShort()
                    val type = session.registries.blockEntityType.getOrNull(readVarInt())
                    val nbt = readNBT()?.asJsonObject() ?: continue
                    if (nbt.isEmpty()) continue
                    if (type == null) continue

                    entities[InChunkPosition(xz shr 4, y.toInt(), xz and 0x0F)] = nbt
                }
            }
        }
        if (entities.isEmpty()) return null

        return entities
    }


    fun PlayInByteBuffer.readBiomeArray(): Array<Biome> {
        val length = when {
            versionId >= ProtocolVersions.V_20W28A -> readVarInt()
            versionId >= V_19W36A -> SpatialBiomeArray.SIZE
            else -> Broken("")
        }

        check(length <= this.size) { "Trying to allocate too much memory" }

        val biomes: Array<Biome?> = arrayOfNulls(length)
        for (index in biomes.indices) {
            val id: Int = if (versionId >= ProtocolVersions.V_20W28A) readVarInt() else readInt()
            biomes[index] = session.registries.biome[id]
        }
        return biomes.cast()
    }

    private fun ChunkReadingData.readChunkData() {
        val chunkData = if (readingData.buffer.versionId < V_21W37A) ChunkPacketUtil.readChunkPacket(buffer, dimension, sectionBitMask!!, null, action == ChunkAction.CREATE, dimension.skyLight) else ChunkPacketUtil.readPaletteChunk(buffer, dimension, null, complete = true, skylight = false)
        ALLOCATOR.free(data)
        if (chunkData == null) {
            action = ChunkAction.UNLOAD
        } else {
            this@ChunkS2CP.prototype.update(chunkData)
        }
    }

    fun parse() {
        this.readingData.readChunkData()
    }

    override fun handle(session: PlaySession) {
        handleChunk(session)
        session.util.chunkReceiver.onChunk()
    }

    private fun handleChunk(session: PlaySession) {
        if (action == ChunkAction.UNLOAD) {
            session.world.chunks -= position
            return
        }
        parse()
        session.world.chunks.set(position, prototype, action == ChunkAction.CREATE)
    }

    override fun log(reducedLog: Boolean) {
        if (reducedLog) {
            return
        }
        Log.log(LogMessageType.NETWORK_IN, level = LogLevels.VERBOSE) { "Chunk (position=$position)" }
    }

    private class ChunkReadingData(
        val data: ByteArray,
        val buffer: PlayInByteBuffer,
        val dimension: DimensionProperties,
        val sectionBitMask: BitSet?,
    )

    companion object {
        val ALLOCATOR = ByteAllocator()
    }
}
