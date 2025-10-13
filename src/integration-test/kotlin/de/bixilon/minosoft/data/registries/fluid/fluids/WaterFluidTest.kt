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

package de.bixilon.minosoft.data.registries.fluid.fluids

import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.kutil.cast.CastUtil.unsafeNull
import de.bixilon.minosoft.data.registries.blocks.MinecraftBlocks
import de.bixilon.minosoft.data.registries.blocks.properties.BlockProperties
import de.bixilon.minosoft.data.registries.blocks.types.building.stone.StoneBlock
import de.bixilon.minosoft.data.registries.blocks.types.fluid.FluidBlock
import de.bixilon.minosoft.data.registries.blocks.types.fluid.water.BubbleColumnBlock
import de.bixilon.minosoft.test.IT
import org.testng.Assert.assertEquals
import org.testng.Assert.assertFalse
import org.testng.annotations.Test

@Test(groups = ["fluid"])
class WaterFluidTest {
    private var block: FluidBlock = unsafeNull()
    private var water: WaterFluid = unsafeNull()
    private var lava: LavaFluid = unsafeNull()

    @Test(priority = -1)
    fun getWater() {
        this.block = IT.REGISTRIES.block[WaterFluid].unsafeCast()
        this.water = IT.REGISTRIES.fluid[WaterFluid].unsafeCast()
    }

    fun heightLevel0() {
        val state = block.states.withProperties(FluidBlock.LEVEL to 0)
        assertEquals(this.water.getHeight(state), 0.8888889f)
    }

    fun heightLevel2() {
        val state = block.states.withProperties(FluidBlock.LEVEL to 2)
        assertEquals(this.water.getHeight(state), 0.6666667f)
    }

    fun heightLevel8() {
        val state = block.states.withProperties(FluidBlock.LEVEL to 8)
        assertEquals(this.water.getHeight(state), 0.8888889f)
    }

    fun heightLevel12() {
        val state = block.states.withProperties(FluidBlock.LEVEL to 12)
        assertEquals(this.water.getHeight(state), 0.8888889f)
    }

    fun waterlogged() {
        val state = IT.REGISTRIES.block[MinecraftBlocks.OAK_SLAB]!!.states.withProperties(BlockProperties.WATERLOGGED to true)
        assertEquals(this.water.getHeight(state), 0.8888889f)
    }

    fun kelp() {
        val state = IT.REGISTRIES.block[MinecraftBlocks.KELP]!!.states.default
        assertEquals(this.water.getHeight(state), 0.8888889f)
    }

    fun bubbleColumn() {
        val state = IT.REGISTRIES.block[BubbleColumnBlock]!!.states.default
        assertEquals(this.water.getHeight(state), 0.8888889f)
    }

    fun stone() {
        val state = IT.REGISTRIES.block[StoneBlock.Block]!!.states.default
        assertEquals(this.water.getHeight(state), 0.0f)
    }

    fun `no match with lava`() {
        assertFalse(water.matches(lava))
    }
}
