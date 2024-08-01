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

package de.bixilon.minosoft.protocol.packets.s2c.play.scoreboard.teams

import de.bixilon.minosoft.modding.event.events.scoreboard.team.TeamMemberRemoveEvent
import de.bixilon.minosoft.protocol.network.session.play.PlaySession
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions
import de.bixilon.minosoft.protocol.protocol.buffers.play.PlayInByteBuffer
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType

class RemoveTeamMemberS2CP(
    val name: String,
    buffer: PlayInByteBuffer,
) : TeamsS2CP {
    val members: Set<String> = buffer.readArray(
        if (buffer.versionId < ProtocolVersions.V_14W04A) {
            buffer.readUnsignedShort()
        } else {
            buffer.readVarInt()
        }
    ) { buffer.readString() }.toSet()


    override fun handle(session: PlaySession) {
        val team = session.scoreboard.teams[name] ?: return
        team.members -= members

        for (member in members) {
            val item = session.tabList.name[member] ?: continue
            if (item.team != team) {
                continue
            }
            item.team = team
        }

        session.scoreboard.updateScoreTeams(team, members, true)
        session.events.fire(TeamMemberRemoveEvent(session, team, members))
    }

    override fun log(reducedLog: Boolean) {
        Log.log(LogMessageType.NETWORK_IN, level = LogLevels.VERBOSE) { "Team member remove (name=$name, members=$members)" }
    }
}
