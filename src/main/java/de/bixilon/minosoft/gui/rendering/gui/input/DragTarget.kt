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

package de.bixilon.minosoft.gui.rendering.gui.input

import de.bixilon.minosoft.data.world.vec.vec2.f.Vec2f
import de.bixilon.kutil.cast.CastUtil.nullCast
import de.bixilon.minosoft.config.key.KeyCodes
import de.bixilon.minosoft.gui.rendering.gui.elements.Element
import de.bixilon.minosoft.gui.rendering.gui.gui.dragged.Dragged
import de.bixilon.minosoft.gui.rendering.gui.input.mouse.MouseActions
import de.bixilon.minosoft.gui.rendering.gui.input.mouse.MouseButtons
import de.bixilon.minosoft.gui.rendering.system.window.KeyChangeTypes

interface DragTarget {

    fun onDragEnter(position: Vec2f, absolute: Vec2f, draggable: Dragged): Element? = this.nullCast()
    fun onDragMove(position: Vec2f, absolute: Vec2f, draggable: Dragged): Element? = this.nullCast()
    fun onDragLeave(draggable: Dragged): Element? = this.nullCast()

    fun onDragScroll(position: Vec2f, scrollOffset: Vec2f, draggable: Dragged): Element? = this.nullCast()

    fun onDragMouseAction(position: Vec2f, button: MouseButtons, action: MouseActions, count: Int, draggable: Dragged): Element? = this.nullCast()
    fun onDragKey(key: KeyCodes, type: KeyChangeTypes, draggable: Dragged): Element? = this.nullCast()
    fun onDragChar(char: Char, draggable: Dragged): Element? = this.nullCast()
}
