/*
 * Minosoft
 * Copyright (C) 2020-2023 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.protocol

import de.bixilon.kutil.base64.Base64Util.fromBase64
import de.bixilon.kutil.base64.Base64Util.toBase64
import de.bixilon.kutil.json.JsonObject
import de.bixilon.kutil.primitive.LongUtil.toLong
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions
import de.bixilon.minosoft.protocol.protocol.encryption.CryptManager
import de.bixilon.minosoft.protocol.protocol.encryption.CryptManager.encodeNetwork
import de.bixilon.minosoft.util.account.minecraft.key.MinecraftKeyPair
import de.bixilon.minosoft.util.signature.SignatureException
import de.bixilon.minosoft.util.yggdrasil.YggdrasilUtil
import java.nio.charset.StandardCharsets
import java.security.PublicKey
import java.time.Instant
import java.util.*

class PlayerPublicKey(
    val expiresAt: Instant,
    val publicKey: PublicKey,
    val signature: ByteArray,
) {

    constructor(nbt: JsonObject) : this(Instant.ofEpochMilli(nbt["expires_at"].toLong()), CryptManager.getPlayerPublicKey(nbt["key"].toString()), nbt["signature"].toString().fromBase64())

    fun toNbt(): JsonObject {
        return mapOf(
            "expires_at" to expiresAt.epochSecond,
            "key" to publicKey.encoded.toBase64(),
            "signature" to signature.toBase64(),
        )
    }

    fun isExpired(): Boolean {
        return expiresAt.isAfter(Instant.now())
    }

    fun isLegacySignatureCorrect(): Boolean {
        val bytes = (expiresAt.toEpochMilli().toString() + publicKey.encodeNetwork()).toByteArray(StandardCharsets.US_ASCII)

        return YggdrasilUtil.verify(bytes, signature)
    }

    fun isSignatureCorrect(uuid: UUID): Boolean {
        return MinecraftKeyPair.isSignatureCorrect(uuid, expiresAt, publicKey, signature)
    }

    fun requireSignature(versionId: Int, uuid: UUID) {
        if (versionId < ProtocolVersions.V_1_19_1_PRE4) {
            if (!isLegacySignatureCorrect()) throw SignatureException()
        } else {
            if (!isSignatureCorrect(uuid)) throw SignatureException()
        }
    }

    fun validate(versionId: Int, uuid: UUID) {
        if (isExpired()) {
            throw IllegalStateException("Key is expired!")
        }
        requireSignature(versionId, uuid)
    }
}
