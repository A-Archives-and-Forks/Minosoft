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

package de.bixilon.minosoft.data.chat.signature.command

import de.bixilon.minosoft.commands.nodes.ArgumentNode
import de.bixilon.minosoft.commands.nodes.LiteralNode
import de.bixilon.minosoft.commands.parser.minecraft.message.MessageParser
import de.bixilon.minosoft.commands.stack.CommandStack
import de.bixilon.minosoft.data.chat.signature.SignatureTestUtil
import de.bixilon.minosoft.data.chat.signature.signer.DummyMessageSigner
import de.bixilon.minosoft.protocol.ProtocolUtil.encodeNetwork
import de.bixilon.minosoft.protocol.network.session.play.SessionTestUtil
import org.testng.Assert.assertEquals
import org.testng.annotations.Test
import java.time.Instant

@Test(groups = ["command", "signature"], dependsOnGroups = ["private_key"])
class SignCommandTest {

    fun basicTest() {
        val stack = CommandStack(SessionTestUtil.createSession())
        stack.push(LiteralNode("unsigned"), "unsigned")
        stack.push(ArgumentNode("message", MessageParser), "hi there. I am Moritz!")

        val signature = stack.sign(DummyMessageSigner, SignatureTestUtil.key.pair.private, 0L, Instant.MIN)
        assertEquals(signature.size, 1)
        assertEquals(signature["message"], "hi there. I am Moritz!".encodeNetwork())
    }
}
