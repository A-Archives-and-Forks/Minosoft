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

package de.bixilon.minosoft.gui.rendering.models.loader

import de.bixilon.kutil.cast.CastUtil.nullCast
import de.bixilon.kutil.collections.iterator.async.ConcurrentIterator
import de.bixilon.kutil.collections.map.LockMap
import de.bixilon.kutil.concurrent.pool.ThreadPool
import de.bixilon.kutil.latch.AbstractLatch
import de.bixilon.kutil.reflection.ReflectionUtil.forceSet
import de.bixilon.minosoft.assets.minecraft.MinecraftPackFormat
import de.bixilon.minosoft.assets.util.InputStreamUtil.readJsonObject
import de.bixilon.minosoft.data.registries.blocks.types.Block
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.gui.rendering.models.block.BlockModel
import de.bixilon.minosoft.gui.rendering.models.block.BlockModelPrototype
import de.bixilon.minosoft.gui.rendering.models.block.state.DirectBlockModel
import de.bixilon.minosoft.gui.rendering.models.loader.ModelFixer.fixPrefix
import de.bixilon.minosoft.gui.rendering.models.loader.ModelLoader.Companion.model
import de.bixilon.minosoft.gui.rendering.models.loader.legacy.CustomModel
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType

class BlockLoader(private val loader: ModelLoader) {
    private val cache: MutableMap<ResourceLocation, BlockModel> = LockMap(HashMap(loader.context.connection.registries.block.size))
    val assets = loader.context.connection.assetsManager
    val version = loader.context.connection.version

    fun loadBlock(name: ResourceLocation): BlockModel? {
        val file = name.blockModel()
        cache[file]?.let { return it }
        val data = assets.getOrNull(file)?.readJsonObject()
        if (data == null) {
            Log.log(LogMessageType.LOADING, LogLevels.WARN) { "Can not find block model $name" }
            return null
        }

        val parent = data["parent"]?.toString()?.toResourceLocation()?.let { loadBlock(it) }


        val model = BlockModel.deserialize(parent, data)
        cache[file] = model
        return model
    }

    fun loadState(block: Block, file: ResourceLocation): BlockModelPrototype? {
        val data = assets.getOrNull(file)?.readJsonObject() ?: return null

        val model = DirectBlockModel.deserialize(this, block, data)
        return model?.load(loader.context.textures)
    }

    fun loadState(block: Block): BlockModelPrototype? {
        if (block is CustomModel) {
            return block.loadModel(this, version)
        }
        return loadState(block, block.identifier.blockState())
    }

    fun load(latch: AbstractLatch?) {
        val iterator = ConcurrentIterator(loader.context.connection.registries.block.spliterator(), priority = ThreadPool.HIGH) // TODO: ConcurrentIterator
        iterator.iterate {
            if (it.model != null) return@iterate // model already set
            val prototype: BlockModelPrototype
            try {
                prototype = loadState(it) ?: return@iterate
            } catch (error: Exception) {
                Log.log(LogMessageType.RENDERING, LogLevels.WARN) { "Can not load block model for block $it: $error" }
                Log.log(LogMessageType.RENDERING, LogLevels.VERBOSE) { error }
                return@iterate
            }

            it.model = prototype
        }
    }

    fun bake(latch: AbstractLatch?) {
        val context = loader.context
        val iterator = ConcurrentIterator(loader.context.connection.registries.block.spliterator(), priority = ThreadPool.HIGH) // TODO: ConcurrentIterator
        iterator.iterate {
            val prototype = it.model.nullCast<BlockModelPrototype>() ?: return@iterate
            it.model = null

            prototype.bake(context, it)
        }
    }

    fun cleanup() {
        this::cache.forceSet(null)
    }

    fun fixTexturePath(name: ResourceLocation): ResourceLocation {
        return ResourceLocation(name.namespace, name.path.fixPrefix(loader.packFormat, MinecraftPackFormat.FLATTENING, "blocks/", "block/"))
    }

    private fun ResourceLocation.blockModel(): ResourceLocation {
        return this.prefix("block/").model()
    }


    companion object {

        fun ResourceLocation.blockState(): ResourceLocation {
            return ResourceLocation(this.namespace, "blockstates/" + this.path + ".json")
        }
    }
}
