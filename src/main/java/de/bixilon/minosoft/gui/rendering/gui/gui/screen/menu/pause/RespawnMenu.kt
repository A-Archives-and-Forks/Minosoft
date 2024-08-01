/*
 * Minosoft
 * Copyright (C) 2020-2024 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.gui.gui.screen.menu.pause

import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.kutil.observer.DataObserver.Companion.observe
import de.bixilon.minosoft.data.text.formatting.color.RGBColor
import de.bixilon.minosoft.gui.rendering.font.renderer.element.TextRenderProperties
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.elements.HorizontalAlignments
import de.bixilon.minosoft.gui.rendering.gui.elements.input.button.ButtonElement
import de.bixilon.minosoft.gui.rendering.gui.elements.spacer.SpacerElement
import de.bixilon.minosoft.gui.rendering.gui.elements.text.TextElement
import de.bixilon.minosoft.gui.rendering.gui.gui.ElementStates
import de.bixilon.minosoft.gui.rendering.gui.gui.GUIBuilder
import de.bixilon.minosoft.gui.rendering.gui.gui.LayoutedGUIElement
import de.bixilon.minosoft.gui.rendering.gui.gui.screen.menu.Menu
import de.bixilon.minosoft.protocol.network.session.play.PlaySessionStates

class RespawnMenu(guiRenderer: GUIRenderer) : Menu(guiRenderer) {

    init {
        background.tint = RGBColor(0xFF, 0x00, 0x00, 0x7F)
        add(TextElement(guiRenderer, "You died!", background = null, properties = TextRenderProperties(HorizontalAlignments.CENTER, scale = 3.0f)))
        add(SpacerElement(guiRenderer, Vec2(0, 20)))
        if (guiRenderer.session.world.hardcore) {
            add(TextElement(guiRenderer, "This world is hardcore, you cannot respawn!"))
        } else {
            add(ButtonElement(guiRenderer, "Respawn") { respawn() })
        }
    }

    fun respawn() {
        guiRenderer.session.util.respawn()
        canPop = true
        guiRenderer.gui.pop()
    }

    override fun onOpen() {
        super.onOpen()
        canPop = false
    }

    companion object : GUIBuilder<LayoutedGUIElement<RespawnMenu>> {

        override fun build(guiRenderer: GUIRenderer): LayoutedGUIElement<RespawnMenu> {
            return LayoutedGUIElement(RespawnMenu(guiRenderer))
        }

        fun register(guiRenderer: GUIRenderer) {
            guiRenderer.session.player::healthCondition.observe(this) {
                if (it.hp <= 0.0f) {
                    guiRenderer.gui.open(this)
                } else {
                    val element = guiRenderer.gui[this]
                    if (element.state == ElementStates.CLOSED) {
                        return@observe
                    }
                    element.layout.canPop = true
                    guiRenderer.gui.pop(element)
                }
            }
            guiRenderer.session::state.observe(this) {
                if (it != PlaySessionStates.SPAWNING) return@observe

                val element = guiRenderer.gui[this]
                if (element.state == ElementStates.CLOSED) {
                    return@observe
                }
                element.layout.canPop = true
                guiRenderer.gui.pop(element)
            }
        }
    }
}
