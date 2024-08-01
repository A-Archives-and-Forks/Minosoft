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
package de.bixilon.minosoft.data.entities.entities.animal

import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.kutil.enums.EnumUtil
import de.bixilon.kutil.enums.ValuesEnum
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.entities.data.EntityData
import de.bixilon.minosoft.data.entities.data.EntityDataField
import de.bixilon.minosoft.data.entities.entities.SynchronizedEntityData
import de.bixilon.minosoft.data.registries.entities.EntityFactory
import de.bixilon.minosoft.data.registries.entities.EntityType
import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.protocol.network.session.play.PlaySession

class Parrot(session: PlaySession, entityType: EntityType, data: EntityData, position: Vec3d, rotation: EntityRotation) : ShoulderRidingAnimal(session, entityType, data, position, rotation) {

    @get:SynchronizedEntityData
    val variant: ParrotVariants
        get() = ParrotVariants.VALUES.getOrNull(data.get(VARIANT_DATA, ParrotVariants.RED_BLUE.ordinal)) ?: ParrotVariants.RED_BLUE

    enum class ParrotVariants {
        RED_BLUE,
        BLUE,
        GREEN,
        YELLOW_BLUE,
        GREY,
        ;

        companion object : ValuesEnum<ParrotVariants> {
            override val VALUES: Array<ParrotVariants> = values()
            override val NAME_MAP: Map<String, ParrotVariants> = EnumUtil.getEnumValues(VALUES)
        }
    }

    companion object : EntityFactory<Parrot> {
        override val identifier: ResourceLocation = minecraft("parrot")
        private val VARIANT_DATA = EntityDataField("PARROT_VARIANT")

        override fun build(session: PlaySession, entityType: EntityType, data: EntityData, position: Vec3d, rotation: EntityRotation): Parrot {
            return Parrot(session, entityType, data, position, rotation)
        }
    }
}
