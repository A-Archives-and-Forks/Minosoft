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

package de.bixilon.minosoft.gui.rendering.gui.atlas

import de.bixilon.kmath.vec.vec2.f.Vec2f
import de.bixilon.kmath.vec.vec2.i.Vec2i
import de.bixilon.minosoft.gui.rendering.system.base.texture.TexturePart
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.Texture
import it.unimi.dsi.fastutil.ints.Int2ObjectMap

class AtlasElement(
    override val texture: Texture,
    override val uvStart: Vec2f,
    override val uvEnd: Vec2f,
    override val size: Vec2i,
    val slots: Int2ObjectMap<AtlasArea>?,
    val areas: Map<String, AtlasArea>?,
) : TexturePart
