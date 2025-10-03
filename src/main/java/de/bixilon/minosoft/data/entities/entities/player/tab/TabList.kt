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

package de.bixilon.minosoft.data.entities.entities.player.tab

import de.bixilon.kutil.observer.DataObserver.Companion.observed
import de.bixilon.minosoft.data.entities.entities.player.additional.PlayerAdditional
import de.bixilon.minosoft.data.text.ChatComponent
import java.util.*

class TabList {
    val uuid: MutableMap<UUID, PlayerAdditional> = mutableMapOf()
    val name: MutableMap<String, PlayerAdditional> = mutableMapOf()
    var header by observed(ChatComponent.EMPTY)
    var footer by observed(ChatComponent.EMPTY)


    fun remove(uuid: UUID) {
        val entry = this.uuid.remove(uuid) ?: return
        name.remove(entry.name)
    }
}
