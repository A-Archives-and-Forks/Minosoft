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

package de.bixilon.minosoft.protocol.packets.s2c.play.bossbar

import de.bixilon.minosoft.modding.event.events.bossbar.BossbarTitleSetEvent
import de.bixilon.minosoft.protocol.network.session.play.PlaySession
import de.bixilon.minosoft.protocol.protocol.buffers.play.PlayInByteBuffer
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import java.util.*

class TitleBossbarS2CP(
    val uuid: UUID,
    buffer: PlayInByteBuffer,
) : BossbarS2CP {
    val title = buffer.readNbtChatComponent()

    override fun handle(session: PlaySession) {
        val bossbar = session.bossbarManager.bossbars[uuid] ?: return

        if (bossbar.title == title) {
            return
        }
        bossbar.title = title

        session.events.fire(BossbarTitleSetEvent(session, uuid, bossbar))
    }

    override fun log(reducedLog: Boolean) {
        if (reducedLog) {
            // servers have sometimes "animated" bossbars
            return
        }
        Log.log(LogMessageType.NETWORK_IN, LogLevels.VERBOSE) { "Bossbar name set (uuid=$uuid, title=\"$title\")" }
    }
}
