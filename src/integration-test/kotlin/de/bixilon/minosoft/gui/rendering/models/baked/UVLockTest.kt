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

import de.bixilon.minosoft.data.world.vec.vec3.f.Vec3f
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.gui.rendering.models.ModelTestUtil.bake
import de.bixilon.minosoft.gui.rendering.models.ModelTestUtil.bgyr
import de.bixilon.minosoft.gui.rendering.models.ModelTestUtil.block
import de.bixilon.minosoft.gui.rendering.models.ModelTestUtil.gyrb
import de.bixilon.minosoft.gui.rendering.models.ModelTestUtil.rbgy
import de.bixilon.minosoft.gui.rendering.models.ModelTestUtil.yrbg
import de.bixilon.minosoft.gui.rendering.models.baked.BakedModelTestUtil.assertFace
import de.bixilon.minosoft.gui.rendering.models.baked.BakedModelTestUtil.createFaces
import de.bixilon.minosoft.gui.rendering.models.baked.BakedModelTestUtil.createTextureManager
import de.bixilon.minosoft.gui.rendering.models.block.BlockModel
import de.bixilon.minosoft.gui.rendering.models.block.element.ModelElement
import de.bixilon.minosoft.gui.rendering.models.block.element.face.FaceUV
import de.bixilon.minosoft.gui.rendering.models.block.element.face.ModelFace
import de.bixilon.minosoft.gui.rendering.models.block.state.apply.SingleBlockStateApply
import de.bixilon.minosoft.gui.rendering.models.util.CuboidUtil.positions
import de.bixilon.minosoft.gui.rendering.textures.TextureUtil.texture
import org.testng.annotations.Test

@Test(groups = ["models"])
class UVLockTest {

    fun y90() {
        val from = Vec3f(0.0f)
        val to = Vec3f(1.0f)
        val model = SingleBlockStateApply(BlockModel(elements = listOf(ModelElement(from, to, faces = createFaces())), textures = mapOf("test" to minecraft("block/test").texture())), y = 1, uvLock = true)

        val baked = model.bake(createTextureManager("block/test"))!!

        baked.assertFace(Directions.DOWN, positions(Directions.DOWN, from, to), yrbg, 0.5f)
        baked.assertFace(Directions.UP, positions(Directions.UP, from, to), rbgy, 1.0f)
        baked.assertFace(Directions.NORTH, positions(Directions.NORTH, from, to), gyrb, 0.8f)
        baked.assertFace(Directions.SOUTH, positions(Directions.SOUTH, from, to), yrbg, 0.8f)
        baked.assertFace(Directions.WEST, positions(Directions.WEST, from, to), yrbg, 0.6f)
        baked.assertFace(Directions.EAST, positions(Directions.EAST, from, to), gyrb, 0.6f)
    }

    fun y90Rotation1() {
        val from = Vec3f(0.0f)
        val to = Vec3f(1.0f)
        val model = SingleBlockStateApply(BlockModel(elements = listOf(ModelElement(from, to, faces = createFaces(rotation = 1))), textures = mapOf("test" to minecraft("block/test").texture())), y = 1, uvLock = true)

        val baked = model.bake(createTextureManager("block/test"))!!

        baked.assertFace(Directions.DOWN, positions(Directions.DOWN, from, to), gyrb, 0.5f)
        baked.assertFace(Directions.UP, positions(Directions.UP, from, to), yrbg, 1.0f)
        baked.assertFace(Directions.NORTH, positions(Directions.NORTH, from, to), bgyr, 0.8f)
        baked.assertFace(Directions.SOUTH, positions(Directions.SOUTH, from, to), gyrb, 0.8f)
        baked.assertFace(Directions.WEST, positions(Directions.WEST, from, to), gyrb, 0.6f)
        baked.assertFace(Directions.EAST, positions(Directions.EAST, from, to), bgyr, 0.6f)
    }

    fun x90() {
        val from = Vec3f(0.0f)
        val to = Vec3f(1.0f)
        val model = SingleBlockStateApply(BlockModel(elements = listOf(ModelElement(from, to, faces = createFaces())), textures = mapOf("test" to minecraft("block/test").texture())), x = 1, uvLock = true)

        val baked = model.bake(createTextureManager("block/test"))!!

        baked.assertFace(Directions.DOWN, positions(Directions.DOWN, from, to), yrbg, 0.5f)
        baked.assertFace(Directions.UP, positions(Directions.UP, from, to), rbgy, 1.0f)
        baked.assertFace(Directions.NORTH, positions(Directions.NORTH, from, to), gyrb, 0.8f)
        baked.assertFace(Directions.SOUTH, positions(Directions.SOUTH, from, to), yrbg, 0.8f)
        baked.assertFace(Directions.WEST, positions(Directions.WEST, from, to), yrbg, 0.6f)
        baked.assertFace(Directions.EAST, positions(Directions.EAST, from, to), gyrb, 0.6f)
    }


    fun `half cube without rotation`() {
        val from = Vec3f(0.0f, 0.5f, 0.0f)
        val to = Vec3f(1.0f, 1.0f, 0.5f)
        val model = SingleBlockStateApply(BlockModel(elements = listOf(ModelElement(from, to, faces = createFaces())), textures = mapOf("test" to minecraft("block/test").texture())), uvLock = true)

        val baked = model.bake(createTextureManager("block/test"))!!

        baked.assertFace(Directions.DOWN, positions(Directions.DOWN, from, to), block(0, 16, 0, 8, 16, 8, 16, 16), 0.5f)
        baked.assertFace(Directions.UP, positions(Directions.UP, from, to), block(0, 0, 16, 0, 16, 8, 0, 8), 1.0f)
    }

