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

package de.bixilon.minosoft.gui.rendering

import de.bixilon.kutil.latch.SimpleLatch
import de.bixilon.kutil.observer.DataObserver
import de.bixilon.kutil.reflection.ReflectionUtil.forceSet
import de.bixilon.minosoft.assets.AssetsLoader
import de.bixilon.minosoft.gui.rendering.system.dummy.DummyRenderSystem
import de.bixilon.minosoft.gui.rendering.system.window.dummy.DummyWindow
import de.bixilon.minosoft.protocol.network.session.play.SessionTestUtil.createSession
import org.testng.Assert.assertTrue
import org.testng.annotations.Test

@Test(priority = 100, groups = ["rendering"])
class RenderTestLoader {

    fun init() {
        val session = createSession(5)
        val latch = SimpleLatch(1)
        session::assetsManager.forceSet(AssetsLoader.create(session.profiles.resources, session.version, latch))
        session.assetsManager.load(latch)
        session::error.forceSet(DataObserver(null))
        RenderTestUtil.rendering = Rendering(session)
        RenderTestUtil.rendering.start(latch, audio = false)
        latch.dec()
        while (latch.count > 0) {
            Thread.sleep(10)
            session.error?.let { throw it }
        }
        val context = RenderTestUtil.rendering.context
        assertTrue(context.window is DummyWindow)
        assertTrue(context.system is DummyRenderSystem)
        RenderTestUtil.context = context
    }
}
