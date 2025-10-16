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

package de.bixilon.minosoft.protocol.packets.s2c.play.map.legacy

import de.bixilon.kmath.vec.vec2.i.Vec2i
import de.bixilon.minosoft.data.world.map.MapPin
import de.bixilon.minosoft.data.world.map.MapPinTypes
import de.bixilon.minosoft.protocol.protocol.buffers.play.PlayInByteBuffer
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType

class PinsLegacyMapS2CP(
    val id: Int,
    buffer: PlayInByteBuffer,
) : LegacyMapS2CP {
    val pins: Map<Vec2i, MapPin>

    init {
        val pins: MutableMap<Vec2i, MapPin> = mutableMapOf()
        for (i in 0 until buffer.size / 3) {
            val rawDirection = buffer.readUnsignedByte()
            val position = Vec2i(buffer.readByte().toInt(), buffer.readByte().toInt())

            val direction = rawDirection shr 4
            val type = MapPinTypes[rawDirection and 0x0F]

            pins[position] = MapPin(direction, type)
        }


        this.pins = pins
    }

    override fun log(reducedLog: Boolean) {
        if (reducedLog) {
            return
        }
        Log.log(LogMessageType.NETWORK_IN, LogLevels.VERBOSE) { "Pins legacy map (pins=$pins)" }
    }
}
