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

package de.bixilon.minosoft.data.registries.blocks.types.pixlyzer.entity

import de.bixilon.kutil.cast.CastUtil.unsafeNull
import de.bixilon.kutil.reflection.ReflectionUtil.field
import de.bixilon.kutil.reflection.ReflectionUtil.jvmField
import de.bixilon.minosoft.data.entities.block.BlockEntity
import de.bixilon.minosoft.data.registries.blocks.entites.BlockEntityType
import de.bixilon.minosoft.data.registries.blocks.types.entity.BlockWithEntity
import de.bixilon.minosoft.data.registries.blocks.types.pixlyzer.PixLyzerBlock
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.registries.registries.Registries
import de.bixilon.minosoft.data.registries.registries.registry.RegistryItem
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection

abstract class PixLyzerBlockWithEntity<T : BlockEntity>(resourceLocation: ResourceLocation, registries: Registries, data: Map<String, Any>) : PixLyzerBlock(resourceLocation, registries, data), BlockWithEntity<BlockEntity> {
    private val blockEntity: BlockEntityType<BlockEntity>? = unsafeNull()

    init {
        FACTORY_FIELD.inject<RegistryItem>(this)
    }


    override fun createBlockEntity(connection: PlayConnection) = blockEntity?.factory?.build(connection)

    private companion object {
        val FACTORY_FIELD = PixLyzerBlockWithEntity<*>::blockEntity.jvmField.field
    }
}
