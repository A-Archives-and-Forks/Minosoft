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

package de.bixilon.minosoft.gui.eros

import de.bixilon.kutil.collections.CollectionUtil.toSynchronizedSet
import de.bixilon.kutil.concurrent.pool.DefaultThreadPool
import de.bixilon.kutil.exception.ExceptionUtil.catchAll
import de.bixilon.kutil.latch.SimpleLatch
import de.bixilon.kutil.observer.DataObserver.Companion.observe
import de.bixilon.minosoft.config.profile.profiles.eros.ErosProfileManager
import de.bixilon.minosoft.config.profile.profiles.other.OtherProfileManager
import de.bixilon.minosoft.data.registries.identified.Namespaces.i18n
import de.bixilon.minosoft.gui.eros.crash.CrashReportState
import de.bixilon.minosoft.gui.eros.dialog.UpdateAvailableDialog
import de.bixilon.minosoft.gui.eros.dialog.simple.ConfirmationDialog
import de.bixilon.minosoft.gui.eros.main.MainErosController
import de.bixilon.minosoft.gui.eros.modding.invoker.JavaFXEventListener.Companion.javaFX
import de.bixilon.minosoft.gui.eros.util.JavaFXUtil
import de.bixilon.minosoft.gui.eros.util.JavaFXUtil.forceInit
import de.bixilon.minosoft.main.MinosoftBoot
import de.bixilon.minosoft.modding.event.events.FinishBootEvent
import de.bixilon.minosoft.modding.event.master.GlobalEventMaster
import de.bixilon.minosoft.properties.MinosoftProperties
import de.bixilon.minosoft.updater.MinosoftUpdater
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import javafx.stage.Modality
import javafx.stage.Window

object Eros {
    private val TITLE = "minosoft:eros_window_title".toResourceLocation()
    private val LAYOUT = "minosoft:eros/main/main.fxml".toResourceLocation()

    private val latch = SimpleLatch(2)

    lateinit var mainErosController: MainErosController


    var skipErosStartup = false

    var initialized = false
        private set

    var visible: Boolean = false
        private set


    @Synchronized
    fun setVisibility(visible: Boolean) {
        if (visible == this.visible) return
        if (CrashReportState.crashed) return
        if (!initialized) return

        if (visible) {
            mainErosController.stage.show()
        } else {
            for (window in Window.getWindows().toSynchronizedSet()) {
                JavaFXUtil.runLater { window.hide() }
            }
        }
        this.visible = visible
    }


    init {
        GlobalEventMaster.javaFX<FinishBootEvent> {
            if (skipErosStartup) return@javaFX
            start()
        }

        ErosProfileManager::selected.observe(this) {
            if (skipErosStartup || !this::mainErosController.isInitialized) {
                return@observe
            }
            JavaFXUtil.runLater {
                this.mainErosController.stage.close()
                start()
            }
        }
    }

    private fun askForUpdates() {
        if (!MinosoftProperties.canUpdate()) return
        val profile = OtherProfileManager.selected.updater
        if (!profile.ask) return
        val dialog = ConfirmationDialog(
            title = i18n("updater.ask.title"),
            header = i18n("updater.ask.header"),
            description = i18n("updater.ask.description"),
            cancelButtonText = i18n("updater.ask.no"),
            confirmButtonText = i18n("updater.ask.yes"),
            onCancel = { profile.ask = false; profile.check = false },
            onConfirm = { profile.ask = false; profile.check = true; DefaultThreadPool += { MinosoftUpdater.check() } },
            modality = Modality.APPLICATION_MODAL,
        )
        dialog.show()
    }

    fun start() {
        if (latch.count >= 1) return
        latch.await()
        mainErosController.stage.show()
        initialized = true
        visible = true

        MinosoftUpdater::update.observe(this, true) {
            if (it == null) return@observe
            if (it.id == OtherProfileManager.selected.updater.dismiss) return@observe // TODO: if not searched manually
            UpdateAvailableDialog(it).show()
        }

        askForUpdates()
        Log.log(LogMessageType.JAVAFX, LogLevels.VERBOSE) { "Eros up!" }
    }

    fun preload() {
        latch.dec()
        JavaFXUtil.openModalAsync<MainErosController>(TITLE, LAYOUT) {
            mainErosController = it
            catchAll { it.stage.forceInit() }
            latch.dec()
            if (MinosoftBoot.LATCH.count == 0) {
                start()
            }
        }
    }
}
