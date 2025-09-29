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

package de.bixilon.minosoft.gui.rendering.models.block.state.baked

import glm_.func.rad
import glm_.mat3x3.Mat3
import glm_.mat4x4.Mat4
import glm_.vec3.Vec3
import org.junit.jupiter.api.Test
import kotlin.math.abs
import kotlin.math.asin

class SkeletalShadeTest {
    private val DEGREE_90 = 1.5707964f


    fun interpolateShade(normal: Float, max: Float): Float {
        var delta = normal
        if (delta < 0.0f) delta = -delta
        if (delta <= 0.003f) return 0.0f
        if (delta >= 1.0f) return max
        delta = asin(delta) / DEGREE_90 // asin is just defined in |x| <= 1


        return delta * max
    }

    fun getShade(normal: Vec3): Float {
        normal.normalizeAssign() // for testing purposes
        // Take code from skeletal/shade.glsl

        val x = interpolateShade(normal.x, 0.6f)
        val y: Float
        y = if (normal.y < 0.0f) {
            interpolateShade(normal.y, 0.5f)
        } else {
            interpolateShade(normal.y, 1.0f)
        }
        val z = interpolateShade(normal.z, 0.8f)

        return x + y + z
    }

    fun transformNormal(normal: Vec3, transform: Mat4): Vec3 {
        //  return normalize(mat3(transpose(inverse(transform))) * normal);
        return (Mat3(transform) * normal).normalizeAssign()
    }

    @Test
    fun up() {
        assertEquals(1.0f, getShade(Vec3(0, 1, 0)))
    }

    @Test
    fun down() {
        assertEquals(0.5f, getShade(Vec3(0, -1, 0)))
    }

    @Test
    fun north() {
        assertEquals(0.8f, getShade(Vec3(0, 0, 1)))
    }

    @Test
    fun south() {
        assertEquals(0.8f, getShade(Vec3(0, 0, -1)))
    }

    @Test
    fun west() {
        assertEquals(0.6f, getShade(Vec3(1, 0, 0)))
    }

    @Test
    fun east() {
        assertEquals(0.6f, getShade(Vec3(-1, 0, 0)))
    }

    @Test
    fun northWest() {
        assertEquals(0.7f, getShade(Vec3(1, 0, 1)))
    }

    @Test
    fun eastSouth() {
        assertEquals(0.7f, getShade(Vec3(-1, 0, -1)))
    }

    @Test
    fun westUp() {
        assertEquals(0.8f, getShade(Vec3(1, 1, 0)))
    }

    @Test
    fun westDown() {
        assertEquals(0.55f, getShade(Vec3(-1, -1, 0)))
    }

    @Test
    fun westNorthUp() {
        assertEquals(0.94f, getShade(Vec3(1, 1, 1)))
    }


    private fun assertEquals(actual: Vec3, expected: Vec3) {
        val delta = actual - expected
        if (delta.length2() < 0.01f) return
        throw AssertionError("Expected $expected, but got $actual")
    }

    @Test
    fun `transform rotate Y 90deg`() {
        val transform = Mat4().rotateYassign(90.0f.rad)
        val normal = Vec3(0.0f, 1.0f, 0.0f)
        assertEquals(transformNormal(normal, transform), Vec3(0.0f, 1.0f, 0.0f))
    }

    @Test
    fun `transform rotate Y 180deg`() {
        val transform = Mat4().rotateYassign(180.0f.rad)
        val normal = Vec3(0.0f, -1.0f, 0.0f)
        assertEquals(transformNormal(normal, transform), Vec3(0.0f, -1.0f, 0.0f))
    }


    @Test
    fun `transform rotate Y 90deg 2`() {
        val transform = Mat4().rotateYassign(90.0f.rad)
        val normal = Vec3(1.0f, 0.0f, 0.0f)
        assertEquals(transformNormal(normal, transform), Vec3(0.0f, 0.0f, -1.0f))
    }

    @Test
    fun `transform rotate Y 180deg 2`() {
        val transform = Mat4().rotateYassign(180.0f.rad)
        val normal = Vec3(1.0f, 0.0f, 0.0f)
        assertEquals(transformNormal(normal, transform), Vec3(-1.0f, 0.0f, 0.0f))
    }

    @Test
    fun `transform translated`() {
        val transform = Mat4()
            .translateAssign(Vec3(123, 456, 789))
            .rotateYassign(180.0f.rad)
        val normal = Vec3(1.0f, 0.0f, 0.0f)
        assertEquals(transformNormal(normal, transform), Vec3(-1.0f, 0.0f, 0.0f))
    }

    @Test
    fun `transform translated scaled`() {
        val transform = Mat4()
            .scaleAssign(0.4f)
            .translateAssign(Vec3(123, 456, 789))
            .rotateYassign(180.0f.rad)
        val normal = Vec3(1.0f, 0.0f, 0.0f)
        assertEquals(transformNormal(normal, transform), Vec3(-1.0f, 0.0f, 0.0f))
    }

    @Test
    fun `somehow broken in the shader`() {
        val transform = Mat4(-0.93298566, 0.0, 0.09189103, 0.0, -0.09186335, 0.023007425, -0.9327047, 0.0, -0.0022551212, -0.9372176, -0.02289664, 0.0, -455.2743, 95.37174, 618.4536, 1.0)
        val expected = Vec3(1.0f, 0.0f, 0.0f)
        val normal = transformNormal(expected, transform)
        assertEquals(normal, Vec3(-1.0f, 0.0f, 0.0f))
        assertEquals(getShade(normal), 0.6f)
    }


    private fun assertEquals(expected: Float, actual: Float) {
        if (abs(expected - actual) < 0.03f) return
        throw AssertionError("Expected $expected but got $actual")
    }
}
