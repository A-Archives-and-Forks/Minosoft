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

package de.bixilon.minosoft.gui.rendering.models.baked

import de.bixilon.kmath.vec.vec3.f.Vec3f
import de.bixilon.kutil.unsafe.UnsafeUtil.setUnsafeAccessible
import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.gui.rendering.models.baked.BakedModelTestUtil.createFaces
import de.bixilon.minosoft.gui.rendering.models.baked.BakedModelTestUtil.createTextureManager
import de.bixilon.minosoft.gui.rendering.models.block.BlockModel
import de.bixilon.minosoft.gui.rendering.models.block.element.ModelElement
import de.bixilon.minosoft.gui.rendering.models.block.state.apply.SingleBlockStateApply
import de.bixilon.minosoft.gui.rendering.models.block.state.apply.WeightedBlockStateApply
import de.bixilon.minosoft.gui.rendering.models.block.state.baked.BakedModel
import de.bixilon.minosoft.gui.rendering.models.block.state.render.WeightedBlockRender
import de.bixilon.minosoft.gui.rendering.textures.TextureUtil.texture
import org.testng.Assert.assertEquals
import org.testng.annotations.Test
import java.util.*

@Test(groups = ["models"])
class WeightedModelTest {

    private val getModel = WeightedBlockRender::class.java.declaredMethods.find { it.name.startsWith("getModel") && it.parameterCount == 2 && it.parameters.first().type == Random::class.java }!!.apply { setUnsafeAccessible() }

    fun `evenly distributed weight`() {
        val model = WeightedBlockStateApply(listOf(
            WeightedBlockStateApply.WeightedApply(1, A),
            WeightedBlockStateApply.WeightedApply(1, B),
            WeightedBlockStateApply.WeightedApply(1, C),
            WeightedBlockStateApply.WeightedApply(1, D),
        ))

        model.load(createTextureManager("block/test"))
        val baked = model.bake()!!

        baked.assert(BlockPosition(29, -61, 12), 1)
        baked.assert(BlockPosition(29, -61, 13), 3)
        baked.assert(BlockPosition(29, -61, 14), 4)
        baked.assert(BlockPosition(29, -61, 15), 3)
        baked.assert(BlockPosition(29, -61, 16), 3)
        baked.assert(BlockPosition(29, -61, 17), 3)

        baked.assert(BlockPosition(30, -61, 12), 3)
        baked.assert(BlockPosition(30, -61, 13), 2)
        baked.assert(BlockPosition(30, -61, 14), 4)
        baked.assert(BlockPosition(30, -61, 15), 3)
        baked.assert(BlockPosition(30, -61, 16), 3)
        baked.assert(BlockPosition(30, -61, 17), 4)

        baked.assert(BlockPosition(31, -61, 12), 3)
        baked.assert(BlockPosition(31, -61, 13), 1)
        baked.assert(BlockPosition(31, -61, 14), 4)
        baked.assert(BlockPosition(31, -61, 15), 1)
        baked.assert(BlockPosition(31, -61, 16), 3)
        baked.assert(BlockPosition(31, -61, 17), 1)

        baked.assert(BlockPosition(32, -61, 12), 1)
        baked.assert(BlockPosition(32, -61, 13), 1)
        baked.assert(BlockPosition(32, -61, 14), 4)
        baked.assert(BlockPosition(32, -61, 15), 4)
        baked.assert(BlockPosition(32, -61, 16), 4)
        baked.assert(BlockPosition(32, -61, 17), 3)

        baked.assert(BlockPosition(33, -61, 12), 3)
        baked.assert(BlockPosition(33, -61, 13), 2)
        baked.assert(BlockPosition(33, -61, 14), 2)
        baked.assert(BlockPosition(33, -61, 15), 3)
        baked.assert(BlockPosition(33, -61, 16), 1)
        baked.assert(BlockPosition(33, -61, 17), 2)
    }

    private fun WeightedBlockRender.assert(position: BlockPosition, entry: Int) {
        val model = getModel.invoke(this, Random(), position.raw) as BakedModel
        assertEquals(model.faces[0][0].positions[0].toInt(), entry)
    }


    private val A = SingleBlockStateApply(BlockModel(elements = listOf(ModelElement(Vec3f(1), Vec3f(10), faces = createFaces())), textures = mapOf("test" to minecraft("block/test").texture())))
    private val B = SingleBlockStateApply(BlockModel(elements = listOf(ModelElement(Vec3f(2), Vec3f(10), faces = createFaces())), textures = mapOf("test" to minecraft("block/test").texture())))
    private val C = SingleBlockStateApply(BlockModel(elements = listOf(ModelElement(Vec3f(3), Vec3f(10), faces = createFaces())), textures = mapOf("test" to minecraft("block/test").texture())))
    private val D = SingleBlockStateApply(BlockModel(elements = listOf(ModelElement(Vec3f(4), Vec3f(10), faces = createFaces())), textures = mapOf("test" to minecraft("block/test").texture())))

}
