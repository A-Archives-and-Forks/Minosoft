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

package de.bixilon.minosoft.gui.rendering.gui.elements.input.button

import de.bixilon.minosoft.data.world.vec.vec2.f.Vec2f
import de.bixilon.minosoft.data.world.vec.vec2.i.Vec2i
import de.bixilon.minosoft.config.key.KeyCodes
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.atlas.AtlasElement
import de.bixilon.minosoft.gui.rendering.gui.elements.Element
import de.bixilon.minosoft.gui.rendering.gui.elements.HorizontalAlignments
import de.bixilon.minosoft.gui.rendering.gui.elements.HorizontalAlignments.Companion.getOffset
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

abstract class AbstractButtonElement(
    guiRenderer: GUIRenderer,
    text: Any,
    disabled: Boolean = false,
) : Element(guiRenderer) {
    protected val textElement = TextElement(guiRenderer, text, background = null, parent = this)
    protected abstract val disabledAtlas: AtlasElement?
    protected abstract val normalAtlas: AtlasElement?
    protected abstract val hoveredAtlas: AtlasElement?

    private var _dynamicSized = true
    var dynamicSized: Boolean
        get() = _dynamicSized
        set(value) {
            if (_dynamicSized == value) {
                return
            }

            textElement.prefMaxSize = if (value) {
                Vec2f(-1, -1)
            } else {
                size - Vec2f(TEXT_PADDING * 2, TEXT_PADDING * 2)
            }
            _dynamicSized = value
            forceApply()
        }


    override var size: Vec2f
        get() = super.size
        set(value) {
            _dynamicSized = false
            super.size = value // will call forceApply
        }

    var _disabled: Boolean = disabled
        set(value) {
            if (field == value) {
                return
            }
            field = value
        }
    var disabled: Boolean
        get() = _disabled
        set(value) {
            if (_disabled == value) {
                return
            }
            _disabled = disabled
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
        size = textElement.size + Vec2f(TEXT_PADDING * 2, TEXT_PADDING * 2)
    }

    override fun forceRender(offset: Vec2f, consumer: GUIVertexConsumer, options: GUIVertexOptions?) {
        val texture = when {
            disabled -> disabledAtlas
            hovered -> hoveredAtlas
            else -> normalAtlas
        } ?: guiRenderer.context.textures.whiteTexture

        val size = size
        val background = AtlasImageElement(guiRenderer, texture)
        background.size = size
        val textSize = textElement.size

        background.render(offset, consumer, options)
        textElement.render(offset + Vec2f(HorizontalAlignments.CENTER.getOffset(size.x, textSize.x), VerticalAlignments.CENTER.getOffset(size.y, textSize.y)), consumer, options)
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

        _submit()
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
        _submit()
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

    abstract fun submit()

    private fun _submit() {
        if (guiRenderer.session.profiles.audio.gui.button) {
            guiRenderer.session.world.play2DSound(CLICK_SOUND)
        }
        submit()
    }

    override fun onChildChange(child: Element) {
        if (child == textElement) {
            if (dynamicSized) {
                size = textElement.size + Vec2f(TEXT_PADDING * 2, TEXT_PADDING * 2)
            }
            cacheUpToDate = false
        }
        parent?.onChildChange(this)
    }

    private companion object {
        val CLICK_SOUND = "minecraft:ui.button.click".toResourceLocation()
        const val TEXT_PADDING = 4
    }
}
