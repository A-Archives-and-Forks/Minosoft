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

package de.bixilon.minosoft.config.profile.profiles.other

import de.bixilon.minosoft.config.profile.ProfileLock
import de.bixilon.minosoft.config.profile.ProfileType
import de.bixilon.minosoft.config.profile.delegate.primitive.BooleanDelegate
import de.bixilon.minosoft.config.profile.profiles.Profile
import de.bixilon.minosoft.config.profile.profiles.other.log.LogC
import de.bixilon.minosoft.config.profile.profiles.other.updater.UpdaterC
import de.bixilon.minosoft.config.profile.storage.ProfileStorage
import de.bixilon.minosoft.data.registries.identified.Namespaces.minosoft
import org.kordamp.ikonli.Ikon
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid

/**
 * Profile for various things that do not fit in any other profile
 */
class OtherProfile(
    override var storage: ProfileStorage? = null,
) : Profile {
    override val lock = ProfileLock()

    /**
     * Use native network transport if available
     */
    var nativeNetwork by BooleanDelegate(this, true)

    /**
     * Listens for servers on your LAN network
     */
    var listenLAN by BooleanDelegate(this, true)

    val log = LogC(this)
    val updater = UpdaterC(this)


    override fun toString(): String {
        return storage?.toString() ?: super.toString()
    }

    companion object : ProfileType<OtherProfile> {
        override val identifier = minosoft("other")
        override val clazz = OtherProfile::class.java
        override val icon: Ikon get() = FontAwesomeSolid.RANDOM

        override fun create(storage: ProfileStorage?) = OtherProfile(storage)
    }
}
