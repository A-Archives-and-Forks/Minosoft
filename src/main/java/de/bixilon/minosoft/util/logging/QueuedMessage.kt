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

@file:OptIn(ExperimentalTime::class)

package de.bixilon.minosoft.util.logging

import de.bixilon.minosoft.data.text.ChatComponent
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

data class QueuedMessage(
    val message: ChatComponent,
    val time: Instant,
    val type: LogMessageType,
    val level: LogLevels,
    val thread: Thread,
    val prefix: ChatComponent? = null,
)
