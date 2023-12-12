/*
 * Minosoft
 * Copyright (C) 2020-2023 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.system.opengl.texture

import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.kutil.latch.AbstractLatch
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.system.base.shader.NativeShader
import de.bixilon.minosoft.gui.rendering.system.base.texture.TextureStates
import de.bixilon.minosoft.gui.rendering.system.base.texture.array.TextureArrayProperties
import de.bixilon.minosoft.gui.rendering.system.base.texture.array.TextureArrayStates
import de.bixilon.minosoft.gui.rendering.system.base.texture.array.font.FontCompressions
import de.bixilon.minosoft.gui.rendering.system.base.texture.array.font.FontTextureArray
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.Texture
import de.bixilon.minosoft.gui.rendering.system.opengl.OpenGLRenderSystem
import de.bixilon.minosoft.gui.rendering.system.opengl.texture.OpenGLTextureUtil.glFormat
import de.bixilon.minosoft.gui.rendering.system.opengl.texture.OpenGLTextureUtil.glType
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import org.lwjgl.opengl.GL30.*
import org.lwjgl.opengl.GL33.GL_TEXTURE_SWIZZLE_RGBA
import java.nio.ByteBuffer

class OpenGLFontTextureArray(
    context: RenderContext,
    compressed: FontCompressions,
) : FontTextureArray(context, RESOLUTION, compressed) {
    val index = context.system.unsafeCast<OpenGLRenderSystem>().textureBindingIndex++
    private var handle = -1
    private var textureIndex = 0


    override fun upload(latch: AbstractLatch?) {
        this.handle = OpenGLTextureUtil.createTextureArray(0)
        val format = when (compression) {
            FontCompressions.NONE -> GL_RGBA8
            FontCompressions.ALPHA -> GL_ALPHA8
            FontCompressions.COMPRESSED_ALPHA -> GL_COMPRESSED_ALPHA
        }
        if (compression != FontCompressions.NONE) {
            glTexParameteriv(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_SWIZZLE_RGBA, intArrayOf(GL_ONE, GL_ONE, GL_ONE, GL_ALPHA))
        }

        glTexImage3D(GL_TEXTURE_2D_ARRAY, 0, format, RESOLUTION, RESOLUTION, textures.size, 0, GL_RGBA, GL_UNSIGNED_BYTE, null as ByteBuffer?)

        for (texture in textures) {
            val renderData = texture.renderData as OpenGLTextureData
            val buffer = texture.data.buffer
            buffer.data.flip()
            glTexSubImage3D(GL_TEXTURE_2D_ARRAY, 0, 0, 0, renderData.index, buffer.size.x, buffer.size.y, 1, buffer.glFormat, buffer.glType, buffer.data)
        }

        Log.log(LogMessageType.RENDERING, LogLevels.VERBOSE) { "Loaded ${textures.size} font textures" }
        state = TextureArrayStates.UPLOADED
    }

    override fun activate() {
        glActiveTexture(GL_TEXTURE0 + index)
        glBindTexture(GL_TEXTURE_2D_ARRAY, handle)
    }

    override fun use(shader: NativeShader, name: String) {
        if (state != TextureArrayStates.UPLOADED) throw IllegalStateException("Texture array is not uploaded yet! Are you trying to load a shader in the init phase?")
        shader.use()
        activate()

        glBindTexture(GL_TEXTURE_2D_ARRAY, handle)
        shader.setTexture("$name[$index]", index)
    }


    private fun load(texture: Texture) {
        if (texture.state != TextureStates.LOADED) texture.load(context)
        val pixel = 1.0f / resolution
        val size = texture.size

        val uvEnd = if (size.x == resolution && size.y == resolution) null else Vec2(size) / resolution
        val array = TextureArrayProperties(uvEnd, resolution, pixel)

        texture.renderData = OpenGLTextureData(this.index, textureIndex++, uvEnd, -1)
        texture.array = array

    }

    override fun load(latch: AbstractLatch) {
        for (texture in textures) {
            load(texture)
        }
        state = TextureArrayStates.LOADED
    }

    private companion object {
        const val RESOLUTION = 1024
    }
}
