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

package de.bixilon.minosoft.commands.parser.minosoft.account.identifier

import de.bixilon.minosoft.commands.parser.selector.AbstractTarget
import de.bixilon.minosoft.data.accounts.Account

class AccountId(
    val id: String,
) : AbstractTarget<Account> {

    override fun filter(entries: Collection<Account>): List<Account> {
        for (session in entries) {
            if (session.id == id) {
                return listOf(session)
            }
        }
        return emptyList()
    }

    override fun toString(): String {
        return "{$id}"
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (other !is AccountId) {
            return false
        }
        return id == other.id
    }
}
