/*
 * Minosoft
 * Copyright (C) 2020-2024 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.data.registries.misc.event.game.handler.gradients

import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.registries.misc.event.game.GameEventHandler
import de.bixilon.minosoft.data.world.weather.WorldWeather
import de.bixilon.minosoft.protocol.network.session.play.PlaySession
import de.bixilon.minosoft.util.KUtil.toResourceLocation

object RainGradientSetHandler : GameEventHandler {
    override val identifier: ResourceLocation = "minecraft:rain_gradient_set".toResourceLocation()

    override fun handle(data: Float, session: PlaySession) {
        val weather = session.world.weather
        session.world.weather = WorldWeather(rain = data, thunder = if (data <= 0.0f) 0.0f else weather.thunder)
    }
}
