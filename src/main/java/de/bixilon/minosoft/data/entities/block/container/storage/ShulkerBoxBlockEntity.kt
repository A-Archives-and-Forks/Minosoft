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

package de.bixilon.minosoft.data.entities.block.container.storage

import de.bixilon.minosoft.data.entities.block.BlockEntityFactory
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.blocks.types.entity.storage.ShulkerBoxBlock
import de.bixilon.minosoft.data.registries.blocks.types.properties.DyedBlock
import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.chunk.entities.renderer.RenderedBlockEntity
import de.bixilon.minosoft.gui.rendering.chunk.entities.renderer.storage.shulker.ShulkerBoxRenderer
import de.bixilon.minosoft.protocol.network.session.play.PlaySession

class ShulkerBoxBlockEntity(session: PlaySession) : StorageBlockEntity(session), RenderedBlockEntity<ShulkerBoxRenderer> {
    override var renderer: ShulkerBoxRenderer? = null

    override fun createRenderer(context: RenderContext, state: BlockState, position: BlockPosition, light: Int): ShulkerBoxRenderer? {
        if (state.block !is ShulkerBoxBlock) return null
        val name = when {
            state.block is DyedBlock -> ShulkerBoxRenderer.NAME_COLOR[state.block.color.ordinal]
            else -> ShulkerBoxRenderer.NAME
        }
        val model = context.models.skeletal[name] ?: return null
        return ShulkerBoxRenderer(this, context, state, position, model, light)
    }

    override fun onOpen() {
        super.onOpen()
        renderer?.open()
    }

    override fun onClose() {
        super.onClose()
        renderer?.close()
    }

    companion object : BlockEntityFactory<ShulkerBoxBlockEntity> {
        override val identifier: ResourceLocation = minecraft("shulker_box")

        override fun build(session: PlaySession): ShulkerBoxBlockEntity {
            return ShulkerBoxBlockEntity(session)
        }
    }
}
