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

package de.bixilon.minosoft.gui.rendering.gui.elements.util

import de.bixilon.minosoft.data.world.vec.vec2.f.Vec2f
import de.bixilon.kutil.math.interpolation.FloatInterpolation.interpolateLinear
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.atlas.AtlasElement
import de.bixilon.minosoft.gui.rendering.gui.elements.Element
import de.bixilon.minosoft.gui.rendering.gui.elements.primitive.AtlasImageElement
import de.bixilon.minosoft.gui.rendering.gui.elements.primitive.ImageElement
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions

open class ProgressElement(
    guiRenderer: GUIRenderer,
    val emptyAtlasElement: AtlasElement?,
    val fullAtlasElement: AtlasElement?,
    progress: Float = 0.0f,
) : Element(guiRenderer) {
    var progress = progress
        set(value) {
            if (field == value) {
                return
            }
            field = value
            forceSilentApply()
            // ToDo: Animate
        }
    protected val emptyImage = AtlasImageElement(guiRenderer, emptyAtlasElement)
    protected var progressImage: ImageElement? = null


    constructor(guiRenderer: GUIRenderer, atlasElements: Array<AtlasElement?>, progress: Float = 0.0f) : this(guiRenderer, atlasElements.getOrNull(0), atlasElements.getOrNull(1), progress)

    init {
        _size = emptyAtlasElement?.size?.let { Vec2f(it) } ?: Vec2f.EMPTY
        forceSilentApply()
    }

    override fun forceRender(offset: Vec2f, consumer: GUIVertexConsumer, options: GUIVertexOptions?) {
        emptyImage.render(offset, consumer, options)
        progressImage?.render(offset, consumer, options)
    }

    override fun forceSilentApply() {
        val full = fullAtlasElement ?: return

        val uvEnd = Vec2f(interpolateLinear(progress, full.uvStart.x, full.uvEnd.x), full.uvEnd.y)
        val size = this.size
        progressImage = ImageElement(guiRenderer, full.texture, uvStart = full.uvStart, uvEnd = uvEnd, size = Vec2f((size.x * progress), size.y))

        cacheUpToDate = false
    }
}
