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

package de.bixilon.minosoft.commands.nodes

import de.bixilon.kutil.reflection.ReflectionUtil.forceSet
import de.bixilon.kutil.reflection.ReflectionUtil.getFieldOrNull
import de.bixilon.minosoft.commands.stack.CommandStack
import de.bixilon.minosoft.commands.suggestion.Suggestion
import de.bixilon.minosoft.commands.util.CommandReader
import de.bixilon.minosoft.data.chat.signature.signer.DummyMessageSigner
import de.bixilon.minosoft.modding.event.master.EventMaster
import de.bixilon.minosoft.protocol.network.network.client.test.TestNetwork
import de.bixilon.minosoft.protocol.network.session.play.PacketTestUtil.assertPacket
import de.bixilon.minosoft.protocol.network.session.play.PlaySession
import de.bixilon.minosoft.protocol.network.session.play.util.SessionUtil
import de.bixilon.minosoft.protocol.packets.c2s.play.chat.ChatMessageC2SP
import de.bixilon.minosoft.protocol.versions.Versions
import de.bixilon.minosoft.terminal.cli.CLI
import de.bixilon.minosoft.test.ITUtil.allocate
import org.testng.Assert.assertEquals
import org.testng.Assert.assertTrue
import org.testng.annotations.Test
import java.security.SecureRandom

@Test(groups = ["brigadier"])
class ChatNodeTest {
    private var cmd = 0
    private var cmi = 0
    private val root = SessionNode().apply { addChild(LiteralNode("cmd", executor = { cmd++ })) }

    init {
        CLI.commands.addChild(LiteralNode("cmi", executor = { cmi++ }))
    }

    private fun create(cli: Boolean): ChatNode {
        return ChatNode("", allowCLI = cli)
    }

    private fun ChatNode.execute(command: String): CommandStack {
        val session = PlaySession::class.java.allocate()
        session::version.forceSet(Versions.AUTOMATIC)
        session::events.forceSet(EventMaster())
        val util = SessionUtil::class.java.allocate()
        util::signer.forceSet(DummyMessageSigner)
        util::class.java.getFieldOrNull("session")!!.forceSet(util, session)
        util::class.java.getFieldOrNull("random")!!.forceSet(util, SecureRandom())
        session::util.forceSet(util)
        session::network.forceSet(TestNetwork())
        session.commands = root
        val stack = CommandStack(session)
        execute(CommandReader(command), stack)
        return stack
    }

    private fun ChatNode.suggest(command: String): Collection<Suggestion> {
        val session = PlaySession::class.java.allocate()
        session.commands = root
        val stack = CommandStack(session)
        return getSuggestions(CommandReader(command), stack)
    }

    fun `normal chat sending`() {
        val node = create(true)
        val stack = node.execute("chat message")
        val packet: ChatMessageC2SP = stack.session.assertPacket(ChatMessageC2SP::class.java)
        assertEquals(packet.message, "chat message")
    }

    fun `command chat sending`() {
        val node = create(true)
        val previous = cmd
        val stack = node.execute("/cmd")
        assertTrue(cmd == previous + 1)
        val packet: ChatMessageC2SP = stack.session.assertPacket(ChatMessageC2SP::class.java) // old version
        assertEquals(packet.message, "/cmd")
    }

    fun `internal execution`() {
        val node = create(true)
        val previous = cmi
        val stack = node.execute(".cmi")
        assertTrue(cmi == previous + 1)
    }

    fun `chat suggestions`() {
        val node = create(true)
        val suggestions = node.suggest("message")
        assertEquals(suggestions.size, 0)
    }

    fun `command suggestions`() {
        val node = create(true)
        val suggestions = node.suggest("/cm")
        assertEquals(suggestions.toSet(), setOf(
            Suggestion(1, "cmd"),
        ))
    }

    fun `internal suggestions`() {
        val node = create(true)
        val suggestions = node.suggest(".cm")
        assertEquals(suggestions.toSet(), setOf(
            Suggestion(1, "cmi"),
        ))
    }

    fun `prefix suggestions`() {
        val node = create(true)
        val suggestions = node.suggest("")
        assertEquals(suggestions.toSet(), setOf(
            Suggestion(0, "."),
            Suggestion(0, "/"),
        ))
    }
}
