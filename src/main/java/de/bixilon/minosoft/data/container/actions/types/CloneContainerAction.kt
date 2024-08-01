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

package de.bixilon.minosoft.data.container.actions.types

import de.bixilon.minosoft.data.container.Container
import de.bixilon.minosoft.data.container.ContainerUtil.slotsOf
import de.bixilon.minosoft.data.container.actions.ContainerAction
import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.data.registries.item.stack.StackableItem
import de.bixilon.minosoft.protocol.network.session.play.PlaySession
import de.bixilon.minosoft.protocol.packets.c2s.play.container.ContainerClickC2SP

class CloneContainerAction(
    val slot: Int,
) : ContainerAction {
    private var copied: ItemStack? = null

    override fun invoke(session: PlaySession, containerId: Int, container: Container) {
        container.floatingItem?.let { return }
        val clicked = container[slot] ?: return
        val stack = clicked.copy(count = if (clicked.item.item is StackableItem) clicked.item.item.maxStackSize else 1)
        this.copied = stack

        // TODO (1.18.2): use creative inventory packet
        session.network.send(ContainerClickC2SP(containerId, container.serverRevision, slot, 3, 0, container.actions.createId(this), slotsOf(), stack))

        container.floatingItem = stack
    }

    override fun revert(session: PlaySession, containerId: Int, container: Container) {
        if (container.floatingItem == copied) {
            container.floatingItem = null
        }
    }
}