    fun `half cube with y=90`() {
        val from = Vec3f(0.0f, 0.5f, 0.0f)
        val to = Vec3f(1.0f, 1.0f, 0.5f)
        val model = SingleBlockStateApply(BlockModel(elements = listOf(ModelElement(from, to, faces = createFaces())), textures = mapOf("test" to minecraft("block/test").texture())), uvLock = true, y = 1)

        val baked = model.bake(createTextureManager("block/test"))!!

        baked.assertFace(Directions.DOWN, positions(Directions.DOWN, Vec3f(0.5f, 0.5f, 0.0f), Vec3f(1.0f)), block(8, 16, 8, 0, 16, 0, 16, 16), 0.5f)
        baked.assertFace(Directions.UP, positions(Directions.UP, Vec3f(0.5f, 0.5f, 0.0f), Vec3f(1.0f)), block(8, 0, 16, 0, 16, 16, 8, 16), 1.0f)
    }

    fun `half cube with y=270`() {
        val from = Vec3f(0.0f, 0.5f, 0.0f)
        val to = Vec3f(1.0f, 1.0f, 0.5f)
        val model = SingleBlockStateApply(BlockModel(elements = listOf(ModelElement(from, to, faces = createFaces())), textures = mapOf("test" to minecraft("block/test").texture())), uvLock = true, y = 3)

        val baked = model.bake(createTextureManager("block/test"))!!

        baked.assertFace(Directions.DOWN, positions(Directions.DOWN, from, Vec3f(0.5f, 1.0f, 1.0f)), block(0, 16, 0, 0, 8, 0, 8, 16), 0.5f)
        baked.assertFace(Directions.UP, positions(Directions.UP, from, Vec3f(0.5f, 1.0f, 1.0f)), block(0, 0, 8, 0, 8, 16, 0, 16), 1.0f)
    }

    fun `stairs top y=90`() {
        val from = Vec3f(0.5f, 0.5f, 0.0f)
        val to = Vec3f(1.0f, 1.0f, 1.0f)
        val model = SingleBlockStateApply(BlockModel(elements = listOf(ModelElement(from, to, faces = mapOf(Directions.UP to ModelFace("#test", FaceUV(0, 16, 8, 0), 0, -1)))), textures = mapOf("test" to minecraft("block/test").texture())), uvLock = true, y = 1)

        val baked = model.bake(createTextureManager("block/test"))!!

        baked.assertFace(Directions.UP, positions(Directions.UP, Vec3f(0, 0.5, 0.5), Vec3f(1, 1, 1.0f)), block(0, 0, 16, 0, 16, 8, 0, 8), 1.0f)
    }

    fun `stairs top y=180`() {
        val from = Vec3f(0.5f, 0.5f, 0.0f)
        val to = Vec3f(1.0f, 1.0f, 1.0f)
        val model = SingleBlockStateApply(BlockModel(elements = listOf(ModelElement(from, to, faces = mapOf(Directions.UP to ModelFace("#test", FaceUV(0, 16, 8, 0), 0, -1)))), textures = mapOf("test" to minecraft("block/test").texture())), uvLock = true, y = 2)

        val baked = model.bake(createTextureManager("block/test"))!!

        baked.assertFace(Directions.UP, positions(Directions.UP, Vec3f(0, 0.5, 0.0), Vec3f(0.5f, 1, 1.0f)), block(8, 0, 16, 0, 16, 16, 8, 16), 1.0f)
    }

    fun `stairs top y=270`() {
        val from = Vec3f(0.5f, 0.5f, 0.0f)
        val to = Vec3f(1.0f, 1.0f, 1.0f)
        val model = SingleBlockStateApply(BlockModel(elements = listOf(ModelElement(from, to, faces = mapOf(Directions.UP to ModelFace("#test", FaceUV(8, 16, 16, 0), 0, -1)))), textures = mapOf("test" to minecraft("block/test").texture())), uvLock = true, y = 3)

        val baked = model.bake(createTextureManager("block/test"))!!

        baked.assertFace(Directions.UP, positions(Directions.UP, Vec3f(0, 0.5, 0), Vec3f(1, 1, 0.5f)), block(0, 0, 16, 0, 16, 8, 0, 8), 1.0f)
    }

    fun `stairs top 2 y=270`() {
        val from = Vec3f(0.5f, 0.5f, 0.0f)
        val to = Vec3f(1.0f, 1.0f, 1.0f)
        val model = SingleBlockStateApply(BlockModel(elements = listOf(ModelElement(from, to, faces = mapOf(Directions.UP to ModelFace("#test", FaceUV(0, 16, 8, 0), 0, -1)))), textures = mapOf("test" to minecraft("block/test").texture())), uvLock = true, y = 3)

        val baked = model.bake(createTextureManager("block/test"))!!

        baked.assertFace(Directions.UP, positions(Directions.UP, Vec3f(0, 0.5, 0), Vec3f(1, 1, 0.5f)), block(0, 8, 16, 8, 16, 16, 0, 16), 1.0f)
    }

    // TODO: test rotation around x axis and combinations
}
