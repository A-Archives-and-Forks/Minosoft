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

package de.bixilon.minosoft.gui.rendering.entities.model.animator

import glm_.func.rad
import de.bixilon.minosoft.data.world.vec.vec3.f.Vec3f
import de.bixilon.minosoft.gui.rendering.entities.easteregg.EntityEasterEggs.isFlipped
import de.bixilon.minosoft.gui.rendering.entities.renderer.EntityRenderer
import de.bixilon.minosoft.gui.rendering.skeletal.instance.TransformInstance
import de.bixilon.minosoft.gui.rendering.util.mat.mat4.Mat4Util.rotateRadAssign
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3Util.EMPTY

class HeadAnimator(
    val renderer: EntityRenderer<*>,
    val transform: TransformInstance,
) {
    private var rotation = Vec3f.EMPTY

    fun update() {
        val info = renderer.info

        val pitch = info.rotation.pitch
        this.rotation.x = pitch.rad
        if (renderer.entity.isFlipped()) {
            this.rotation.x = -this.rotation.x // TODO: not 100% correct
        }
        transform.value
            .translateAssign(transform.pivot)
            .rotateRadAssign(this.rotation)
            .translateAssign(transform.nPivot)
    }
}
