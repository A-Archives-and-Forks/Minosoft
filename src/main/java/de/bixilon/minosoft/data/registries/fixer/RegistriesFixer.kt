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

package de.bixilon.minosoft.data.registries.fixer

import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.minosoft.data.entities.block.FlowerPotBlockEntity
import de.bixilon.minosoft.data.entities.entities.player.PlayerEntity
import de.bixilon.minosoft.data.registries.blocks.entites.BlockEntityType
import de.bixilon.minosoft.data.registries.effects.attributes.AttributeType
import de.bixilon.minosoft.data.registries.effects.attributes.MinecraftAttributes
import de.bixilon.minosoft.data.registries.registries.Registries
import de.bixilon.minosoft.modding.event.events.loading.RegistriesLoadEvent
import de.bixilon.minosoft.modding.event.listener.CallbackEventListener.Companion.listen
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.versions.Version

object RegistriesFixer {


    fun register(connection: PlayConnection) {
        connection.events.listen<RegistriesLoadEvent> {
            if (it.state != RegistriesLoadEvent.States.POST) {
                return@listen
            }
            it.registries.fixBlockEntities(it.connection.version)
            it.registries.fixPlayerSpeed()
        }
    }

    private fun Registries.fixFlowerPot(version: Version) {
        // add minecraft:flower_pot as block entity, even if it's not a real entity, but we need it for setting the flower type (in earlier versions of the game)
        if (version.flattened) return
        val flowerPot = block[FlowerPotBlockEntity] ?: return
        blockEntityType[FlowerPotBlockEntity] = BlockEntityType(FlowerPotBlockEntity.identifier, setOf(flowerPot), FlowerPotBlockEntity)
    }

    private fun Registries.fixPlayerSpeed() {
        // old pixlyzer generated all attributes mostly wrong
        entityType[PlayerEntity]?.attributes?.unsafeCast<MutableMap<AttributeType, Double>>()?.put(MinecraftAttributes.MOVEMENT_SPEED, 0.1f.toDouble())
    }

    private fun Registries.fixBlockEntities(version: Version) {
        this.fixFlowerPot(version)
    }
}
