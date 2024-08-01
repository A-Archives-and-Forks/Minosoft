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
package de.bixilon.minosoft.protocol.packets.s2c

import de.bixilon.minosoft.protocol.network.network.client.netty.exceptions.WrongSessionTypeException
import de.bixilon.minosoft.protocol.network.session.Session
import de.bixilon.minosoft.protocol.network.session.play.PlaySession
import de.bixilon.minosoft.protocol.network.session.status.StatusSession
import de.bixilon.minosoft.protocol.packets.types.HandleablePacket

interface StatusS2CPacket : S2CPacket, HandleablePacket {

    fun handle(session: StatusSession) = Unit

    fun check(session: StatusSession) = Unit


    override fun handle(session: Session) {
        if (session !is StatusSession) throw WrongSessionTypeException(PlaySession::class.java, session::class.java)
        check(session)
        handle(session)
    }
}
