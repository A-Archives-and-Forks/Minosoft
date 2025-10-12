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

package de.bixilon.minosoft.gui.rendering.gui.elements.input.checkbox

import de.bixilon.minosoft.data.world.vec.vec2.f.Vec2f
import de.bixilon.minosoft.data.world.vec.vec2.i.Vec2i
import de.bixilon.minosoft.config.key.KeyCodes
import de.bixilon.minosoft.data.registries.identified.Namespaces.minosoft
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.atlas.Atlas.Companion.get
import de.bixilon.minosoft.gui.rendering.gui.elements.Element
import de.bixilon.minosoft.gui.rendering.gui.elements.VerticalAlignments
import de.bixilon.minosoft.gui.rendering.gui.elements.VerticalAlignments.Companion.getOffset
import de.bixilon.minosoft.gui.rendering.gui.elements.primitive.AtlasImageElement
import de.bixilon.minosoft.gui.rendering.gui.elements.text.TextElement
import de.bixilon.minosoft.gui.rendering.gui.input.mouse.MouseActions
import de.bixilon.minosoft.gui.rendering.gui.input.mouse.MouseButtons
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions
import de.bixilon.minosoft.gui.rendering.system.window.CursorShapes
import de.bixilon.minosoft.gui.rendering.system.window.KeyChangeTypes
import de.bixilon.minosoft.util.KUtil.toResourceLocation

open class SwitchElement(
    guiRenderer: GUIRenderer,
    text: Any,
    state: Boolean = false,
    disabled: Boolean = false,
    parent: Element?,
    var onChange: (state: Boolean) -> Unit,
) : AbstractCheckboxElement(guiRenderer) {
    protected val textElement = TextElement(guiRenderer, text, background = null).apply { this.parent = this@SwitchElement }
    private val atlas = guiRenderer.atlas[ATLAS]
    private val disabledAtlas = atlas["disabled"]
    private val normalAtlas = atlas["normal"]
    private val hoveredAtlas = atlas["hovered"]

    private val onStateAtlas = atlas["state_on"]
    private val offStateAtlas = atlas["state_off"]

    var state: Boolean = state
        set(value) {
            if (field == value) {
                return
            }
            field = value
            onChange(state)
            forceApply()
        }
    var disabled: Boolean = disabled
        set(value) {
            if (field == value) {
                return
            }
            field = value
            forceApply()
        }

    var hovered: Boolean = false
        private set(value) {
            if (field == value) {
                return
            }
            field = value
            forceApply()
        }

    override val canFocus: Boolean
        get() = !disabled


    init {
        size = SIZE + Vec2f(5f + TEXT_MARGIN + textElement.size.x, 0f)
        this.parent = parent
    }

    override fun forceRender(offset: Vec2f, consumer: GUIVertexConsumer, options: GUIVertexOptions?) {
        val texture = when {
            disabled -> disabledAtlas
            hovered -> hoveredAtlas
            else -> normalAtlas
        } ?: guiRenderer.context.textures.whiteTexture

        val size = size
        val background = AtlasImageElement(guiRenderer, texture)
        background.size = SIZE

        background.render(offset, consumer, options)


        if (state) {
            AtlasImageElement(guiRenderer, onStateAtlas, size = SLIDER_SIZE).render(offset + Vec2f(SIZE.x - SLIDER_SIZE.x, 0f), consumer, options)
        } else {
            AtlasImageElement(guiRenderer, offStateAtlas, size = SLIDER_SIZE).render(offset, consumer, options)
        }

        textElement.render(offset + Vec2f(SIZE.x + TEXT_MARGIN, VerticalAlignments.CENTER.getOffset(size.y, textElement.size.y)), consumer, options)
    }

    override fun forceSilentApply() {
        textElement.silentApply()
        cacheUpToDate = false
    }

    override fun onMouseAction(position: Vec2f, button: MouseButtons, action: MouseActions, count: Int): Boolean {
        if (disabled) {
            return true
        }
        if (button != MouseButtons.LEFT) {
            return true
        }
        if (action != MouseActions.PRESS) {
            return true
        }

        switchState()
        return true
    }

    override fun onKey(key: KeyCodes, type: KeyChangeTypes): Boolean {
        if (!hovered) {
            return true
        }
        if (disabled) {
            return true
        }
        if (key != KeyCodes.KEY_ENTER) {
            return true
        }
        if (type != KeyChangeTypes.PRESS) {
            return true
        }
        switchState()
        return true
    }

    override fun onMouseEnter(position: Vec2f, absolute: Vec2f): Boolean {
        hovered = true
        context.window.cursorShape = CursorShapes.HAND

        return true
    }

    override fun onMouseLeave(): Boolean {
        hovered = false
        context.window.resetCursor()

        return true
    }

    open fun switchState() {
        state = !state
        if (guiRenderer.session.profiles.audio.gui.button) {
            guiRenderer.session.world.play2DSound(CLICK_SOUND)
        }
    }

    companion object {
        val ATLAS = minosoft("elements/switch")
        val CLICK_SOUND = "minecraft:ui.button.click".toResourceLocation()
        const val TEXT_MARGIN = 5
        val SIZE = Vec2f(30, 20)
        val SLIDER_SIZE = Vec2f(6, 20)
    }
}
