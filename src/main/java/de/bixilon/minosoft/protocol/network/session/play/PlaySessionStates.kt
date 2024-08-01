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

package de.bixilon.minosoft.protocol.network.session.play

import de.bixilon.kutil.enums.EnumUtil
import de.bixilon.kutil.enums.ValuesEnum
import de.bixilon.minosoft.data.language.translate.Translatable
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.util.KUtil.toResourceLocation

enum class PlaySessionStates : Translatable {
    WAITING,

    WAITING_MODS,

    LOADING_ASSETS,
    LOADING,

    ESTABLISHING,
    HANDSHAKING,

    LOGGING_IN,
    JOINING,
    DEAD,

    SPAWNING,

    PLAYING,

    DISCONNECTED,
    KICKED,

    ERROR,
    ;

    override val translationKey: ResourceLocation = "minosoft:session.play.state.${name.lowercase()}".toResourceLocation()


    companion object : ValuesEnum<PlaySessionStates> {
        override val VALUES: Array<PlaySessionStates> = values()
        override val NAME_MAP: Map<String, PlaySessionStates> = EnumUtil.getEnumValues(VALUES)

        val PlaySessionStates.disconnected
            get() = this == DISCONNECTED || this == KICKED || this == ERROR
    }
}
