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

package de.bixilon.minosoft.gui.rendering.chunk.queue.meshing

import de.bixilon.minosoft.data.world.positions.ChunkPosition
import de.bixilon.minosoft.gui.rendering.chunk.ChunkRenderer
import de.bixilon.minosoft.gui.rendering.chunk.WorldQueueItem

class ChunkQueueComparator : Comparator<WorldQueueItem> {
    private var sort = 1
    private var position: ChunkPosition = ChunkPosition.EMPTY
    private var height = 0


    fun update(renderer: ChunkRenderer) {
        if (this.position == renderer.cameraChunkPosition && this.height == renderer.cameraSectionHeight) return
        this.position = renderer.cameraChunkPosition
        this.height = renderer.cameraSectionHeight
        sort++
    }

    private fun getDistance(item: WorldQueueItem): Int {
        if (item.sort == this.sort) return item.distance

        val array = item.sectionPosition.array
        val x = array[0] - position.x
        val y = array[1] - height
        val z = array[2] - position.z
        val distance = (x * x + y * y + z * z)

        item.distance = distance
        item.sort = sort

        return distance
    }

    override fun compare(a: WorldQueueItem, b: WorldQueueItem): Int {
        return getDistance(a).compareTo(getDistance(b))
    }
}
