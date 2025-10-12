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

package de.bixilon.minosoft.data.registries.blocks.types.fluid.water

import de.bixilon.kmath.vec.vec3.d.Vec3d
import de.bixilon.kutil.cast.CastUtil.unsafeNull
import de.bixilon.kutil.exception.Broken
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.entities.entities.Entity
import de.bixilon.minosoft.data.registries.blocks.factory.BlockFactory
import de.bixilon.minosoft.data.registries.blocks.handler.entity.EntityCollisionHandler
import de.bixilon.minosoft.data.registries.blocks.properties.BlockProperties
import de.bixilon.minosoft.data.registries.blocks.settings.BlockSettings
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.blocks.types.Block
import de.bixilon.minosoft.data.registries.blocks.types.fluid.FluidBlock
import de.bixilon.minosoft.data.registries.blocks.types.fluid.FluidFilled
import de.bixilon.minosoft.data.registries.blocks.types.properties.LightedBlock
import de.bixilon.minosoft.data.registries.blocks.types.properties.physics.VelocityBlock
import de.bixilon.minosoft.data.registries.blocks.types.properties.rendering.RandomDisplayTickable
import de.bixilon.minosoft.data.registries.fluid.fluids.WaterFluid
import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.registries.registries.Registries
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.physics.entities.EntityPhysics
import de.bixilon.minosoft.protocol.network.session.play.PlaySession
import java.util.*

class BubbleColumnBlock(identifier: ResourceLocation = Companion.identifier, settings: BlockSettings) : Block(identifier, settings), VelocityBlock, EntityCollisionHandler, FluidFilled, LightedBlock, RandomDisplayTickable {
    override val hardness: Float get() = Broken("Fluid!")
    override val fluid: WaterFluid = unsafeNull()
    override val velocity: Float get() = 1.0f

    init {
        this::fluid.inject(WaterFluid)
    }

    override fun onEntityCollision(entity: Entity, physics: EntityPhysics<*>, position: BlockPosition, state: BlockState) {
        val up = entity.session.world[position + Directions.UP]
        val drag = state.hasDrag()
        if (up == null) {
            surfaceCollision(physics, drag)
        } else {
            onCollision(physics, drag)
        }
    }

    private fun onCollision(physics: EntityPhysics<*>, drag: Boolean) {
        val velocity = physics.velocity
        physics.velocity.y = if (drag) maxOf(-0.3, velocity.y - 0.03) else minOf(0.7, velocity.y + 0.06)
        physics.fallDistance = 0.0f
    }

    private fun surfaceCollision(physics: EntityPhysics<*>, drag: Boolean) {
        val velocity = physics.velocity
        physics.velocity.y = if (drag) maxOf(-0.9, velocity.y - 0.03) else minOf(1.8, velocity.y + 0.1)
    }


    override fun getLightProperties(blockState: BlockState) = FluidBlock.LIGHT_PROPERTIES

    override fun randomDisplayTick(session: PlaySession, state: BlockState, position: BlockPosition, random: Random) {
        fluid.randomTick(session, state, position, random)
    }

    companion object : BlockFactory<BubbleColumnBlock> {
        override val identifier = minecraft("bubble_column")

        override fun build(registries: Registries, settings: BlockSettings) = BubbleColumnBlock(settings = settings)


        private fun BlockState.hasDrag(): Boolean {
            return this[BlockProperties.BUBBLE_COLUMN_DRAG]
        }
    }
}
