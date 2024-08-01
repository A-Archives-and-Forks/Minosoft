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

package de.bixilon.minosoft.protocol.packets.s2c.play.scoreboard.score

import de.bixilon.kutil.collections.CollectionUtil.toSynchronizedMap
import de.bixilon.minosoft.modding.event.events.scoreboard.ScoreboardScoreRemoveEvent
import de.bixilon.minosoft.protocol.network.session.play.PlaySession
import de.bixilon.minosoft.protocol.protocol.buffers.play.PlayInByteBuffer
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType

class RemoveScoreboardScoreS2CP(
    val entity: String,
    val objective: String?,
    buffer: PlayInByteBuffer,
) : ScoreboardScoreS2CP {

    constructor(buffer: PlayInByteBuffer) : this(buffer.readString(), buffer.readOptional { buffer.readString() }, buffer)

    override fun handle(session: PlaySession) {
        val objective = session.scoreboard.objectives[objective] ?: let {
            for ((_, objective) in session.scoreboard.objectives.toSynchronizedMap()) {
                val score = objective.scores.remove(entity) ?: continue

                session.events.fire(ScoreboardScoreRemoveEvent(session, objective, entity))
            }
            return
        }
        val score = objective.scores.remove(entity) ?: return

        session.events.fire(ScoreboardScoreRemoveEvent(session, objective, entity))
    }


    override fun log(reducedLog: Boolean) {
        Log.log(LogMessageType.NETWORK_IN, level = LogLevels.VERBOSE) { "Remove scoreboard score (entity=$entity§r, objective=$objective§r)" }
    }
}
