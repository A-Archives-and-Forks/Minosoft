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

package de.bixilon.minosoft.gui.rendering.gui.hud.elements.bossbar

import de.bixilon.minosoft.data.world.vec.vec2.f.Vec2f
import de.bixilon.kutil.collections.CollectionUtil.synchronizedMapOf
import de.bixilon.minosoft.data.bossbar.Bossbar
import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.elements.HorizontalAlignments
import de.bixilon.minosoft.gui.rendering.gui.elements.LayoutedElement
import de.bixilon.minosoft.gui.rendering.gui.elements.layout.RowLayout
import de.bixilon.minosoft.gui.rendering.gui.gui.LayoutedGUIElement
import de.bixilon.minosoft.gui.rendering.gui.hud.elements.HUDBuilder
import de.bixilon.minosoft.modding.event.events.bossbar.*
import de.bixilon.minosoft.modding.event.listener.CallbackEventListener.Companion.listen
import de.bixilon.minosoft.util.Initializable
import de.bixilon.minosoft.util.KUtil.toResourceLocation

class BossbarLayout(guiRenderer: GUIRenderer) : RowLayout(guiRenderer, HorizontalAlignments.CENTER, 2.0f), LayoutedElement, Initializable {
    private val session = context.session
    private val bossbars: MutableMap<Bossbar, BossbarElement> = synchronizedMapOf()

    override val layoutOffset: Vec2f
        get() = Vec2f((guiRenderer.scaledSize.x - super.size.x) / 2, 2)


    val atlas = guiRenderer.atlas[ATLAS]?.let { BossbarAtlas(it) }

    override fun postInit() {
        session.events.listen<BossbarAddEvent> {
            val element = BossbarElement(guiRenderer, it.bossbar, atlas)
            this += element
            val previous = bossbars.put(it.bossbar, element) ?: return@listen
            this -= previous
        }

        session.events.listen<BossbarRemoveEvent> {
            val element = bossbars.remove(it.bossbar) ?: return@listen
            this -= element
        }

        session.events.listen<BossbarValueSetEvent> {
            bossbars[it.bossbar]?.apply()
        }
        session.events.listen<BossbarTitleSetEvent> {
            bossbars[it.bossbar]?.apply()
        }
        session.events.listen<BossbarStyleSetEvent> {
            bossbars[it.bossbar]?.apply()
        }
    }


    companion object : HUDBuilder<LayoutedGUIElement<BossbarLayout>> {
        private val ATLAS = minecraft("hud/bossbar")
        override val identifier: ResourceLocation = "minosoft:bossbar".toResourceLocation()

        override fun register(gui: GUIRenderer) {
            gui.atlas.load(ATLAS)
        }

        override fun build(guiRenderer: GUIRenderer): LayoutedGUIElement<BossbarLayout> {
            return LayoutedGUIElement(BossbarLayout(guiRenderer))
        }
    }
}
