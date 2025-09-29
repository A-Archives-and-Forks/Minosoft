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

package de.bixilon.minosoft.protocol.packets.s2c.play.scoreboard.teams

import de.bixilon.kutil.bit.BitByte.isBitMask
import de.bixilon.minosoft.data.scoreboard.NameTagVisibilities
import de.bixilon.minosoft.data.scoreboard.TeamCollisionRules
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.data.text.formatting.color.ChatColors
import de.bixilon.minosoft.data.text.formatting.color.RGBColor
import de.bixilon.minosoft.modding.event.events.scoreboard.team.TeamUpdateEvent
import de.bixilon.minosoft.protocol.network.session.play.PlaySession
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions
import de.bixilon.minosoft.protocol.protocol.buffers.play.PlayInByteBuffer
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType

class UpdateTeamS2CP(
    val name: String,
    buffer: PlayInByteBuffer,
) : TeamsS2CP {
    val displayName = buffer.readNbtChatComponent()
    lateinit var prefix: ChatComponent
        private set
    lateinit var suffix: ChatComponent
        private set
    var friendlyFire = false
        private set
    var canSeeInvisibleTeam = false
        private set
    var collisionRule = TeamCollisionRules.NEVER
        private set
    var nameTagVisibility = NameTagVisibilities.ALWAYS
        private set
    var color: RGBColor? = null
        private set

    init {
        if (buffer.versionId < ProtocolVersions.V_18W01A) {
            this.prefix = buffer.readChatComponent()
            this.suffix = buffer.readChatComponent()
        }

        if (buffer.versionId < ProtocolVersions.V_16W06A) { // ToDo
            setLegacyFriendlyFire(buffer.readUnsignedByte())
        } else {
            buffer.readUnsignedByte().let {
                this.friendlyFire = it.isBitMask(0x01)
                this.canSeeInvisibleTeam = it.isBitMask(0x02)
            }
        }

        if (buffer.versionId >= ProtocolVersions.V_14W07A) {
            this.nameTagVisibility = NameTagVisibilities[buffer.readString()]
            if (buffer.versionId >= ProtocolVersions.V_16W06A) { // ToDo
                this.collisionRule = TeamCollisionRules[buffer.readString()]
            }
            if (buffer.versionId < ProtocolVersions.V_18W01A) {
                this.color = ChatColors.getOrNull(buffer.readUnsignedByte())?.rgb()
            } else {
                this.color = ChatColors.getOrNull(buffer.readVarInt())?.rgb()
            }
        }

        if (buffer.versionId >= ProtocolVersions.V_18W20A) {
            prefix = buffer.readNbtChatComponent()
            suffix = buffer.readNbtChatComponent()
        }
    }

    private fun setLegacyFriendlyFire(data: Int) {
        when (data) {
            0 -> this.friendlyFire = false
            1 -> this.friendlyFire = true
            2 -> {
                this.friendlyFire = false
                this.canSeeInvisibleTeam = true
            }
        }
        // ToDo: seeFriendlyInvisibles for case 0 and 1
    }


    override fun handle(session: PlaySession) {
        val team = session.scoreboard.teams[name] ?: return

        val formatting = team.formatting
        formatting.name = displayName; formatting.prefix = prefix; formatting.suffix = suffix; formatting.color = color

        val visibility = team.visibility
        visibility.invisibleTeam = canSeeInvisibleTeam; visibility.name = nameTagVisibility

        team.friendlyFire = friendlyFire
        team.collisions = collisionRule

        session.events.fire(TeamUpdateEvent(session, team))
    }

    override fun log(reducedLog: Boolean) {
        if (reducedLog) {
            return
        }
        Log.log(LogMessageType.NETWORK_IN, level = LogLevels.VERBOSE) { "Team update (name=$name, prefix=$prefix, suffix=$suffix, friendlyFire=$friendlyFire, canSeeInvisibleTeam=$canSeeInvisibleTeam, collisionRule=$collisionRule, nameTagVisibility=$nameTagVisibility, color=$color)" }
    }
}
