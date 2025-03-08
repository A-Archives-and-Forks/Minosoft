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

package de.bixilon.minosoft.data.registries.blocks.types.pixlyzer.plant

import de.bixilon.minosoft.camera.target.targets.BlockTarget
import de.bixilon.minosoft.data.registries.blocks.MinecraftBlocks
import de.bixilon.minosoft.data.registries.blocks.factory.PixLyzerBlockFactory
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.blocks.wawla.BlockWawlaProvider
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.registries.registries.Registries
import de.bixilon.minosoft.data.text.BaseComponent
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.data.text.TextComponent
import de.bixilon.minosoft.data.text.formatting.color.ChatColors
import de.bixilon.minosoft.protocol.network.session.play.PlaySession

open class CropBlock(resourceLocation: ResourceLocation, registries: Registries, data: Map<String, Any>) : PlantBlock(resourceLocation, registries, data), BlockWawlaProvider {

    override fun canPlaceOn(blockState: BlockState): Boolean {
        return blockState.block.identifier == MinecraftBlocks.FARMLAND
    }

    override fun getWawlaInformation(session: PlaySession, target: BlockTarget): ChatComponent {
        val light = session.world.getLight(target.blockPosition)

        val component = BaseComponent("Light: ")

        component += TextComponent(light.block).color(if (light.block < MIN_LIGHT_LEVEL) ChatColors.RED else ChatColors.GREEN)

        component += " (${light.sky})"

        return component
    }

    companion object : PixLyzerBlockFactory<CropBlock> {
        const val MIN_LIGHT_LEVEL = 7

        override fun build(resourceLocation: ResourceLocation, registries: Registries, data: Map<String, Any>): CropBlock {
            return CropBlock(resourceLocation, registries, data)
        }
    }
}
