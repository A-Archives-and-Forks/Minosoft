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

package de.bixilon.minosoft.gui.rendering.gui.hud.elements.hotbar

import de.bixilon.kmath.vec.vec2.f.Vec2f
import de.bixilon.kmath.vec.vec2.i.Vec2i
import de.bixilon.minosoft.data.entities.entities.player.Arms
import de.bixilon.minosoft.data.entities.entities.player.Arms.Companion.opposite
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.atlas.Atlas.Companion.get
import de.bixilon.minosoft.gui.rendering.gui.elements.Element
import de.bixilon.minosoft.gui.rendering.gui.elements.items.ContainerItemsElement
import de.bixilon.minosoft.gui.rendering.gui.elements.primitive.AtlasImageElement
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions
import de.bixilon.minosoft.gui.rendering.util.vec.vec4.Vec4fUtil.marginOf
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap

class HotbarOffhandElement(guiRenderer: GUIRenderer) : Element(guiRenderer) {
    private val atlas = guiRenderer.atlas[HotbarBaseElement.ATLAS]
    private val frames = arrayOf(
        atlas["offhand_right"],
        atlas["offhand_left"],
    )

    val offArm = guiRenderer.context.session.player.mainArm.opposite // ToDo: Support arm change
    private val frame = frames[offArm.ordinal]

    private var frameImage = AtlasImageElement(guiRenderer, frame)
    private val containerElement = ContainerItemsElement(guiRenderer, guiRenderer.context.session.player.items.inventory, frame?.slots ?: Int2ObjectOpenHashMap())

    init {
        _size = Vec2f(frame?.size ?: Vec2i.EMPTY)
        val margin = if (offArm == Arms.LEFT) {
            marginOf(right = 5.0f)
        } else {
            marginOf(left = 5.0f)
        }
        this.margin = margin
        containerElement.parent = this
    }


    override fun forceRender(offset: Vec2f, consumer: GUIVertexConsumer, options: GUIVertexOptions?) {
        frameImage.render(offset, consumer, options)
        containerElement.render(offset, consumer, options)
    }

    override fun silentApply(): Boolean {
        val container = containerElement.silentApply()

        if (super.silentApply()) {
            return true
        }
        if (container) {
            forceSilentApply()
            return true
        }
        return false
    }

    override fun forceSilentApply() {
        cacheUpToDate = false
    }
}
