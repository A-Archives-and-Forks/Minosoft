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

package de.bixilon.minosoft.gui.rendering.gui.gui.screen.menu.debug

import glm_.vec2.Vec2
import glm_.vec3.Vec3d
import de.bixilon.minosoft.data.text.TextComponent
import de.bixilon.minosoft.data.text.formatting.color.ChatColors
import de.bixilon.minosoft.data.world.weather.WorldWeather
import de.bixilon.minosoft.gui.rendering.font.renderer.element.TextRenderProperties
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.elements.HorizontalAlignments
import de.bixilon.minosoft.gui.rendering.gui.elements.input.button.ButtonElement
import de.bixilon.minosoft.gui.rendering.gui.elements.spacer.SpacerElement
import de.bixilon.minosoft.gui.rendering.gui.elements.text.TextElement
import de.bixilon.minosoft.gui.rendering.gui.gui.GUIBuilder
import de.bixilon.minosoft.gui.rendering.gui.gui.LayoutedGUIElement
import de.bixilon.minosoft.gui.rendering.gui.gui.screen.menu.Menu

class DebugMenu(guiRenderer: GUIRenderer) : Menu(guiRenderer) {
    private val session = guiRenderer.session

    init {
        this += TextElement(guiRenderer, "Debug options", background = null, properties = TextRenderProperties(HorizontalAlignments.CENTER))
        this += SpacerElement(guiRenderer, Vec2(0, 10))

        this += ButtonElement(guiRenderer, "Switch to next gamemode") { session.util.typeChat("/gamemode ${session.player.gamemode.next().name.lowercase()}") }
        this += ButtonElement(guiRenderer, "Hack to next gamemode") { session.player.additional.apply { gamemode = gamemode.next() } }
        this += ButtonElement(guiRenderer, "Fake y=100") {
            val entity = session.player
            val position = entity.physics.position

            entity.forceTeleport(Vec3d(position.x, 100.0, position.z))
        }
        this += ButtonElement(guiRenderer, "Toggle allow flight") { session.player.apply { abilities = abilities.copy(allowFly = !abilities.allowFly) } }
        this += ButtonElement(guiRenderer, TextComponent("☀").color(ChatColors.YELLOW)) { session.world.weather = WorldWeather.SUNNY }

        this += ButtonElement(guiRenderer, "Back") { guiRenderer.gui.pop() }
    }

    companion object : GUIBuilder<LayoutedGUIElement<DebugMenu>> {
        override fun build(guiRenderer: GUIRenderer): LayoutedGUIElement<DebugMenu> {
            return LayoutedGUIElement(DebugMenu(guiRenderer))
        }
    }
}
