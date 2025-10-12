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

package de.bixilon.minosoft.gui.rendering.gui.hud.elements.chat

import de.bixilon.minosoft.data.world.vec.vec2.f.Vec2f
import de.bixilon.kutil.concurrent.pool.DefaultThreadPool
import de.bixilon.minosoft.data.chat.message.internal.InternalChatMessage
import de.bixilon.minosoft.data.world.vec.vec2.f.MVec2f
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.elements.Element
import de.bixilon.minosoft.modding.event.events.chat.ChatMessageEvent
import de.bixilon.minosoft.modding.event.listener.CallbackEventListener.Companion.listen
import de.bixilon.minosoft.util.delegate.RenderingDelegate.observeRendering

class InternalChatElement(guiRenderer: GUIRenderer) : AbstractChatElement(guiRenderer) {
    private val chatProfile = profile.chat.internal
    private var active = false
        set(value) {
            field = value
            messages._active = value
            messages.forceSilentApply()
            forceApply()
        }
    override var skipDraw: Boolean // skips hud draw and draws it in gui stage
        get() = chatProfile.hidden || active
        set(value) {
            chatProfile.hidden = !value
        }
    override val activeWhenHidden: Boolean
        get() = true

    init {
        messages.prefMaxSize = Vec2f(chatProfile.width, chatProfile.height)
        chatProfile::width.observeRendering(this, context = context) { messages.prefMaxSize = Vec2f(it, messages.prefMaxSize.y) }
        chatProfile::height.observeRendering(this, context = context) { messages.prefMaxSize = Vec2f(messages.prefMaxSize.x, it) }
        forceSilentApply()
    }


    override fun postInit() {
        session.events.listen<ChatMessageEvent> {
            if (it.message !is InternalChatMessage || profile.chat.internal.hidden) {
                return@listen
            }
            DefaultThreadPool += { messages += it.message.raw }
        }
    }

    override fun forceSilentApply() {
        messages.silentApply()
        _size = Vec2f(messages.prefMaxSize.x, messages.size.y + ChatElement.CHAT_INPUT_MARGIN * 2)
        cacheUpToDate = false
    }

    override fun onOpen() {
        active = true
        messages.onOpen()
    }

    override fun onClose() {
        active = false
        messages.onClose()
    }

    override fun getAt(position: Vec2f): Pair<Element, Vec2f>? {
        if (position.x < ChatElement.CHAT_INPUT_MARGIN) {
            return null
        }
        val offset = MVec2f(position)
        offset.x -= ChatElement.CHAT_INPUT_MARGIN

        val messagesSize = messages.size
        if (offset.y < messagesSize.y) {
            if (offset.x > messagesSize.x) {
                return null
            }
            return Pair(messages, offset.unsafe)
        }
        offset.y -= messagesSize.y
        return null
    }

    override fun onChildChange(child: Element) {
        forceSilentApply()
        super.onChildChange(child)
    }
}
