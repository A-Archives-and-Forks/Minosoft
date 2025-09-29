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

package de.bixilon.minosoft.util.collections.floats

import de.bixilon.kutil.collections.primitive.floats.AbstractFloatList
import de.bixilon.kutil.collections.primitive.floats.HeapArrayFloatList
import de.bixilon.kutil.exception.Broken
import de.bixilon.kutil.reflection.ReflectionUtil.field
import de.bixilon.kutil.reflection.ReflectionUtil.getFieldOrNull
import de.bixilon.minosoft.util.collections.floats.FloatListUtil.copy
import org.lwjgl.system.MemoryUtil.memAllocFloat
import org.lwjgl.system.MemoryUtil.memFree
import java.nio.FloatBuffer

class FragmentedArrayFloatList(
    growStep: Int = FloatListUtil.DEFAULT_INITIAL_SIZE,
) : AbstractFloatList(), DirectArrayFloatList {
    var complete: MutableList<FloatBuffer> = ArrayList()
    var incomplete: MutableList<FloatBuffer> = ArrayList()
    override var limit: Int = 0
        private set
    override var size: Int = 0
        private set
    override val isEmpty: Boolean
        get() = size == 0
    private var unloaded = false

    private val nextGrowStep = when {
        growStep <= 0 -> FloatListUtil.DEFAULT_INITIAL_SIZE
        growStep <= 128 -> 128
        else -> growStep
    }

    private var output: FloatArray? = null
    private var buffer: FloatBuffer? = null


    override fun ensureSize(needed: Int) {
        if (needed == 0) {
            return
        }
        tryGrow(needed)
    }

    private fun tryGrow(size: Int): FloatBuffer {
        if (limit - this.size >= size) {
            return this.incomplete[0]
        }
        return grow(size)
    }

    private fun grow(size: Int): FloatBuffer {
        val grow = if (nextGrowStep < size) {
            (size / nextGrowStep + 1) * nextGrowStep
        } else {
            nextGrowStep
        }
        return forceGrow(grow)
    }

    private fun forceGrow(size: Int): FloatBuffer {
        val buffer = memAllocFloat(size)
        incomplete += buffer
        limit += size
        return buffer
    }

    override fun add(value: Float) {
        checkFinished()
        val buffer = tryGrow(1)
        buffer.put(value)
        size += 1
        tryPush(buffer)
        invalidateOutput()
    }

    private fun batchAdd(value: Float, buffer: FloatBuffer, left: Int): FloatBuffer {
        buffer.put(value)
        if (!tryPush(buffer)) {
            return buffer
        }
        if (left == 0) return buffer
        return this.incomplete.firstOrNull() ?: grow(left)
    }

    override fun add(value1: Float, value2: Float) {
        checkFinished()
        var buffer = tryGrow(1)
        buffer = batchAdd(value1, buffer, 1)
        batchAdd(value2, buffer, 0)

        size += 2
        invalidateOutput()
    }

    override fun add(value1: Float, value2: Float, value3: Float) {
        checkFinished()
        var buffer = tryGrow(1)
        buffer = batchAdd(value1, buffer, 2)
        buffer = batchAdd(value2, buffer, 1)
        batchAdd(value3, buffer, 0)

        size += 3
        invalidateOutput()
    }

    override fun add(value1: Float, value2: Float, value3: Float, value4: Float) {
        checkFinished()
        var buffer = tryGrow(1)
        buffer = batchAdd(value1, buffer, 3)
        buffer = batchAdd(value2, buffer, 2)
        buffer = batchAdd(value3, buffer, 1)
        batchAdd(value4, buffer, 0)

        size += 4
        invalidateOutput()
    }

    override fun add(value1: Float, value2: Float, value3: Float, value4: Float, value5: Float) {
        checkFinished()
        var buffer = tryGrow(1)
        buffer = batchAdd(value1, buffer, 4)
        buffer = batchAdd(value2, buffer, 3)
        buffer = batchAdd(value3, buffer, 2)
        buffer = batchAdd(value4, buffer, 1)
        batchAdd(value5, buffer, 0)

        size += 5
        invalidateOutput()
    }

    override fun add(value1: Float, value2: Float, value3: Float, value4: Float, value5: Float, value6: Float) {
        checkFinished()
        size += 6
        invalidateOutput()

        var buffer = this.incomplete.firstOrNull() ?: tryGrow(1)
        val left = buffer.limit() - buffer.position()
        if (left >= 6) {
            buffer.put(value1); buffer.put(value2); buffer.put(value3); buffer.put(value4); buffer.put(value5); buffer.put(value6)
            if (left == 6) {
                tryPush(buffer)
            }
        } else {
            buffer = batchAdd(value1, buffer, 5)
            buffer = batchAdd(value2, buffer, 4)
            buffer = batchAdd(value3, buffer, 3)
            buffer = batchAdd(value4, buffer, 2)
            buffer = batchAdd(value5, buffer, 1)
            batchAdd(value6, buffer, 0)
        }
    }


    override fun add(value1: Float, value2: Float, value3: Float, value4: Float, value5: Float, value6: Float, value7: Float) {
        checkFinished()
        size += 7
        invalidateOutput()

        var buffer = this.incomplete.firstOrNull() ?: tryGrow(1)
        val left = buffer.limit() - buffer.position()
        if (left >= 7) {
            buffer.put(value1); buffer.put(value2); buffer.put(value3); buffer.put(value4); buffer.put(value5); buffer.put(value6); buffer.put(value7)
            if (left == 7) {
                tryPush(buffer)
            }
        } else {
            buffer = batchAdd(value1, buffer, 6)
            buffer = batchAdd(value2, buffer, 5)
            buffer = batchAdd(value3, buffer, 4)
            buffer = batchAdd(value4, buffer, 3)
            buffer = batchAdd(value5, buffer, 2)
            buffer = batchAdd(value6, buffer, 1)
            batchAdd(value7, buffer, 0)
        }
    }

    private inline fun tryPush(fragment: FloatBuffer): Boolean {
        if (fragment.position() != fragment.limit()) {
            return false
        }
        complete += fragment
        incomplete -= fragment
        return true
    }

    fun add(array: FloatArray, offset: Int, length: Int) {
        if (offset + length > array.size) throw IndexOutOfBoundsException("Index ${offset + length} out of bounds!")
        if (length == 0) return
        checkFinished()
        invalidateOutput()


        var offset = offset
        var left = length

        var fragmentOffset = 0 // avoid ConcurrentModificationException when pushing list
        for (fragmentIndex in 0 until incomplete.size) {
            val fragment = incomplete[fragmentIndex + fragmentOffset]
            val capacity = fragment.limit() - fragment.position()
            val copy = minOf(left, capacity)
            fragment.put(array, offset, copy)

            offset += copy
            left -= copy

            if (tryPush(fragment)) fragmentOffset-- // fragment not anymore in the list (shifted)
            this.size += copy // tryPush needs the current size

            if (left == 0) break
        }

        if (left > 0) {
            val next = tryGrow(left)
            next.put(array, offset, left)
            tryPush(next)
            this.size += left
        }
    }

    override fun add(array: FloatArray) = add(array, 0, array.size)

    override fun add(buffer: FloatBuffer) {
        if (buffer.position() == 0) return
        checkFinished()
        invalidateOutput()

        var offset = 0
        val position = buffer.position()
        var indexOffset = 0
        for (index in 0 until incomplete.size) {
            val fragment = incomplete[index + indexOffset]
            val remaining = fragment.limit() - fragment.position()
            val copy = minOf(position - offset, remaining)
            buffer.copy(offset, fragment, fragment.position(), copy)
            offset += copy
            if (tryPush(fragment)) indexOffset--

            if (position == offset) {
                // everything copied
                size += position
                // verifyPosition()
                return
            }
        }
        size += offset
        val length = position - offset
        val next = tryGrow(length)
        buffer.copy(offset, next, 0, length)
        next.position(length)
        size += length
        tryPush(next)
        // verifyPosition()
    }

    override fun add(floatList: AbstractFloatList) {
        when (floatList) {
            is FragmentedArrayFloatList -> {
                // TODO: add dirty method (just adding their fragments to our list of fragments)
                floatList.forEach(this::add)
            }

            is DirectArrayFloatList -> add(floatList.toBuffer())
            is HeapArrayFloatList -> add(HEAP_DATA[floatList], 0, floatList.size)
            else -> add(floatList.toArray())
        }
        invalidateOutput()
    }

    private fun checkOutputArray(): FloatArray {
        this.output?.let { return it }
        val output = FloatArray(size)
        var offset = 0
        forEach {
            val position = it.position()
            it.position(0)
            it.get(output, offset, position)
            offset += position
            it.position(position)
        }
        this.output = output
        return output
    }

    override fun toArray(): FloatArray {
        return checkOutputArray()
    }

    override fun unload() {
        check(!unloaded) { "Already unloaded!" }
        unloaded = true
        finished = true // Is unloaded
        forEach { memFree(it) }
        this.output = null
        val buffer = this.buffer
        if (buffer != null) {
            memFree(buffer)
            this.buffer = null
        }
        complete.clear()
        incomplete.clear()
    }

    override fun clear() {
        size = 0
        forEach { it.clear() }
        incomplete.addAll(0, complete)
        complete.clear()
        invalidateOutput()
    }

    override fun finish() {
        finished = true
        if (complete.size + incomplete.size == 0) {
            return
        }
        for (fragment in incomplete) {
            if (fragment.position() == 0) {
                continue
            }
            val next = memAllocFloat(fragment.position())
            fragment.copy(next)
            complete += next
        }
    }

    protected fun finalize() {
        if (unloaded) {
            return
        }
        unload()
    }

    private fun invalidateOutput() {
        if (this.output != null) {
            this.output = null
        }

        if (this.buffer != null) {
            this.buffer = null
        }
    }

    override fun toBuffer(): FloatBuffer {
        this.buffer?.let { return it }
        if (complete.size + incomplete.size == 1) {
            return complete.firstOrNull() ?: incomplete.first()
        }
        val buffer = memAllocFloat(size)
        forEach { it.copy(buffer) }
        if (buffer.position() != this.size) {
            // TODO: this should never happen, remove this check
            Broken("Position mismatch: ${buffer.position()}, expected $size")
        }
        this.buffer = buffer
        return buffer
    }

    inline fun forEach(callable: (FloatBuffer) -> Unit) {
        for (buffer in this.complete) {
            callable(buffer)
        }
        for (buffer in this.incomplete) {
            callable(buffer)
        }
    }

    private fun verifyPosition() {
        val expected = size
        var actual = 0
        forEach { actual += it.position() }
        if (expected != actual) {
            Broken("Buffer size mismatch: expected=$expected, actual=$actual")
        }
    }


    companion object {
        private val HEAP_DATA = HeapArrayFloatList::class.java.getFieldOrNull("data")!!.field
    }
}
