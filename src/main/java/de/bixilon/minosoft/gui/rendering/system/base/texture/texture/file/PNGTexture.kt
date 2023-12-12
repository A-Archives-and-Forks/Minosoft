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

package de.bixilon.minosoft.gui.rendering.system.base.texture.texture.file

import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.minosoft.assets.AssetsManager
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.gui.rendering.RenderConstants
import de.bixilon.minosoft.gui.rendering.system.base.texture.TextureStates
import de.bixilon.minosoft.gui.rendering.system.base.texture.TextureTransparencies
import de.bixilon.minosoft.gui.rendering.system.base.texture.array.TextureArrayProperties
import de.bixilon.minosoft.gui.rendering.system.base.texture.data.TextureData
import de.bixilon.minosoft.gui.rendering.system.base.texture.data.buffer.TextureBuffer
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.TextureRenderData
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.file.FileTexture.Companion.readImageProperties
import de.bixilon.minosoft.gui.rendering.textures.TextureAnimation
import de.bixilon.minosoft.gui.rendering.textures.TextureUtil.readTexture
import de.bixilon.minosoft.gui.rendering.textures.properties.ImageProperties
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import java.io.FileNotFoundException


class PNGTexture(
    override val resourceLocation: ResourceLocation,
    override var mipmaps: Int,
) : FileTexture {
    override lateinit var renderData: TextureRenderData
    override var animation: TextureAnimation? = null

    override lateinit var array: TextureArrayProperties
    override var state: TextureStates = TextureStates.DECLARED
        private set
    override lateinit var size: Vec2i
        private set
    override lateinit var transparency: TextureTransparencies
        private set
    override var properties: ImageProperties = ImageProperties.DEFAULT


    override lateinit var data: TextureData


    private fun updateImageProperties(assets: AssetsManager) {
        properties = assets.readImageProperties(resourceLocation) ?: return
    }

    @Synchronized
    override fun load(assets: AssetsManager) {
        if (state == TextureStates.LOADED) return

        updateImageProperties(assets)
        val buffer = readTexture(assets)

        val data = createData(mipmaps, buffer)

        this.size = data.size
        this.transparency = buffer.getTransparency()
        this.data = data

        state = TextureStates.LOADED
    }

    override fun toString(): String {
        return resourceLocation.toString()
    }

    private fun readTexture(assets: AssetsManager): TextureBuffer {
        try {
            return assets[resourceLocation].readTexture()
        } catch (error: Throwable) {
            state = TextureStates.ERRORED
            Log.log(LogMessageType.RENDERING, LogLevels.WARN) { "Can not load texture $resourceLocation: $error" }
            if (error !is FileNotFoundException) {
                Log.log(LogMessageType.RENDERING, LogLevels.VERBOSE) { error }
            }
            return assets[RenderConstants.DEBUG_TEXTURE_RESOURCE_LOCATION].readTexture()
        }
    }

    override fun updateAnimation(size: Vec2i, animation: TextureAnimation) {
        this.animation = animation
        this.size = size
    }

    override fun updateProperties(properties: ImageProperties) {
        this.properties = properties
    }
}
