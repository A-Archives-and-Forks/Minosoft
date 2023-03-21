/*
 * Minosoft
 * Copyright (C) 2020-2023 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.data.registries.blocks.types.fluid

import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.kutil.exception.Broken
import de.bixilon.minosoft.data.registries.blocks.light.CustomLightProperties
import de.bixilon.minosoft.data.registries.blocks.settings.BlockSettings
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.blocks.types.Block
import de.bixilon.minosoft.data.registries.blocks.types.legacy.CustomBlockModel
import de.bixilon.minosoft.data.registries.blocks.types.properties.LightedBlock
import de.bixilon.minosoft.data.registries.blocks.types.properties.shape.outline.OutlinedBlock
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.registries.shapes.aabb.AABB
import de.bixilon.minosoft.data.registries.shapes.voxel.VoxelShape
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3Util.EMPTY
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import java.util.*

abstract class FluidBlock(identifier: ResourceLocation, settings: BlockSettings) : Block(identifier, settings), FluidHolder, OutlinedBlock, LightedBlock, CustomBlockModel {
    override val hardness: Float get() = Broken("Fluid is kind of unbreakable?")
    override val modelName: ResourceLocation? get() = null

    override fun getOutlineShape(connection: PlayConnection, blockState: BlockState): VoxelShape {
        return VoxelShape(AABB(Vec3.EMPTY, Vec3(1.0f, fluid.getHeight(blockState), 1.0f)))
    }

    override fun getLightProperties(blockState: BlockState) = LIGHT_PROPERTIES

    override fun randomTick(connection: PlayConnection, blockState: BlockState, blockPosition: Vec3i, random: Random) {
        fluid.randomTick(connection, blockState, blockPosition, random)
    }

    companion object {
        val LIGHT_PROPERTIES = CustomLightProperties(true, false, true)
    }
}
