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
package de.bixilon.minosoft.data.registries.fluid.fluids

import de.bixilon.minosoft.data.registries.fluid.Fluid
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.registries.registries.Registries
import de.bixilon.minosoft.data.registries.registries.registry.codec.IdentifierCodec
import de.bixilon.minosoft.protocol.network.session.play.PlaySession

open class PixLyzerFluid(resourceLocation: ResourceLocation) : Fluid(resourceLocation) {

    override fun getVelocityMultiplier(session: PlaySession): Double = 1.0

    companion object : IdentifierCodec<Fluid> {

        override fun deserialize(registries: Registries?, identifier: ResourceLocation, data: Map<String, Any>): PixLyzerFluid {
            return PixLyzerFluid(identifier)
        }
    }
}
