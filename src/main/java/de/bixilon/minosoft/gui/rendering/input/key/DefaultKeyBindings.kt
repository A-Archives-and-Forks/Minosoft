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

package de.bixilon.minosoft.gui.rendering.input.key

import de.bixilon.minosoft.config.key.KeyActions
import de.bixilon.minosoft.config.key.KeyBinding
import de.bixilon.minosoft.config.key.KeyCodes
import de.bixilon.minosoft.data.registries.identified.Namespaces.minosoft
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.util.KUtil.format

object DefaultKeyBindings {
    val SCREENSHOT = minosoft("take_screenshot")
    val FULLSCREEN = minosoft("toggle_fullscreen")

    fun register(context: RenderContext) {
        val bindings = context.input.bindings
        val window = context.window
        val session = context.session

        bindings.register(SCREENSHOT, KeyBinding(
            KeyActions.PRESS to setOf(KeyCodes.KEY_F2),
            ignoreConsumer = true,
        )) { context.screenshotTaker.takeScreenshot() }


        bindings.register(FULLSCREEN, KeyBinding(
            KeyActions.PRESS to setOf(KeyCodes.KEY_F11),
            ignoreConsumer = true,
        )) {
            val next = !window.fullscreen
            window.fullscreen = next
            session.util.sendDebugMessage("Fullscreen: ${next.format()}")
        }
    }
}
