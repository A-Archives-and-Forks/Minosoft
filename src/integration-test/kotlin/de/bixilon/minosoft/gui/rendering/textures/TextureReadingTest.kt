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

package de.bixilon.minosoft.gui.rendering.textures

import de.bixilon.kmath.vec.vec2.i.Vec2i
import de.bixilon.kutil.stream.InputStreamUtil.readAll
import de.bixilon.kutil.unsafe.UnsafeUtil.setUnsafeAccessible
import de.bixilon.minosoft.data.text.formatting.color.RGBAColor
import de.bixilon.minosoft.gui.rendering.system.base.texture.data.buffer.RGB8Buffer
import de.bixilon.minosoft.gui.rendering.system.base.texture.data.buffer.RGBA8Buffer
import de.bixilon.minosoft.gui.rendering.system.base.texture.data.buffer.TextureBuffer
import de.bixilon.minosoft.gui.rendering.system.base.texture.data.buffer.TextureBufferFactory
import org.testng.Assert.assertEquals
import org.testng.annotations.Test
import java.io.ByteArrayInputStream
import java.io.InputStream

@Test(groups = ["texture", "assets"])
class TextureReadingTest {
    private val GRAY_GRAY = TextureReadingTest::class.java.getResourceAsStream("/texture_reading/gray_gray.png")!!.readAll()
    private val GRAY_RGB = TextureReadingTest::class.java.getResourceAsStream("/texture_reading/gray_rgb.png")!!.readAll()
    private val SAND = TextureReadingTest::class.java.getResourceAsStream("/texture_reading/sand.png")!!.readAll()

    private val READ_1 = TextureUtil::class.java.getDeclaredMethod("readTexture1", InputStream::class.java, TextureBufferFactory::class.java).apply { setUnsafeAccessible() }
    private val READ_2 = TextureUtil::class.java.getDeclaredMethod("readTexture2", InputStream::class.java, TextureBufferFactory::class.java).apply { setUnsafeAccessible() }

    private fun TextureBuffer.assertGray() {
        assertEquals(size, Vec2i(16, 16))

        assertEquals(getR(0, 0), 0x94)
        assertEquals(getG(0, 0), 0x94)
        assertEquals(getB(0, 0), 0x94)
        assertEquals(getA(0, 0), 0xFF)

        assertEquals(getR(1, 0), 0xC3)
        assertEquals(getR(0, 2), 0xA3)
        assertEquals(getR(0, 4), 0xA2)
    }

    fun `read rgb 1`() {
        val texture = READ_1.invoke(TextureUtil, ByteArrayInputStream(GRAY_RGB), null) as TextureBuffer
        texture.assertGray()
    }

    fun `read rgb 2`() {
        val texture = READ_2.invoke(TextureUtil, ByteArrayInputStream(GRAY_RGB), null) as TextureBuffer
        texture.assertGray()
    }

    @Test(enabled = false)
    fun `read gray 1`() {
        val texture = READ_1.invoke(TextureUtil, ByteArrayInputStream(GRAY_GRAY), null) as TextureBuffer
        texture.assertGray()
    }

    fun `read gray 2`() {
        val texture = READ_2.invoke(TextureUtil, ByteArrayInputStream(GRAY_GRAY), null) as TextureBuffer
        texture.assertGray()
    }

    private fun TextureBuffer.assertSand() {
        assertEquals(getRGBA(0, 0), RGBAColor(0xE7, 0xE4, 0xBB))
        assertEquals(getRGBA(1, 0), RGBAColor(0xDA, 0xCF, 0xA3))
        assertEquals(getRGBA(0, 1), RGBAColor(0xD5, 0xC4, 0x96))
    }

    fun `read1 sand rgba`() {
        val texture = READ_1.invoke(TextureUtil, ByteArrayInputStream(SAND), TextureBufferFactory { RGBA8Buffer(it) }) as TextureBuffer
        texture.assertSand()
    }

    @Test(enabled = false)
    fun `read1 sand rgb`() {
        val texture = READ_1.invoke(TextureUtil, ByteArrayInputStream(SAND), TextureBufferFactory { RGB8Buffer(it) }) as TextureBuffer
        texture.assertSand()
    }

    @Test(enabled = false)
    fun `read2 sand rgba`() {
        val texture = READ_2.invoke(TextureUtil, ByteArrayInputStream(SAND), TextureBufferFactory { RGBA8Buffer(it) }) as TextureBuffer
        texture.assertSand()
    }

    @Test(enabled = false)
    fun `read2 sand rgb`() {
        val texture = READ_2.invoke(TextureUtil, ByteArrayInputStream(SAND), TextureBufferFactory { RGB8Buffer(it) }) as TextureBuffer
        texture.assertSand()
    }
}
