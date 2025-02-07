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

package de.bixilon.minosoft.config.profile.profiles.rendering.light

import de.bixilon.minosoft.config.profile.delegate.primitive.BooleanDelegate
import de.bixilon.minosoft.config.profile.delegate.primitive.FloatDelegate
import de.bixilon.minosoft.config.profile.profiles.rendering.RenderingProfile

class LightC(profile: RenderingProfile) {

    /**
     * Changes the gamma value of the light map
     * In original minecraft this setting is called brightness
     * Must be non-negative and may not exceed 1
     */
    var gamma by FloatDelegate(profile, 0.0f, arrayOf(0.0f..1.0f))

    /**
     * Makes everything bright
     */
    var fullbright by BooleanDelegate(profile, false)

    /**
     * Ambient occlusion effect (corners are darker)
     */
    var ambientOcclusion by BooleanDelegate(profile, true)
}
