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

package de.bixilon.minosoft.data.registries.blocks.types.pixlyzer.wall

import de.bixilon.kmath.vec.vec3.d.MVec3d
import de.bixilon.kmath.vec.vec3.d.Vec3d
import de.bixilon.kutil.random.RandomUtil.chance
import de.bixilon.minosoft.camera.target.targets.BlockTarget
import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.data.entities.entities.player.Hands
import de.bixilon.minosoft.data.registries.blocks.factory.PixLyzerBlockFactory
import de.bixilon.minosoft.data.registries.blocks.properties.BlockProperties
import de.bixilon.minosoft.data.registries.blocks.properties.BlockProperties.getFacing
import de.bixilon.minosoft.data.registries.blocks.properties.BlockProperties.isPowered
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.blocks.types.properties.InteractBlockHandler
import de.bixilon.minosoft.data.registries.blocks.types.properties.rendering.RandomDisplayTickable
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.registries.particle.data.DustParticleData
import de.bixilon.minosoft.data.registries.registries.Registries
import de.bixilon.minosoft.data.text.formatting.color.Colors
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.gui.rendering.particle.types.render.texture.simple.dust.DustParticle
import de.bixilon.minosoft.input.interaction.InteractionResults
import de.bixilon.minosoft.protocol.network.session.play.PlaySession
import java.util.*

open class LeverBlock(resourceLocation: ResourceLocation, registries: Registries, data: Map<String, Any>) : WallMountedBlock(resourceLocation, registries, data), InteractBlockHandler, RandomDisplayTickable {
    private val dustParticleType = registries.particleType[DustParticle]

    private fun spawnParticles(session: PlaySession, blockState: BlockState, blockPosition: BlockPosition, scale: Float) {
        val particle = session.world.particle ?: return
        if (dustParticleType == null) return
        val direction = blockState.getFacing().inverted
        val mountDirection = getRealFacing(blockState)

        val position = (Vec3d(blockPosition) + 0.5).plus((direction.vectord * 0.1) + (mountDirection.vectord * 0.2))

        particle += DustParticle(session, position, MVec3d.EMPTY, DustParticleData(Colors.TRUE_RED.rgba(), scale, dustParticleType))
    }

    override fun randomDisplayTick(session: PlaySession, state: BlockState, position: BlockPosition, random: Random) {
        if (!state.isPowered()) {
            return
        }
        if (random.chance(25)) {
            spawnParticles(session, state, position, 0.5f)
        }
    }

    override fun interact(session: PlaySession, target: BlockTarget, hand: Hands, stack: ItemStack?): InteractionResults {
        val nextState = target.state.cycle(BlockProperties.POWERED)
        session.world[target.blockPosition] = nextState
        spawnParticles(session, nextState, target.blockPosition, 1.0f)

        return InteractionResults.SUCCESS
    }

    companion object : PixLyzerBlockFactory<LeverBlock> {
        override fun build(resourceLocation: ResourceLocation, registries: Registries, data: Map<String, Any>): LeverBlock {
            return LeverBlock(resourceLocation, registries, data)
        }
    }
}
