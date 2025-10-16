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

package de.bixilon.minosoft.data.registries.dimension

import de.bixilon.kutil.cast.CastUtil.nullCast
import de.bixilon.kutil.primitive.BooleanUtil.toBoolean
import de.bixilon.kutil.primitive.FloatUtil.toFloat
import de.bixilon.kutil.primitive.IntUtil.toInt
import de.bixilon.minosoft.data.registries.dimension.effects.DefaultDimensionEffects
import de.bixilon.minosoft.data.registries.dimension.effects.DimensionEffects
import de.bixilon.minosoft.data.registries.dimension.effects.minecraft.OverworldEffects
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.world.chunk.ChunkSize
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import de.bixilon.minosoft.util.nbt.tag.NBTUtil.get

data class DimensionProperties(
    //   val piglinSafe: Boolean = false,
    //   val natural: Boolean = true,
    val ambientLight: AmbientLight = AmbientLight.DEFAULT,
    //   val respawnAnchorWorks: Boolean = false,
    val light: Boolean = true,
    val skyLight: Boolean = true,
    //   val bedWorks: Boolean = true,
    val effects: DimensionEffects = OverworldEffects,
    //   val hasRaids: Boolean = true,
    // val logicalHeight: Int = DEFAULT_HEIGHT,
    //   val coordinateScale: Double = 0.0,
    val minY: Int = 0,
    //   val hasCeiling: Boolean = false,
    val ultraWarm: Boolean = false,
    val height: Int = DEFAULT_HEIGHT,
    val supports3DBiomes: Boolean = true,
) {
    val maxY = height + minY - 1
    val sections = height / ChunkSize.SECTION_HEIGHT_Y
    val minSection = minY shr 4
    val maxSection = (minSection + sections - 1)


    init {
        check(maxSection >= minSection) { "Upper section can not be lower that the lower section ($minSection >= $maxSection)" }
        check(minSection in ChunkSize.CHUNK_MIN_SECTION..ChunkSize.CHUNK_MAX_SECTION) { "Minimum section out of bounds: $minSection" }
        check(maxSection in ChunkSize.CHUNK_MIN_SECTION..ChunkSize.CHUNK_MAX_SECTION) { "Maximum section out of bounds: $minSection" }
    }


    companion object {
        const val DEFAULT_HEIGHT = 256

        fun deserialize(identifier: ResourceLocation? = null, data: Map<String, Any>): DimensionProperties {
            return DimensionProperties(
                //piglinSafe = data["piglin_safe"]?.toBoolean() ?: false,
                //natural = data["natural"]?.toBoolean() ?: false,
                ambientLight = data["ambient_light"]?.toFloat()?.let { AmbientLight(it) } ?: AmbientLight.DEFAULT,
                //infiniBurn = ResourceLocation(data["infiniburn"].nullCast<String>() ?: "infiniburn_overworld"),
                //respawnAnchorWorks = data["respawn_anchor_works"]?.toBoolean() ?: false,
                skyLight = data["has_skylight", "has_sky_light"]?.toBoolean() ?: true,
                //bedWorks = data["bed_works"]?.toBoolean() ?: false,
                effects = data["effects"].nullCast<String>()?.let { DefaultDimensionEffects[it.toResourceLocation()] } ?: identifier?.let { DefaultDimensionEffects[it] } ?: OverworldEffects,
                //hasRaids = data["has_raids"]?.toBoolean() ?: false,
                // logicalHeight = data["logical_height"]?.toInt() ?: DEFAULT_MAX_Y,
                //coordinateScale = data["coordinate_scale"].nullCast() ?: 0.0,
                minY = data["min_y"]?.toInt() ?: 0,
                //hasCeiling = data["has_ceiling"]?.toBoolean() ?: false,
                ultraWarm = data["ultrawarm"]?.toBoolean() ?: false,
                height = data["height"]?.toInt() ?: DEFAULT_HEIGHT,
                supports3DBiomes = data["supports_3d_biomes"]?.toBoolean() ?: true,
            )
        }
    }
}
