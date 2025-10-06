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

package de.bixilon.minosoft.gui.eros.crash

import de.bixilon.kutil.collections.CollectionUtil.toSynchronizedSet
import de.bixilon.kutil.exception.ExceptionUtil.catchAll
import de.bixilon.kutil.exception.ExceptionUtil.toStackTrace
import de.bixilon.kutil.file.FileUtil.div
import de.bixilon.kutil.file.PathUtil.div
import de.bixilon.kutil.file.watcher.FileWatcherService
import de.bixilon.kutil.shutdown.AbstractShutdownReason
import de.bixilon.kutil.shutdown.ShutdownManager
import de.bixilon.kutil.time.TimeUtil.format1
import de.bixilon.kutil.unsafe.UnsafeUtil
import de.bixilon.minosoft.gui.eros.controller.JavaFXWindowController
import de.bixilon.minosoft.gui.eros.util.JavaFXInitializer
import de.bixilon.minosoft.gui.eros.util.JavaFXUtil
import de.bixilon.minosoft.protocol.network.session.play.PlaySession
import de.bixilon.minosoft.terminal.RunConfiguration
import de.bixilon.minosoft.util.crash.CrashReportUtil
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.Hyperlink
import javafx.scene.control.TextArea
import javafx.scene.text.TextFlow
import javafx.stage.Modality
import javafx.stage.Stage
import javafx.stage.Window
import java.io.File
import java.io.FileOutputStream
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import kotlin.time.Clock
import kotlin.time.ExperimentalTime


class ErosCrashReport : JavaFXWindowController() {
    @FXML private lateinit var crashReportPathDescriptionFX: TextFlow
    @FXML private lateinit var crashReportPathFX: Hyperlink
    @FXML private lateinit var detailsFX: TextArea


    var crashReportPath: File? = null
        set(value) {
            field = value
            JavaFXUtil.runLater {
                crashReportPathDescriptionFX.isVisible = value != null
                if (value != null) {
                    crashReportPathFX.text = value.path
                }
            }
        }

    var exception: Throwable? = null

    var details: String? = null
        set(value) {
            field = value
            JavaFXUtil.runLater { detailsFX.text = value }
        }

    fun exit() {
        ShutdownManager.shutdown(exception?.message, AbstractShutdownReason.CRASH)
    }

    fun hardCrash() {
        UnsafeUtil.hardCrash()
    }

    @OptIn(ExperimentalTime::class)
    companion object {

        /**
         * Kills all connections, closes all windows, creates and saves a crash report
         * Special: Does not use any general functions/translations/..., because when a crash happens, you can't rely on anything.
         */
        fun Throwable?.crash(notes: String = "-/-") {
            if (CrashReportState.crashed) {
                return
            }
            CrashReportState.crashed = true
            val details = try {
                CrashReportUtil.createCrashReport(this, notes)
            } catch (error: Throwable) {
                error.toStackTrace()
            }

            catchAll {
                for (session in PlaySession.ACTIVE_CONNECTIONS.toSynchronizedSet()) {
                    session.terminate()
                    session.rendering?.context?.window?.destroy()
                }
            }
            catchAll {
                for (window in Window.getWindows().toSynchronizedSet()) {
                    JavaFXUtil.runLater { window.hide() }
                }
            }

            // Kill some stuff
            catchAll { FileWatcherService.stop() }


            var crashReportPath: File?
            try {
                val crashReportFolder = (RunConfiguration.HOME_DIRECTORY / "crash-reports").toFile()
                crashReportFolder.mkdirs()

                crashReportPath = crashReportFolder / "crash-${SimpleDateFormat("yyyy-MM-dd-HH.mm.ss").format1(Clock.System.now())}.txt"

                val stream = FileOutputStream(crashReportPath)

                stream.write(details.toByteArray(StandardCharsets.UTF_8))
                stream.close()
            } catch (exception: Throwable) {
                exception.printStackTrace()
                crashReportPath = null
            }

            if (RunConfiguration.DISABLE_EROS) {
                this?.printStackTrace()
                ShutdownManager.shutdown(this?.message, AbstractShutdownReason.CRASH)
                return
            }

            if (!JavaFXInitializer.initializing && !JavaFXInitializer.initialized) {
                try {
                    JavaFXInitializer.start()
                } catch (exception: Throwable) {
                    Log.log(LogMessageType.JAVAFX, LogLevels.WARN) { "Can not show crash report screen!" }
                    exception.printStackTrace()
                    return
                }
            }

            for (i in 0 until 10) {
                try {
                    JavaFXInitializer.await()
                } catch (error: InterruptedException) {
                    continue
                }
                break
            }
            if (!JavaFXInitializer.initialized) {
                Log.log(LogMessageType.JAVAFX, LogLevels.FATAL) { "JavaFX Toolkit still not initialized, can't wait longer. Exit!" }
                ShutdownManager.shutdown(reason = AbstractShutdownReason.CRASH)
                return
            }

            JavaFXUtil.runLater {
                val fxmlLoader = FXMLLoader(ErosCrashReport::class.java.getResource("/assets/minosoft/eros/crash/crash_screen.fxml"))
                val parent = fxmlLoader.load<Parent>()
                val stage = Stage()
                stage.initModality(Modality.APPLICATION_MODAL)
                stage.title = "Fatal Crash - Minosoft"
                stage.scene = Scene(parent)
                stage.icons.setAll(JavaFXUtil.MINOSOFT_LOGO)
                ErosCrashReport::class.java.getResource("/assets/minosoft/eros/style.css")?.toExternalForm()?.let { stage.scene.stylesheets.add(it) }

                val crashReport = fxmlLoader.getController<ErosCrashReport>()
                crashReport.exception = this
                crashReport.details = details
                crashReport.crashReportPath = crashReportPath
                crashReport.stage = stage

                stage.setOnCloseRequest { ShutdownManager.shutdown(this?.message, AbstractShutdownReason.CRASH) }
                stage.show()
            }
        }
    }
}
