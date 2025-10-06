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

package de.bixilon.minosoft.data.chat.signature.signer

import de.bixilon.minosoft.data.chat.signature.LastSeenMessageList
import de.bixilon.minosoft.data.chat.signature.signer.MessageSigningUtil.getJsonSignatureBytes
import de.bixilon.minosoft.data.chat.signature.signer.MessageSigningUtil.update
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions
import de.bixilon.minosoft.protocol.protocol.buffers.OutByteBuffer
import de.bixilon.minosoft.protocol.protocol.encryption.CryptManager
import de.bixilon.minosoft.protocol.versions.Version
import de.bixilon.minosoft.util.KUtil.sha256
import java.security.PrivateKey
import java.time.Instant
import java.util.*

class MessageSigner2(
    private val version: Version,
) : MessageSigner {
    private var previous: ByteArray? = null

    override fun signMessage(privateKey: PrivateKey, message: String, preview: ChatComponent?, salt: Long, sender: UUID, time: Instant, lastSeen: LastSeenMessageList): ByteArray {
        val signature = CryptManager.createSignature(version)

        signature.initSign(privateKey)


        val buffer = OutByteBuffer()
        buffer.writeLong(salt)
        buffer.writeLong(time.epochSecond)
        if (version.versionId >= ProtocolVersions.V_1_19_2) { // ToDo: This changed somewhere after 1.19.1-pre5
            buffer.writeBareString(message)
        } else {
            buffer.writeBareByteArray(message.getJsonSignatureBytes())
        }

        if (version.versionId >= ProtocolVersions.V_1_19_1_PRE5) {
            buffer.writeByte(0x46)
            // ToDo: send preview text (optional)

            for (entry in lastSeen.entries) {
                buffer.writeByte(0x46)
                buffer.writeUUID(entry.sender)
                buffer.writeBareByteArray(entry.signature)
            }
        }
        val hash = buffer.toArray().sha256()

        previous?.let { signature.update(it) }
        signature.update(sender)
        signature.update(hash)

        return signature.sign()
    }
}
