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
package de.bixilon.minosoft.data.entities.entities.decoration.armorstand

import glm_.vec2.Vec2
import glm_.vec3.Vec3
import glm_.vec3.Vec3d
import de.bixilon.kutil.bit.BitByte.isBitMask
import de.bixilon.kutil.observer.DataObserver.Companion.observe
import de.bixilon.kutil.primitive.IntUtil.toInt
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.entities.data.EntityData
import de.bixilon.minosoft.data.entities.data.EntityDataField
import de.bixilon.minosoft.data.entities.entities.LivingEntity
import de.bixilon.minosoft.data.entities.entities.SynchronizedEntityData
import de.bixilon.minosoft.data.registries.entities.EntityFactory
import de.bixilon.minosoft.data.registries.entities.EntityType
import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.registries.shapes.aabb.AABB
import de.bixilon.minosoft.data.text.formatting.color.RGBAColor
import de.bixilon.minosoft.protocol.network.session.play.PlaySession

class ArmorStand(session: PlaySession, entityType: EntityType, data: EntityData, position: Vec3d, rotation: EntityRotation) : LivingEntity(session, entityType, data, position, rotation) {
    private var flags: Int by data(FLAGS_DATA, 0x00) { it.toInt() }

    private fun updateFlags() {
        this.dimensions = when {
            isMarker -> DIMENSIONS_MARKER
            isSmall -> DIMENSIONS_SMALL
            else -> DIMENSIONS
        }
        this.defaultAABB = createDefaultAABB()
    }


    private fun getArmorStandFlag(bitMask: Int): Boolean {
        return flags.isBitMask(bitMask)
    }

    override val canRaycast: Boolean get() = !isMarker && super.canRaycast
    override val hitboxColor: RGBAColor? get() = if (isMarker) null else super.hitboxColor
    override var defaultAABB: AABB = AABB.EMPTY
    override var dimensions: Vec2 = DIMENSIONS
        private set

    @get:SynchronizedEntityData
    val isSmall: Boolean
        get() = getArmorStandFlag(0x01)

    @get:SynchronizedEntityData
    val hasArms: Boolean
        get() = getArmorStandFlag(0x04)

    @get:SynchronizedEntityData
    val hasNoBasePlate: Boolean
        get() = getArmorStandFlag(0x08)

    @get:SynchronizedEntityData
    val isMarker: Boolean
        get() = getArmorStandFlag(0x10)

    @get:SynchronizedEntityData
    val headRotation: Vec3 by data(HEAD_ROTATION_DATA, HEAD_ROTATION)

    @get:SynchronizedEntityData
    val bodyRotation: Vec3 by data(BODY_ROTATION_DATA, BODY_ROTATION)

    @get:SynchronizedEntityData
    val leftArmRotation: Vec3 by data(LEFT_ARM_ROTATION_DATA, LEFT_ARM_ROTATION)

    @get:SynchronizedEntityData
    val rightArmRotation: Vec3 by data(RIGHT_ARM_ROTATION_DATA, RIGHT_ARM_ROTATION)

    @get:SynchronizedEntityData
    val leftLegRotation: Vec3 by data(LEFT_LEG_ROTATION_DATA, LEFT_LEG_ROTATION)

    @get:SynchronizedEntityData
    val rightLegRotation: Vec3 by data(RIGHT_LEG_ROTATION_DATA, RIGHT_LEG_ROTATION)


    override fun tick() {
        if (isMarker && age % 20 != 0) return // tick them really slow to improve performance
        super.tick()
    }

    override fun init() {
        this::flags.observe(this, true) { updateFlags() }
        super.init()
    }


    companion object : EntityFactory<ArmorStand> {
        override val identifier: ResourceLocation = minecraft("armor_stand")
        val FLAGS_DATA = EntityDataField("ARMOR_STAND_FLAGS")
        private val HEAD_ROTATION_DATA = EntityDataField("ARMOR_STAND_HEAD_ROTATION")
        private val BODY_ROTATION_DATA = EntityDataField("ARMOR_STAND_BODY_ROTATION")
        private val LEFT_ARM_ROTATION_DATA = EntityDataField("ARMOR_STAND_LEFT_ARM_ROTATION")
        private val RIGHT_ARM_ROTATION_DATA = EntityDataField("ARMOR_STAND_RIGHT_ARM_ROTATION")
        private val LEFT_LEG_ROTATION_DATA = EntityDataField("ARMOR_STAND_LEFT_LEG_ROTATION", "ARMOR_STAND_LEFT_LAG_ROTATION")
        private val RIGHT_LEG_ROTATION_DATA = EntityDataField("ARMOR_STAND_RIGHT_LEG_ROTATION", "ARMOR_STAND_RIGHT_LAG_ROTATION")

        private val DIMENSIONS = Vec2(0.5f, 1.975f)
        private val DIMENSIONS_MARKER = Vec2(0.0f)
        private val DIMENSIONS_SMALL = DIMENSIONS * 0.5f

        private val HEAD_ROTATION = Vec3(0.0f, 0.0f, 0.0f)
        private val BODY_ROTATION = Vec3(0.0f, 0.0f, 0.0f)
        private val LEFT_ARM_ROTATION = Vec3(-10.0f, 0.0f, -10.0f)
        private val RIGHT_ARM_ROTATION = Vec3(-15.0f, 0.0f, 10.0f)
        private val LEFT_LEG_ROTATION = Vec3(-1.0f, 0.0f, -1.0f)
        private val RIGHT_LEG_ROTATION = Vec3(1.0f, 0.0f, 1.0f)

        override fun build(session: PlaySession, entityType: EntityType, data: EntityData, position: Vec3d, rotation: EntityRotation): ArmorStand {
            return ArmorStand(session, entityType, data, position, rotation)
        }
    }
}
