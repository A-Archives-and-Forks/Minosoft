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

package de.bixilon.minosoft.gui.rendering.system.base.texture.texture

import de.bixilon.minosoft.data.world.vec.vec2.f.Vec2f
import de.bixilon.minosoft.data.world.vec.vec2.i.Vec2i
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.system.base.texture.TextureStates
import de.bixilon.minosoft.gui.rendering.system.base.texture.TextureTransparencies
import de.bixilon.minosoft.gui.rendering.system.base.texture.array.TextureArrayProperties
import de.bixilon.minosoft.gui.rendering.system.base.texture.data.MipmapTextureData
import de.bixilon.minosoft.gui.rendering.system.base.texture.data.TextureData
import de.bixilon.minosoft.gui.rendering.system.base.texture.data.buffer.TextureBuffer
import de.bixilon.minosoft.gui.rendering.system.base.texture.shader.ShaderTexture
import de.bixilon.minosoft.gui.rendering.textures.TextureAnimation
import de.bixilon.minosoft.gui.rendering.textures.properties.ImageProperties

interface Texture : ShaderTexture {
    var array: TextureArrayProperties
    val state: TextureStates
    val size: Vec2i
    val transparency: TextureTransparencies
    val properties: ImageProperties
    val animation: TextureAnimation? get() = null

    var renderData: TextureRenderData

    var data: TextureData
    val mipmaps: Int


    fun load(context: RenderContext)

    override val shaderId: Int
        get() = renderData.shaderTextureId


    override fun transformUV(end: FloatArray?): FloatArray {
        return renderData.transformUV(end)
    }

    override fun transformUV(end: Vec2f?): Vec2f {
        return renderData.transformUV(end)
    }

    fun createData(mipmaps: Int = this.mipmaps, buffer: TextureBuffer): TextureData {
        if (mipmaps <= 0) return TextureData(buffer)
        return MipmapTextureData(buffer, mipmaps)
    }
}
