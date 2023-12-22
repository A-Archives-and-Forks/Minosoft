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

package de.bixilon.minosoft.data.language

import de.bixilon.kutil.exception.ExceptionUtil.tryCatch
import de.bixilon.kutil.json.JsonObject
import de.bixilon.minosoft.assets.AssetsManager
import de.bixilon.minosoft.assets.util.InputStreamUtil.readJsonObject
import de.bixilon.minosoft.data.language.lang.LanguageData
import de.bixilon.minosoft.data.language.lang.LanguageFile
import de.bixilon.minosoft.data.language.manager.Language
import de.bixilon.minosoft.data.language.translate.Translated
import de.bixilon.minosoft.data.language.translate.Translator
import de.bixilon.minosoft.data.registries.identified.Namespaces
import de.bixilon.minosoft.data.registries.identified.Namespaces.minosoft
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.data.text.TextComponent
import de.bixilon.minosoft.protocol.versions.Version
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import java.io.BufferedReader
import java.io.FileNotFoundException
import java.io.InputStreamReader

object LanguageUtil {
    const val FALLBACK_LANGUAGE = "en_us"


    fun String?.i18n(): Translated {
        val resourceLocation = this.toResourceLocation()
        if (resourceLocation.namespace == Namespaces.MINECRAFT) {
            return Translated(minosoft(resourceLocation.path))
        }
        return Translated(resourceLocation)
    }

    fun loadJsonLanguage(json: JsonObject): LanguageData {
        val data: LanguageData = HashMap()

        for ((key, value) in json) {
            val path = ResourceLocation.of(key).path
            data[path] = value.toString().correctValue()
        }

        return data
    }

    fun loadLanguage(lines: BufferedReader): LanguageData {
        val data: LanguageData = HashMap()

        while (true) {
            val line = lines.readLine() ?: break
            if (line.isBlank() || line.startsWith("#")) {
                continue
            }
            val (key, value) = line.split('=', limit = 2)
            val path = ResourceLocation.of(key).path
            data[path] = value.correctValue()
        }
        return data
    }

    private fun String.correctValue(): String {
        return this.replace("\\n", "\n")
    }

    fun getFallbackTranslation(key: ResourceLocation?, parent: TextComponent?, restricted: Boolean = false, vararg data: Any?): ChatComponent {
        if (data.isEmpty()) {
            return ChatComponent.of(key.toString(), null, parent, restricted)
        }
        return ChatComponent.of(key.toString() + "->" + data.contentToString(), null, parent, restricted)
    }

    fun loadLanguage(language: String, assetsManager: AssetsManager, json: Boolean, path: ResourceLocation): Translator? {
        val assets = assetsManager.getAllOrNull(ResourceLocation(path.namespace, path.path + language + if (json) ".json" else ".lang")) ?: return null
        if (assets.isEmpty()) return null
        val languages: MutableList<LanguageFile> = mutableListOf()

        for (asset in assets) {
            val data = if (json) loadJsonLanguage(asset.readJsonObject()) else loadLanguage(BufferedReader(InputStreamReader(asset, Charsets.UTF_8)))
            languages += LanguageFile(language, path.namespace, data)
        }

        if (languages.size == 1) {
            return languages.first()
        }

        return Language(languages.toTypedArray())
    }


    fun load(language: String, version: Version?, assets: AssetsManager, path: ResourceLocation = ResourceLocation.of("lang/")): Translator {
        val name = language.lowercase()
        val json = version != null && version.jsonLanguage

        val translators: MutableList<Translator> = mutableListOf()


        if (name != FALLBACK_LANGUAGE) {
            tryCatch(FileNotFoundException::class.java, executor = { translators += loadLanguage(name, assets, json, path) ?: return@tryCatch })
        }
        loadLanguage(FALLBACK_LANGUAGE, assets, json, path)?.let { translators += it }

        if (translators.size == 1) {
            return translators.first()
        }

        return Language(translators.toTypedArray())
    }


    fun ResourceLocation.translation(name: String): ResourceLocation {
        return ResourceLocation(this.namespace, "$name.$namespace.$path") // TODO: use name?
    }
}
