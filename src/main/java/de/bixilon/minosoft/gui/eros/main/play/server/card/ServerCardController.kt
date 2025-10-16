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

package de.bixilon.minosoft.gui.eros.main.play.server.card

import de.bixilon.kutil.concurrent.pool.DefaultThreadPool
import de.bixilon.kutil.primitive.IntUtil.thousands
import de.bixilon.kutil.unit.UnitFormatter.format
import de.bixilon.minosoft.data.language.IntegratedLanguage
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.gui.eros.card.AbstractCardController
import de.bixilon.minosoft.gui.eros.card.CardFactory
import de.bixilon.minosoft.gui.eros.main.play.server.ServerListController
import de.bixilon.minosoft.gui.eros.main.play.server.card.FaviconManager.favicon
import de.bixilon.minosoft.gui.eros.main.play.server.card.FaviconManager.saveFavicon
import de.bixilon.minosoft.gui.eros.util.JavaFXUtil
import de.bixilon.minosoft.gui.eros.util.JavaFXUtil.ctext
import de.bixilon.minosoft.gui.eros.util.JavaFXUtil.text
import de.bixilon.minosoft.util.KUtil.text
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import de.bixilon.minosoft.util.PixelImageView
import de.bixilon.minosoft.util.delegate.JavaFXDelegate.observeFX
import javafx.fxml.FXML
import javafx.scene.control.Label
import javafx.scene.image.Image
import javafx.scene.text.TextFlow
import java.io.ByteArrayInputStream

class ServerCardController : AbstractCardController<ServerCard>() {
    @FXML private lateinit var faviconFX: PixelImageView
    @FXML private lateinit var serverNameFX: TextFlow
    @FXML private lateinit var motdFX: TextFlow
    @FXML private lateinit var pingFX: Label
    @FXML private lateinit var playerCountFX: Label
    @FXML private lateinit var serverVersionFX: Label

    var serverList: ServerListController? = null


    override fun clear() {
        faviconFX.image = JavaFXUtil.MINOSOFT_LOGO

        serverNameFX.children.clear()
        motdFX.children.clear()
        resetPing()
    }

    private fun resetPing() {
        pingFX.ctext = ""
        playerCountFX.ctext = ""
        serverVersionFX.ctext = ""
    }

    override fun updateItem(item: ServerCard?, empty: Boolean) {
        val previous = this.item
        super.updateItem(item, empty)

        root.isVisible = !empty
        if (previous === item) {
            return
        }
        item ?: return

        serverNameFX.text = item.server.name

        item.ping()

        item.server.favicon?.let { faviconFX.image = it }

        val ping = item.ping


        ping::status.observeFX(ping, instant = true) {
            if (this.item != item || ping.error != null) {
                // error already occurred, not setting any data
                return@observeFX
            }
            if (it == null) return@observeFX // card already clear

            motdFX.text = it.motd ?: ChatComponent.EMPTY
            playerCountFX.ctext = "${it.usedSlots?.thousands()} / ${it.slots?.thousands()}"
            serverVersionFX.ctext = ping.serverVersion?.name

            val favicon = it.favicon

            if (favicon == null) {
                item.server.faviconHash = null
            } else {
                DefaultThreadPool += { item.server.saveFavicon(favicon) }  // ToDo: This is running every time?
            }

            faviconFX.image = favicon?.let { Image(ByteArrayInputStream(favicon)) } ?: JavaFXUtil.MINOSOFT_LOGO


            serverList?.onPingUpdate(item)
        }

        ping::state.observeFX(ping, instant = true) {
            if (this.item != item || ping.error != null || ping.status != null) {
                return@observeFX
            }

            motdFX.text = ChatComponent.of(IntegratedLanguage.LANGUAGE.translate(it))
            serverList?.onPingUpdate(item)
            resetPing()
        }

        ping::error.observeFX(ping, instant = true) {
            if (this.item != item || it == null) {
                return@observeFX
            }
            motdFX.text = it.text
            resetPing()

            serverList?.onPingUpdate(item)
        }
        ping::pong.observeFX(ping, instant = true) {
            if (this.item != item || ping.error != null || it == null) {
                // error already occurred, not setting any data
                return@observeFX
            }
            pingFX.text = it.latency.format()
            serverList?.onPingUpdate(item)
        }
    }

    companion object : CardFactory<ServerCardController> {
        override val LAYOUT: ResourceLocation = "minosoft:eros/main/play/server/server_card.fxml".toResourceLocation()
    }
}
