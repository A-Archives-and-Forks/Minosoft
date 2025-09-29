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

package de.bixilon.minosoft.data.world.iterator

import de.bixilon.kutil.exception.Broken
import de.bixilon.minosoft.data.entities.entities.Entity
import de.bixilon.minosoft.data.registries.blocks.shapes.collision.CollisionPredicate
import de.bixilon.minosoft.data.registries.blocks.shapes.collision.context.CollisionContext
import de.bixilon.minosoft.data.registries.blocks.shapes.collision.context.EmptyCollisionContext
import de.bixilon.minosoft.data.registries.blocks.shapes.collision.context.EntityCollisionContext
import de.bixilon.minosoft.data.registries.blocks.types.fluid.FluidHolder
import de.bixilon.minosoft.data.registries.blocks.types.properties.shape.collision.CollidableBlock
import de.bixilon.minosoft.data.registries.shapes.aabb.AABB
import de.bixilon.minosoft.data.world.World
import de.bixilon.minosoft.data.world.chunk.chunk.Chunk
import de.bixilon.minosoft.data.world.positions.BlockPosition

class WorldIterator(
    private val iterator: Iterator<BlockPosition>,
    private val world: World,
    private var chunk: Chunk? = null,
) : Iterator<BlockPair> {
    private var pair: BlockPair? = null
    private var next: BlockPair? = null
    private var revision = -1


    constructor(aabb: AABB, world: World, chunk: Chunk? = null) : this(aabb.positions(), world, chunk)

    private fun update(): Boolean {
        if (world.chunks.chunks.unsafe.isEmpty()) return false
        if (!iterator.hasNext()) return false

        var chunk = this.chunk
        val minY = world.dimension.minY
        val maxY = world.dimension.maxY

        for (position in iterator) {
            if (position.y !in minY..maxY) continue
            val chunkPosition = position.chunkPosition

            if (chunk == null) {
                if (revision == world.chunks.revision) continue // previously found no chunk, can not find it now
                this.revision = world.chunks.revision
                chunk = world.chunks[chunkPosition] ?: continue
            } else if (chunk.position != chunkPosition) {
                chunk = chunk.neighbours.traceChunk(chunkPosition - chunk.position) ?: continue
            }
            if (this.chunk !== chunk) {
                this.chunk = chunk
            }

            val state = chunk[position.inChunkPosition] ?: continue

            val pair = pair ?: BlockPair(position, state, chunk)
            this.pair = pair

            pair.position = position
            pair.state = state
            pair.chunk = chunk
            this.next = pair

            return true
        }

        return false
    }

    override fun hasNext(): Boolean {
        if (next != null) {
            return true
        }
        return update()
    }

    override fun next(): BlockPair {
        var next = this.next
        if (next != null) {
            this.next = null
            return next
        }

        if (!update()) throw IllegalStateException("There is no future!")
        next = this.next ?: Broken("next is null")

        this.next = null
        return next
    }


    fun hasCollisions(fluids: Boolean = true, predicate: CollisionPredicate? = null) = hasCollisions(EmptyCollisionContext, fluids, predicate)
    fun hasCollisions(entity: Entity, fluids: Boolean = true, predicate: CollisionPredicate? = null) = hasCollisions(EntityCollisionContext(entity), fluids, predicate)
    fun hasCollisions(entity: Entity, aabb: AABB, fluids: Boolean = true, predicate: CollisionPredicate? = null) = hasCollisions(EntityCollisionContext(entity, aabb = aabb), fluids, predicate)

    fun hasCollisions(context: CollisionContext, fluids: Boolean = true, predicate: CollisionPredicate? = null): Boolean {
        val aabb = context.aabb
        for ((position, state) in this) {
            if (fluids && (state.block is FluidHolder)) {
                //   val height = state.block.fluid.getHeight(state)
                //   if (position.y + height > aabb.min.y) {
                return true
                //    }
            }
            if (state.block !is CollidableBlock) continue
            if (predicate != null && !predicate.invoke(state)) continue

            val shape = state.block.getCollisionShape(world.session, context, position, state, null) ?: continue
            if ((shape + position).intersect(aabb)) {
                return true
            }
        }
        return false
    }
}
