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
package de.bixilon.minosoft.data.registries.particle.data

import de.bixilon.minosoft.data.registries.particle.ParticleType
import de.bixilon.minosoft.data.text.formatting.color.RGBAColor
import de.bixilon.minosoft.protocol.protocol.buffers.play.PlayInByteBuffer

class DustParticleData(val color: RGBAColor, val scale: Float, type: ParticleType) : ParticleData(type) {

    override fun toString(): String {
        return "$type (scale=$scale, color=$color)"
    }

    companion object : ParticleDataFactory<DustParticleData> {
        override fun read(buffer: PlayInByteBuffer, type: ParticleType): DustParticleData {
            return DustParticleData(RGBAColor(buffer.readFloat(), buffer.readFloat(), buffer.readFloat()), buffer.readFloat(), type)
        }
    }
}
