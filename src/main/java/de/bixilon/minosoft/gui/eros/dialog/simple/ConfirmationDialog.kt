/*
 * Minosoft
 * Copyright (C) 2020-2023 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.eros.dialog.simple

import de.bixilon.kutil.concurrent.pool.DefaultThreadPool
import de.bixilon.minosoft.data.language.IntegratedLanguage
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.data.text.TranslatableComponents
import de.bixilon.minosoft.gui.eros.controller.DialogController
import de.bixilon.minosoft.gui.eros.util.JavaFXUtil
import de.bixilon.minosoft.gui.eros.util.JavaFXUtil.text
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.text.TextFlow
import javafx.stage.Modality

class ConfirmationDialog(
    val title: Any = DEFAULT_TITLE_TEXT,
    val header: Any = DEFAULT_TITLE_TEXT,
    val description: Any? = null,
    val cancelButtonText: Any = TranslatableComponents.GENERAL_CANCEL,
    val confirmButtonText: Any = TranslatableComponents.GENERAL_CONFIRM,
    val onCancel: () -> Unit = {},
    val onConfirm: () -> Unit,
    val modality: Modality = Modality.WINDOW_MODAL,
) : DialogController() {
    @FXML private lateinit var headerFX: TextFlow
    @FXML private lateinit var descriptionFX: TextFlow
    @FXML private lateinit var cancelButtonFX: Button
    @FXML private lateinit var confirmButtonFX: Button

    public override fun show() {
        JavaFXUtil.runLater {
            JavaFXUtil.openModal(title, LAYOUT, this, modality)
            super.show()
        }
    }

    override fun init() {
        headerFX.text = IntegratedLanguage.LANGUAGE.translate(header)
        descriptionFX.text = description?.let { IntegratedLanguage.LANGUAGE.translate(it) } ?: ChatComponent.EMPTY
        cancelButtonFX.text = IntegratedLanguage.LANGUAGE.translate(cancelButtonText).message
        confirmButtonFX.text = IntegratedLanguage.LANGUAGE.translate(confirmButtonText).message
    }

    override fun postInit() {
        super.postInit()

        stage.setOnCloseRequest {
            DefaultThreadPool += onCancel
        }
    }

    @FXML
    fun confirm() {
        DefaultThreadPool += onConfirm
        stage.close()
    }

    @FXML
    fun cancel() {
        DefaultThreadPool += onCancel
        stage.close()
    }


    companion object {
        private val LAYOUT = "minosoft:eros/dialog/simple/confirmation.fxml".toResourceLocation()
        private val DEFAULT_TITLE_TEXT = "minosoft:general.dialog.are_you_sure".toResourceLocation()
    }
}
