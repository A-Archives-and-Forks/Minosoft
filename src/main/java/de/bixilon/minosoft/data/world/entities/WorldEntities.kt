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

package de.bixilon.minosoft.data.world.entities

import glm_.vec3.Vec3d
import de.bixilon.kutil.concurrent.lock.RWLock
import de.bixilon.kutil.observer.set.SetObserver.Companion.observedSet
import de.bixilon.minosoft.data.abilities.Gamemodes
import de.bixilon.minosoft.data.entities.entities.Entity
import de.bixilon.minosoft.data.entities.entities.player.PlayerEntity
import de.bixilon.minosoft.data.entities.entities.player.local.LocalPlayerEntity
import de.bixilon.minosoft.data.registries.shapes.shape.Shape
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3dUtil
import de.bixilon.minosoft.protocol.network.session.play.PlaySession
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import java.util.*

class WorldEntities : Iterable<Entity> {
    private val idEntityMap: Int2ObjectOpenHashMap<Entity> = Int2ObjectOpenHashMap()
    private val entityIdMap: Object2IntOpenHashMap<Entity> = Object2IntOpenHashMap()
    private val entityUUIDMap: MutableMap<Entity, UUID> = mutableMapOf()
    private val uuidEntityMap: MutableMap<UUID, Entity> = mutableMapOf()
    val entities: MutableSet<Entity> by observedSet(mutableSetOf())
    private val ticker = EntityTicker(this)

    val lock = RWLock.rwlock()

    val size: Int
        get() = entities.size


    fun add(entityId: Int?, entityUUID: UUID?, entity: Entity) {
        try {
            lock.lock()
            if (entityId != null) {
                idEntityMap[entityId] = entity
                entityIdMap[entity] = entityId
            }
            if (entityUUID != null) {
                uuidEntityMap[entityUUID] = entity
                entityUUIDMap[entity] = entityUUID
            }
            entities += entity
        } finally {
            lock.unlock()
        }
    }

    operator fun get(id: Int?): Entity? {
        if (id == null) return null
        try {
            lock.acquire()
            return idEntityMap[id]
        } finally {
            lock.release()
        }
    }

    fun getId(entity: Entity): Int? {
        try {
            lock.acquire()
            return entityIdMap[entity]
        } finally {
            lock.release()
        }
    }

    operator fun get(uuid: UUID): Entity? {
        try {
            lock.acquire()
            return uuidEntityMap[uuid]
        } finally {
            lock.release()
        }
    }

    fun getUUID(entity: Entity): UUID? {
        try {
            lock.acquire()
            return entityUUIDMap[entity]
        } finally {
            lock.release()
        }
    }

    fun remove(entity: Entity) {
        lock.lock()
        entity._id = null
        entity._uuid = null
        if (entity !is LocalPlayerEntity && !entities.remove(entity)) {
            lock.unlock()
            return
        }
        entityIdMap.remove(entity)?.let { idEntityMap -= it }
        entityUUIDMap.remove(entity)?.let { uuidEntityMap -= it }
        lock.unlock()
    }

    fun remove(entityId: Int) {
        lock.lock()
        val entity = idEntityMap.remove(entityId)
        if (entity == null) {
            lock.unlock()
            return
        }
        entity._id = null
        entity._uuid = null
        if (entity is LocalPlayerEntity) {
            idEntityMap.put(entityId, entity)
            lock.unlock()
            return
        }
        entities -= entity
        entityIdMap.removeInt(entity)
        val uuid = entityUUIDMap.remove(entity)
        if (uuid != null) {
            uuidEntityMap.remove(uuid)
        }
        lock.unlock()
    }

    override fun iterator(): Iterator<Entity> {
        return entities.iterator()
    }

    fun getInRadius(position: Vec3d, distance: Double, check: (Entity) -> Boolean): List<Entity> {
        // ToDo: Improve performance
        val entities: MutableList<Entity> = mutableListOf()
        lock.acquire()

        val distance2 = distance * distance
        for (entity in this) {
            if (Vec3dUtil.distance2(entity.physics.position, position) > distance2) {
                continue
            }
            if (check(entity)) {
                entities += entity
            }
        }
        lock.release()
        return entities
    }

    fun getClosestInRadius(position: Vec3d, distance: Double, check: (Entity) -> Boolean): Entity? {
        val entities = getInRadius(position, distance, check)
        var closestDistance = Double.MAX_VALUE
        var closestEntity: Entity? = null

        for (entity in entities) {
            val currentDistance = Vec3dUtil.distance2(entity.physics.position, position)
            if (currentDistance < closestDistance) {
                closestDistance = currentDistance
                closestEntity = entity
            }
        }

        return closestEntity
    }

    fun isEntityIn(shape: Shape): Boolean {
        try {
            lock.acquire()
            for (entity in this) {
                if (!entity.canRaycast) {
                    continue
                }
                val aabb = entity.physics.aabb

                if (shape.intersects(aabb)) {
                    return true
                }
            }
        } finally {
            lock.release()
        }
        return false
    }

    fun tick() {
        lock.acquire()
        ticker.tick()
        lock.release()
    }

    fun clear(session: PlaySession, local: Boolean = false) {
        this.lock.lock()
        for (entity in this.entities) {
            entity._id = null
            entity._uuid = null
            if (!local && entity is LocalPlayerEntity) continue
            entityIdMap.remove(entity)?.let { idEntityMap.remove(it) }
            entityUUIDMap.remove(entity)?.let { uuidEntityMap.remove(it) }
        }
        val remove = this.entities.toMutableSet()
        remove -= session.player
        this.entities.removeAll(remove)
        this.lock.unlock()
    }

    companion object {
        val CHECK_CLOSEST_PLAYER: (Entity) -> Boolean = check@{
            if (it !is PlayerEntity) {
                return@check false
            }
            if (it.gamemode == Gamemodes.SPECTATOR) {
                return@check false
            }
            return@check true
        }
    }
}
