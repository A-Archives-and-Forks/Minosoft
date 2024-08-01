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

package de.bixilon.minosoft.util.crash

import de.bixilon.kutil.exception.ExceptionUtil.catchAll
import de.bixilon.minosoft.properties.MinosoftProperties
import de.bixilon.minosoft.util.crash.section.*

object CrashReportUtil {
    const val INTENT = "    "
    val CRASH_REPORT_COMMENTS = listOf(
        "Let's blame Bixilon for this",
        "But it worked once",
        "It works on my computer",
        "Not a bug, it's a feature",
        "My bad",
        "Whoops",
        "Don't try to crash this!",
        "Makes not sense!",
        "Let's hack the game",
        "You're evil",
        "Maybe in another life.",
        "This sucks",
        "Chill your life",
        "Damn!",
        "Developing is hard.",
        "Please don't kill me for this",
        "Trying my best",
        "That happens when you develop while playing games!",
        "Written while driving in a FlixBus",
        "Coded while traveling in the ICE 272 towards Hamburg-Altona",
        "Sorry, the ICE 693 drive towards Munich was really long",
        "Coded while playing bedwars",
        "I am #1 in bedwars swordless",
        "Der AB kam vor der CD",
        "You can't do this",
        "This message should not be visible...",
        "lmfao",
        "Technoblade never dies", // In memorial to technoblade. RIP Technoblade 30.6.2022
        "Find help at: https://www.youtube.com/watch?v=dQw4w9WgXcQ", // rick
        "//TODO: What the hell is causing this?", // S H&R
        "If this happens, we're in big shit.", // S H&R
        "this is bulky, but fuck it, gotta get this shit working like yesterday", // S H&R
    )


    fun createCrashReport(error: Throwable?, notes: String = "-/-"): String {
        val builder = CrashReportBuilder()

        builder += GeneralSection(notes)
        error?.let { builder += ThrowableSection(it) }
        builder += RuntimeSection()
        builder += SystemSection()
        builder += GPUSection()

        builder += ModCrashSection()

        builder += SessionCrashSection()


        catchAll { builder += PropertiesSection(MinosoftProperties) }
        catchAll { MinosoftProperties.git?.let { builder += GitSection(it) } }


        return builder.build()
    }

    fun StringBuilder.removeTrailingNewline() {
        if (isEmpty()) {
            return
        }
        val lastIndex = this.lastIndex
        if (this[lastIndex] == '\n') {
            deleteCharAt(lastIndex)
        }
    }
}
