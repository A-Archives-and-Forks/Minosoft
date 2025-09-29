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

package de.bixilon.minosoft.util.crash.section

import de.bixilon.kutil.time.TimeUtil.format1
import de.bixilon.kutil.time.TimeUtil.millis
import java.text.SimpleDateFormat
import java.time.Instant

class GeneralSection(notes: String) : CrashSection(
    "General information", arrayOf(
        "Time" to SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format1(Instant.now()) + "(${millis() / 1000L})",
        "Thread" to Thread.currentThread().name,
        "Notes" to notes,
    )
)
