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

package de.bixilon.minosoft.data.world.container.palette.data.array

import de.bixilon.kutil.memory.allocator.LongAllocator
import de.bixilon.minosoft.data.world.container.palette.data.PaletteData
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_1_16
import de.bixilon.minosoft.protocol.protocol.buffers.play.PlayInByteBuffer

class ArrayPaletteData(
    val versionId: Int,
    val elementBits: Int,
    override val size: Int,
) : PaletteData {
    private val singleValueMask = (1 shl elementBits) - 1
    private val valuesPerLong = Long.SIZE_BITS / elementBits
    private lateinit var data: LongArray

    override var isEmpty = true

    init {
        check(elementBits in 0..32)
    }

    private fun calculateLongs(): Int {
        if (versionId < LONG_BIT_SPLITTING_VERSION) {
            val bits = this.size * elementBits

            return (bits + (Long.SIZE_BITS - 1)) / Long.SIZE_BITS // divide up
        }
        return (this.size + valuesPerLong - 1) / valuesPerLong
    }

    private fun checkEmpty(size: Int): Boolean {
        for (i in 0 until size) {
            if (this.data[i] != 0L) {
                return false
            }
        }
        return true
    }

    override fun read(buffer: PlayInByteBuffer) {
        val packetSize = buffer.readVarInt()
        val longs = calculateLongs()
        this.data = ALLOCATOR.allocate(longs)
        if (packetSize != longs) {
            buffer.pointer += packetSize * Long.SIZE_BYTES // data is ignored
            return
        }
        buffer.readLongArray(this.data, longs)
        this.isEmpty = checkEmpty(longs)
    }

    override operator fun get(index: Int): Int {
        val blockId: Long = if (versionId < LONG_BIT_SPLITTING_VERSION) {
            val startLong = index * elementBits / Long.SIZE_BITS
            val startOffset = index * elementBits % Long.SIZE_BITS
            val endLong = ((index + 1) * elementBits - 1) / Long.SIZE_BITS

            if (startLong == endLong) {
                data[startLong] ushr startOffset
            } else {
                val endOffset = Long.SIZE_BITS - startOffset
                data[startLong] ushr startOffset or (data[endLong] shl endOffset)
            }
        } else {
            val startLong = index / valuesPerLong
            val startOffset = index % valuesPerLong * elementBits
            data[startLong] ushr startOffset
        }

        return blockId.toInt() and singleValueMask
    }

    override fun free() {
        ALLOCATOR.free(data)
    }

    companion object {
        private val ALLOCATOR = LongAllocator()
        const val LONG_BIT_SPLITTING_VERSION = V_1_16 // ToDo: When did this changed? is just a guess
    }
}
