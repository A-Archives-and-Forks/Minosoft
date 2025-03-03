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

package de.bixilon.minosoft.gui.rendering.chunk.mesher

import de.bixilon.kutil.concurrent.pool.runnable.InterruptableRunnable
import de.bixilon.minosoft.gui.rendering.chunk.ChunkRenderer
import de.bixilon.minosoft.gui.rendering.chunk.WorldQueueItem
import de.bixilon.minosoft.gui.rendering.chunk.mesh.ChunkMeshes
import de.bixilon.minosoft.gui.rendering.chunk.queue.meshing.tasks.MeshPrepareTask
import de.bixilon.minosoft.gui.rendering.chunk.util.ChunkRendererUtil.smallMesh
import de.bixilon.minosoft.protocol.packets.s2c.play.block.chunk.ChunkUtil

class ChunkMesher(
    private val renderer: ChunkRenderer,
) {
    private val solid = SolidSectionMesher(renderer.context)
    private val fluid = FluidSectionMesher(renderer.context)

    private fun mesh(item: WorldQueueItem): ChunkMeshes? {
        if (item.section.blocks.isEmpty) {
            renderer.unload(item)
            return null
        }
        val neighbours = item.chunk.neighbours
        if (!neighbours.complete) {
            renderer.unload(item)
            return null
        }
        val sectionNeighbours = ChunkUtil.getDirectNeighbours(neighbours.neighbours, item.chunk, item.section.sectionHeight)
        val mesh = ChunkMeshes(renderer.context, item.chunkPosition, item.sectionHeight, item.section.smallMesh)
        try {
            solid.mesh(item.chunkPosition, item.sectionHeight, item.chunk, item.section, neighbours.neighbours, sectionNeighbours, mesh)

            if (item.section.blocks.hasFluid) {
                fluid.mesh(item.chunkPosition, item.sectionHeight, item.chunk, item.section, mesh)
            }
        } catch (exception: Exception) {
            mesh.unload()
            throw exception
        }

        return mesh
    }

    private fun mesh(item: WorldQueueItem, runnable: InterruptableRunnable) {
        val mesh = mesh(item) ?: return
        runnable.interruptable = false
        if (Thread.interrupted()) return
        if (mesh.clearEmpty() == 0) {
            return renderer.unload(item)
        }
        mesh.finish()
        item.mesh = mesh
        renderer.loadingQueue.queue(mesh)
    }

    fun tryMesh(item: WorldQueueItem, task: MeshPrepareTask, runnable: InterruptableRunnable) {
        try {
            mesh(item, runnable)
            renderer.meshingQueue.tasks -= task
        } catch (ignored: InterruptedException) {
        } finally {
            task.runnable.interruptable = false
            Thread.interrupted() // clear interrupted flag
            renderer.meshingQueue.work()
        }
    }
}
