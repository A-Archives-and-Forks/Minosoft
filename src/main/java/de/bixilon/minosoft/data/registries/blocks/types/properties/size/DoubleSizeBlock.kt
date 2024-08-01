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

package de.bixilon.minosoft.data.registries.blocks.types.properties.size

import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.entities.block.BlockEntity
import de.bixilon.minosoft.data.registries.blocks.properties.BlockProperties
import de.bixilon.minosoft.data.registries.blocks.properties.Halves
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.blocks.state.PropertyBlockState
import de.bixilon.minosoft.gui.rendering.util.VecUtil.plus
import de.bixilon.minosoft.protocol.network.session.play.PlaySession

interface DoubleSizeBlock : MultiSizeBlock {


    fun isTop(state: BlockState, session: PlaySession) = state.unsafeCast<PropertyBlockState>().properties[BlockProperties.STAIR_HALF] == Halves.UPPER
    fun getTop(state: BlockState, session: PlaySession): BlockState = state.withProperties(BlockProperties.STAIR_HALF to Halves.UPPER)
    fun getBottom(state: BlockState, session: PlaySession): BlockState = state.withProperties(BlockProperties.STAIR_HALF to Halves.LOWER)

    override fun onBreak(session: PlaySession, position: Vec3i, state: BlockState, entity: BlockEntity?) {
        val offset = if (isTop(state, session)) Directions.DOWN else Directions.UP
        session.world[position + offset] = null
    }

    override fun onPlace(session: PlaySession, position: Vec3i, state: BlockState, entity: BlockEntity?) {

        val top = isTop(state, session)

        val offset = if (top) Directions.DOWN else Directions.UP
        val otherState = if (top) getTop(state, session) else getBottom(state, session)

        session.world[position + offset] = otherState
    }
}
