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

package de.bixilon.minosoft.gui.rendering.system.opengl

import de.bixilon.kotlinglm.mat4x4.Mat4
import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.kotlinglm.vec4.Vec4
import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.kutil.exception.ExceptionUtil.catchAll
import de.bixilon.kutil.stream.InputStreamUtil.readAsString
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.text.formatting.color.RGBAColor
import de.bixilon.minosoft.data.text.formatting.color.RGBColor
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.exceptions.ShaderLinkingException
import de.bixilon.minosoft.gui.rendering.exceptions.ShaderLoadingException
import de.bixilon.minosoft.gui.rendering.system.base.buffer.uniform.UniformBuffer
import de.bixilon.minosoft.gui.rendering.system.base.shader.NativeShader
import de.bixilon.minosoft.gui.rendering.system.base.shader.code.glsl.GLSLShaderCode
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import it.unimi.dsi.fastutil.ints.IntArrayList
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import org.lwjgl.opengl.GL11.GL_FALSE
import org.lwjgl.opengl.GL43.*
import org.lwjgl.system.MemoryUtil
import java.io.FileNotFoundException

class OpenGLNativeShader(
    override val context: RenderContext,
    private val vertex: ResourceLocation,
    private val geometry: ResourceLocation?,
    private val fragment: ResourceLocation,
    val system: OpenGLRenderSystem = context.system.unsafeCast(),
) : NativeShader {
    override var loaded: Boolean = false
        private set
    override val defines: MutableMap<String, Any> = mutableMapOf()
    private var handler = -1
    private val uniformLocations: Object2IntOpenHashMap<String> = Object2IntOpenHashMap()

    private fun load(file: ResourceLocation, type: ShaderType, code: String?): Int {
        val code = GLSLShaderCode(context, code ?: context.session.assetsManager[file].readAsString())
        system.log { "Compiling shader $file" }

        code.defines += defines
        code.defines["SHADER_TYPE_${type.name}"] = ""
        for (hack in system.vendor.hacks) {
            code.defines[hack.name] = ""
        }

        val program = glCreateShader(type.native)
        if (program.toLong() == MemoryUtil.NULL) {
            throw ShaderLoadingException()
        }

        val glsl = code.code
        glShaderSource(program, glsl)

        glCompileShader(program)

        if (glGetShaderi(program, GL_COMPILE_STATUS) == GL_FALSE) {
            throw ShaderLoadingException("Can not load shader: $file:\n" + glGetShaderInfoLog(program), glsl)
        }

        return program
    }

    override fun load() {
        val geometryCode = geometry?.let { catchAll { context.session.assetsManager[it].readAsString() } }
        if (geometryCode != null) {
            defines["HAS_GEOMETRY_SHADER"] = " "
        }
        handler = glCreateProgram()

        if (handler.toLong() == MemoryUtil.NULL) {
            throw ShaderLoadingException()
        }

        val programs = IntArrayList(3)

        programs += load(vertex, ShaderType.VERTEX, null)
        try {
            geometry?.let { programs += load(it, ShaderType.GEOMETRY, geometryCode) }
        } catch (_: FileNotFoundException) {
        }
        programs += load(fragment, ShaderType.FRAGMENT, null)

        for (program in programs) {
            glAttachShader(handler, program)
        }

        glLinkProgram(handler)

        glValidateProgram(handler)

        if (glGetProgrami(handler, GL_LINK_STATUS) == GL_FALSE) {
            throw ShaderLinkingException("Can not link shaders: $vertex with $geometry with ${fragment}: \n ${glGetProgramInfoLog(handler)}")
        }
        for (program in programs) {
            glDeleteShader(program)
        }
        loaded = true
    }

    override fun unload() {
        check(loaded) { "Not loaded!" }
        glDeleteProgram(this.handler)
        loaded = false
        this.handler = -1
    }

    override fun reload() {
        unload()
        load()
    }


    private fun getUniformLocation(uniformName: String): Int {
        val location = uniformLocations.getOrPut(uniformName) {
            val location = glGetUniformLocation(handler, uniformName)
            if (location < 0) {
                val error = "No uniform named $uniformName in $this, maybe you use something that has been optimized out? Check your shader code!"
                if (!context.profile.advanced.allowUniformErrors) {
                    throw IllegalArgumentException(error)
                }
                Log.log(LogMessageType.RENDERING, LogLevels.WARN, error)
            }
            return@getOrPut location
        }
        return location
    }

    override fun setFloat(uniformName: String, value: Float) {
        glUniform1f(getUniformLocation(uniformName), value)
    }

    override fun setInt(uniformName: String, value: Int) {
        glUniform1i(getUniformLocation(uniformName), value)
    }

    override fun setUInt(uniformName: String, value: Int) {
        glUniform1ui(getUniformLocation(uniformName), value)
    }

    override fun setBoolean(uniformName: String, boolean: Boolean) {
        setInt(uniformName, if (boolean) 1 else 0)
    }

    override fun setMat4(uniformName: String, mat4: Mat4) {
        glUniformMatrix4fv(getUniformLocation(uniformName), false, mat4.array)
    }

    override fun setVec2(uniformName: String, vec2: Vec2) {
        glUniform2f(getUniformLocation(uniformName), vec2.x, vec2.y)
    }

    override fun setVec3(uniformName: String, vec3: Vec3) {
        glUniform3f(getUniformLocation(uniformName), vec3.x, vec3.y, vec3.z)
    }

    override fun setVec4(uniformName: String, vec4: Vec4) {
        glUniform4f(getUniformLocation(uniformName), vec4.x, vec4.y, vec4.z, vec4.w)
    }

    override fun setArray(uniformName: String, array: Array<*>) {
        for ((i, value) in array.withIndex()) {
            this["$uniformName[$i]"] = value
        }
    }

    override fun setIntArray(uniformName: String, array: IntArray) {
        for ((i, value) in array.withIndex()) {
            this.setInt("$uniformName[$i]", value)
        }
    }

    override fun setUIntArray(uniformName: String, array: IntArray) {
        for ((i, value) in array.withIndex()) {
            this.setUInt("$uniformName[$i]", value)
        }
    }

    override fun setCollection(uniformName: String, collection: Collection<*>) {
        for ((i, value) in collection.withIndex()) {
            this["$uniformName[$i]"] = value
        }
    }

    override fun setRGBColor(uniformName: String, color: RGBColor) {
        setVec4(uniformName, Vec4(color.redf, color.greenf, color.bluef, 1.0f))
    }

    override fun setRGBAColor(uniformName: String, color: RGBAColor) {
        setVec4(uniformName, Vec4(color.redf, color.greenf, color.bluef, color.alphaf))
    }

    override fun setTexture(uniformName: String, textureId: Int) {
        glUniform1i(getUniformLocation(uniformName), textureId)
    }

    override fun setUniformBuffer(uniformName: String, uniformBuffer: UniformBuffer) {
        val index = uniformLocations.getOrPut(uniformName) {
            val index = glGetUniformBlockIndex(handler, uniformName)
            if (index < 0) {
                throw IllegalArgumentException("No uniform buffer called $uniformName")
            }
            return@getOrPut index
        }
        glUniformBlockBinding(handler, index, uniformBuffer.bindingIndex)
    }

    fun unsafeUse() {
        glUseProgram(handler)
    }

    override val log: String
        get() = TODO()


    override fun toString(): String {
        return "OpenGLShader: $vertex:$geometry:$fragment"
    }

    private enum class ShaderType(
        val native: Int,
    ) {
        GEOMETRY(GL_GEOMETRY_SHADER),
        VERTEX(GL_VERTEX_SHADER),
        FRAGMENT(GL_FRAGMENT_SHADER),
        COMPUTE(GL_COMPUTE_SHADER),
    }
}
