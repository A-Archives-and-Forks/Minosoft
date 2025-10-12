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

package de.bixilon.minosoft.gui.rendering.entities.feature.block

import glm_.mat4x4.Mat4
import de.bixilon.minosoft.data.world.vec.vec3.f.Vec3f
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.gui.rendering.entities.feature.properties.MeshedFeature
import de.bixilon.minosoft.gui.rendering.entities.renderer.EntityRenderer
import de.bixilon.minosoft.gui.rendering.entities.visibility.EntityLayer
import de.bixilon.minosoft.gui.rendering.util.mat.mat4.Mat4Util.reset
import de.bixilon.minosoft.gui.rendering.util.mat.mat4.Mat4Util.translateXAssign
import de.bixilon.minosoft.gui.rendering.util.mat.mat4.Mat4Util.translateZAssign
import kotlin.time.TimeSource.Monotonic.ValueTimeMark

open class BlockFeature(
    renderer: EntityRenderer<*>,
    state: BlockState?,
    var scale: Vec3f = DEFAULT_SCALE,
) : MeshedFeature<BlockMesh>(renderer) {
    private var matrix = Mat4()
    var state: BlockState? = state
        set(value) {
            if (field == value) return
            field = value
            unload()
        }

    override val layer get() = EntityLayer.Translucent // TODO

    override fun update(time: ValueTimeMark, delta: Float) {
        if (!_enabled) return unload()
        if (this.mesh == null) {
            val state = this.state ?: return unload()
            createMesh(state)
        }
        updateMatrix()
    }

    private fun createMesh(state: BlockState) {
        val mesh = BlockMesh(renderer.renderer.context)
        val model = (state.block.model ?: state.model) ?: return
        // TODO: block entity support?

        val tint = renderer.renderer.context.tints.getBlockTint(state, BlockPosition(), null, null) // TODO

        model.render(mesh, state, tint)

        this.mesh = mesh
    }

    private fun updateMatrix() {
        this.matrix.reset()
        this.matrix
            .scaleAssign(this.scale)
            .translateXAssign(-0.5f).translateZAssign(-0.5f)

        // TODO: rotate?

        this.matrix = renderer.matrix * matrix
    }

    override fun draw(mesh: BlockMesh) {
        renderer.renderer.context.system.set(layer.settings)
        val shader = renderer.renderer.features.block.shader
        draw(mesh, shader)
    }

    protected open fun draw(mesh: BlockMesh, shader: BlockShader) {
        shader.use()
        shader.matrix = matrix
        shader.tint = renderer.light.value.rgba()
        super.draw(mesh)
    }

    companion object {
        val DEFAULT_SCALE = Vec3f(0.99f)
    }
}
