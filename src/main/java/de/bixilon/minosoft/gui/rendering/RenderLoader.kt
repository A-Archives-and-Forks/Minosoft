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

import de.bixilon.kutil.exception.ExceptionUtil.ignoreAll
import de.bixilon.kutil.latch.AbstractLatch
import de.bixilon.kutil.latch.ParentLatch
import de.bixilon.kutil.latch.SimpleLatch
import de.bixilon.kutil.observer.DataObserver.Companion.observe
import de.bixilon.kutil.primitive.BooleanUtil.decide
import de.bixilon.kutil.reflection.ReflectionUtil.forceSet
import de.bixilon.kutil.unit.UnitFormatter.formatNanos
import de.bixilon.minosoft.gui.rendering.RenderUtil.pause
import de.bixilon.minosoft.gui.rendering.RenderUtil.runAsync
import de.bixilon.minosoft.gui.rendering.events.ResizeWindowEvent
import de.bixilon.minosoft.gui.rendering.font.manager.FontManager
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.input.key.DebugKeyBindings
import de.bixilon.minosoft.gui.rendering.input.key.DefaultKeyBindings
import de.bixilon.minosoft.gui.rendering.renderer.renderer.DefaultRenderer
import de.bixilon.minosoft.modding.event.listener.CallbackEventListener.Companion.listen
import de.bixilon.minosoft.protocol.network.session.play.PlaySessionStates
import de.bixilon.minosoft.util.Stopwatch
import de.bixilon.minosoft.util.delegate.RenderingDelegate.observeRendering
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import kotlin.system.measureNanoTime

object RenderLoader {

    private fun RenderContext.setThread() {
        if (this.thread != null) { // unsafeNull
            throw IllegalStateException("Thread is already set!")
        }
        this::thread.forceSet(Thread.currentThread())
    }

    private fun RenderContext.registerRenderer() {
        for (builder in DefaultRenderer.list) {
            this.renderer.register(builder)
        }
    }

    fun RenderContext.load(latch: AbstractLatch) {
        val renderLatch = ParentLatch(1, latch)
        setThread()
        Log.log(LogMessageType.RENDERING, LogLevels.VERBOSE) { "Creating window..." }
        val stopwatch = Stopwatch()
        registerRenderer()

        window.init(session.profiles.rendering)
        ignoreAll { window.setDefaultIcon(session.assetsManager) }

        camera.init()


        Log.log(LogMessageType.RENDERING, LogLevels.VERBOSE) { "Creating context (after ${stopwatch.labTime()})..." }

        system.init()
        system.reset()

        Log.log(LogMessageType.RENDERING, LogLevels.VERBOSE) { "We are running on ${system.vendorString} (detected ${system.vendor}). Version is ${system.version} and we got an ${system.gpuType}." }

        // Init stage
        val initLatch = ParentLatch(1, renderLatch)
        Log.log(LogMessageType.RENDERING, LogLevels.VERBOSE) { "Generating font, gathering textures and loading models (after ${stopwatch.labTime()})..." }
        initLatch.inc(); runAsync { tints.init(session.assetsManager); initLatch.dec() }
        textures.dynamic.load(initLatch); textures.dynamic.upload(initLatch)
        textures.initializeSkins(session)
        textures.loadDefaultTextures()

        initLatch.inc(); runAsync { font = FontManager.create(this, initLatch); initLatch.dec() }
        initLatch.inc(); runAsync { models.loadRegistry(latch); initLatch.dec() }


        framebuffer.init()


        Log.log(LogMessageType.RENDERING, LogLevels.VERBOSE) { "Initializing renderer (after ${stopwatch.labTime()})..." }
        light.init()
        skeletal.init()
        renderer.init(initLatch)

        // Wait for init stage to complete
        initLatch.dec()
        initLatch.await()
        models.loadDynamic(latch)

        renderer[GUIRenderer]?.atlas?.load() // TODO: remove this

        // Post init stage
        Log.log(LogMessageType.RENDERING, LogLevels.VERBOSE) { "Loading textures (after ${stopwatch.labTime()})..." }
        // TODO: async load both
        textures.static.load(renderLatch)
        textures.font.load(renderLatch)

        Log.log(LogMessageType.RENDERING, LogLevels.VERBOSE) { "Uploading textures (after ${stopwatch.labTime()})..." }
        textures.static.upload(renderLatch)
        textures.font.upload(renderLatch)

        Log.log(LogMessageType.RENDERING, LogLevels.VERBOSE) { "Baking models (after ${stopwatch.labTime()})..." }
        font.postInit(renderLatch)
        models.bake(renderLatch)
        models.upload()
        models.cleanup()

        Log.log(LogMessageType.RENDERING, LogLevels.VERBOSE) { "Post loading renderer (after ${stopwatch.labTime()})..." }
        shaders.postInit()
        skeletal.postInit()
        renderer.postInit(renderLatch)
        framebuffer.postInit()

        Log.log(LogMessageType.RENDERING, LogLevels.VERBOSE) { "Finishing up (after ${stopwatch.labTime()})..." }

        window::focused.observeRendering(this) { state = it.decide(RenderingStates.RUNNING, RenderingStates.SLOW) }

        window::iconified.observeRendering(this) { state = it.decide(RenderingStates.PAUSED, RenderingStates.RUNNING) }


        input.init()
        DefaultKeyBindings.register(this)
        DebugKeyBindings.register(this)
        session.events.listen<ResizeWindowEvent> { system.viewport = it.size }

        this::state.observe(this) {
            if (it == RenderingStates.PAUSED || it == RenderingStates.SLOW || it == RenderingStates.STOPPED) {
                pause()
            }
        }

        window.postInit()

        textures.dynamic.activate()
        textures.static.activate()


        renderLatch.dec() // initial count from rendering
        renderLatch.await()

        Log.log(LogMessageType.RENDERING) { "Rendering is fully prepared in ${stopwatch.totalTime()}" }
    }

    fun RenderContext.awaitPlaying() {
        state = RenderingStates.AWAITING

        val latch = SimpleLatch(1)

        session::state.observe(this, instant = true) {
            if (it == PlaySessionStates.PLAYING && latch.count > 0) {
                latch.dec()
            }
        }

        val time = measureNanoTime {
            latch.await()
            state = RenderingStates.RUNNING
            window.visible = true
        }
        Log.log(LogMessageType.RENDERING) { "Showing window after ${time.formatNanos()}" }
    }
}
