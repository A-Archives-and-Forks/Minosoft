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

package de.bixilon.minosoft.data.registries.blocks.state

import de.bixilon.minosoft.data.registries.blocks.light.LightProperties
import de.bixilon.minosoft.data.registries.blocks.properties.BlockProperty
import de.bixilon.minosoft.data.registries.blocks.state.builder.BlockStateSettings
import de.bixilon.minosoft.data.registries.blocks.types.Block
import de.bixilon.minosoft.data.registries.shapes.voxel.AbstractVoxelShape

open class AdvancedBlockState(
    block: Block,
    properties: Map<BlockProperty<*>, Any>,
    luminance: Int,
    val collisionShape: AbstractVoxelShape?,
    val outlineShape: AbstractVoxelShape?,
    val lightProperties: LightProperties,
) : PropertyBlockState(block, properties, luminance) {

    constructor(block: Block, settings: BlockStateSettings) : this(block, settings.properties ?: emptyMap(), settings.luminance, settings.collisionShape, settings.outlineShape, settings.lightProperties) {
        if (settings.solidRenderer) {
            flags += BlockStateFlags.FULLY_OPAQUE
        }
    }
}
