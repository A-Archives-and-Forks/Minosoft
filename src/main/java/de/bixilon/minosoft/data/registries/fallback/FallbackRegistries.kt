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

package de.bixilon.minosoft.data.registries.fallback

import de.bixilon.kutil.json.JsonUtil.asJsonObject
import de.bixilon.minosoft.assets.IntegratedAssets
import de.bixilon.minosoft.assets.util.InputStreamUtil.readJson
import de.bixilon.minosoft.data.container.equipment.EquipmentSlots
import de.bixilon.minosoft.data.entities.EntityAnimations
import de.bixilon.minosoft.data.entities.EntityObjectType
import de.bixilon.minosoft.data.entities.block.BlockDataDataType
import de.bixilon.minosoft.data.entities.data.types.EntityDataTypes
import de.bixilon.minosoft.data.registries.PluginChannel
import de.bixilon.minosoft.data.registries.chat.ChatMessageType
import de.bixilon.minosoft.data.registries.containers.ContainerType
import de.bixilon.minosoft.data.registries.entities.variants.CatVariant
import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.data.registries.identified.Namespaces.minosoft
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.registries.registries.registry.PerVersionEnumRegistry
import de.bixilon.minosoft.data.registries.registries.registry.PerVersionRegistry
import de.bixilon.minosoft.data.registries.registries.registry.Registry
import de.bixilon.minosoft.data.registries.registries.registry.ResourceLocationRegistry
import de.bixilon.minosoft.protocol.packets.c2s.play.entity.EntityActionC2SP
import de.bixilon.minosoft.protocol.packets.s2c.play.title.TitleS2CF
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType

object FallbackRegistries {
    private val ENUM_RESOURCE_LOCATION = minosoft("mapping/enums.json")
    private val REGISTRIES_RESOURCE_LOCATION = minosoft("mapping/default_registries.json")
    private var initialized = false

    val EQUIPMENT_SLOTS_REGISTRY = PerVersionEnumRegistry(EquipmentSlots)
    val HAND_EQUIPMENT_SLOTS_REGISTRY = PerVersionEnumRegistry(EquipmentSlots)
    val ARMOR_EQUIPMENT_SLOTS_REGISTRY = PerVersionEnumRegistry(EquipmentSlots)
    val ARMOR_STAND_EQUIPMENT_SLOTS_REGISTRY = PerVersionEnumRegistry(EquipmentSlots)

    val ENTITY_DATA_TYPES_REGISTRY = PerVersionEnumRegistry(EntityDataTypes)

    val TITLE_ACTIONS_REGISTRY = PerVersionEnumRegistry(TitleS2CF.TitleActions)

    val ENTITY_ANIMATION_REGISTRY = PerVersionEnumRegistry(EntityAnimations)
    val ENTITY_ACTIONS_REGISTRY = PerVersionEnumRegistry(EntityActionC2SP.EntityActions)

    val ENTITY_OBJECT_REGISTRY = PerVersionRegistry { Registry(codec = EntityObjectType) }

    val BLOCK_DATA_TYPE_REGISTRY: PerVersionRegistry<BlockDataDataType, Registry<BlockDataDataType>> = PerVersionRegistry { Registry(codec = BlockDataDataType) }

    val DEFAULT_PLUGIN_CHANNELS_REGISTRY: PerVersionRegistry<PluginChannel, Registry<PluginChannel>> = PerVersionRegistry { Registry(codec = PluginChannel) }

    val CONTAINER_TYPE_REGISTRY: PerVersionRegistry<ContainerType, Registry<ContainerType>> = PerVersionRegistry { Registry(codec = ContainerType) }

    val GAME_EVENT_REGISTRY: PerVersionRegistry<ResourceLocation, ResourceLocationRegistry> = PerVersionRegistry { ResourceLocationRegistry() }
    val WORLD_EVENT_REGISTRY: PerVersionRegistry<ResourceLocation, ResourceLocationRegistry> = PerVersionRegistry { ResourceLocationRegistry() }

    val CAT_VARIANT_REGISTRY: PerVersionRegistry<CatVariant, Registry<CatVariant>> = PerVersionRegistry { Registry(codec = CatVariant) }


    val MESSAGE_TYPES_REGISTRY: PerVersionRegistry<ChatMessageType, Registry<ChatMessageType>> = PerVersionRegistry { Registry(codec = ChatMessageType) }
    val VIBRATION_SOURCE: PerVersionRegistry<ResourceLocation, ResourceLocationRegistry> = PerVersionRegistry { ResourceLocationRegistry() }


    fun load() {
        check(!initialized) { "Already initialized!" }
        Log.log(LogMessageType.OTHER, LogLevels.VERBOSE) { "Loading default registries..." }

        val enumJson: Map<ResourceLocation, Any> = IntegratedAssets.DEFAULT[ENUM_RESOURCE_LOCATION].readJson()

        EQUIPMENT_SLOTS_REGISTRY.initialize(enumJson[ResourceLocation.of("equipment_slots")].asJsonObject())
        HAND_EQUIPMENT_SLOTS_REGISTRY.initialize(enumJson[ResourceLocation.of("hand_equipment_slots")].asJsonObject())
        ARMOR_EQUIPMENT_SLOTS_REGISTRY.initialize(enumJson[ResourceLocation.of("armor_equipment_slots")].asJsonObject())
        ARMOR_STAND_EQUIPMENT_SLOTS_REGISTRY.initialize(enumJson[ResourceLocation.of("armor_stand_equipment_slots")].asJsonObject())

        ENTITY_DATA_TYPES_REGISTRY.initialize(enumJson[ResourceLocation.of("entity_data_data_types")].asJsonObject()) // ToDo

        TITLE_ACTIONS_REGISTRY.initialize(enumJson[ResourceLocation.of("title_actions")].asJsonObject())

        ENTITY_ANIMATION_REGISTRY.initialize(enumJson[ResourceLocation.of("entity_animations")].asJsonObject())
        ENTITY_ACTIONS_REGISTRY.initialize(enumJson[ResourceLocation.of("entity_actions")].asJsonObject())


        val registriesJson: Map<ResourceLocation, Any> = IntegratedAssets.DEFAULT[REGISTRIES_RESOURCE_LOCATION].readJson()

        DEFAULT_PLUGIN_CHANNELS_REGISTRY.initialize(registriesJson[ResourceLocation.of("default_channels")].asJsonObject())

        ENTITY_OBJECT_REGISTRY.initialize(registriesJson[ResourceLocation.of("entity_objects")].asJsonObject())

        BLOCK_DATA_TYPE_REGISTRY.initialize(registriesJson[ResourceLocation.of("block_data_data_types")].asJsonObject())

        CONTAINER_TYPE_REGISTRY.initialize(registriesJson[ResourceLocation.of("container_types")].asJsonObject())

        GAME_EVENT_REGISTRY.initialize(registriesJson[ResourceLocation.of("game_events")].asJsonObject())
        WORLD_EVENT_REGISTRY.initialize(registriesJson[ResourceLocation.of("world_events")].asJsonObject())


        CAT_VARIANT_REGISTRY.initialize(registriesJson[ResourceLocation.of("variants/cat")].asJsonObject())

        MESSAGE_TYPES_REGISTRY.initialize(registriesJson[minecraft("message_types")].asJsonObject())
        VIBRATION_SOURCE.initialize(registriesJson[minecraft("vibration_source")].asJsonObject())

        initialized = true
        Log.log(LogMessageType.OTHER, LogLevels.VERBOSE) { "Loaded default registries!" }
    }
}
