/**
 * The Source plugin - A Bukkit plugin for sharing source code
 * Copyright (C) 2020 sfinnqs
 *
 * This file is part of the Source plugin.
 *
 * The Source plugin is free software; you can redistribute it and/or modify it
 * under the terms of version 3 of the GNU Affero General Public License as
 * published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see <https://www.gnu.org/licenses>.
 *
 * Additional permission under GNU AGPL version 3 section 7
 *
 * If you modify this Program, or any covered work, by linking or combining it
 * with the "Minecraft: Java Edition" server (or a modified version of the
 * "Minecraft: Java Edition" server), containing parts covered by the terms of
 * the Minecraft End-User Licence Agreement, the licensor of the Source plugin
 * grants you additional permission to support user interaction over a network
 * with the resulting work. In this case, you are still required to provide
 * access to the Corresponding Source of your version (under GNU AGPL version 3
 * section 13) but you may omit source code from the "Minecraft: Java Edition"
 * server from the available Corresponding Source.
 */
package org.sfinnqs.source.config

import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.collections.immutable.toPersistentMap
import net.jcip.annotations.Immutable
import org.bukkit.configuration.Configuration
import org.bukkit.configuration.InvalidConfigurationException

@Immutable
data class SourceConfig(
    val serverType: String,
    val offer: OfferConfig,
    val sources: PersistentMap<String, String>
) {
    constructor(config: Configuration) : this(
        config.serverType,
        config.offer,
        config.sources
    )

    fun asMap(): Map<String, Any> {
        val result =
            mutableMapOf("server type" to serverType, "offer" to offer.asMap())
        if (sources.isNotEmpty()) result["sources"] = sources
        return result.toImmutableMap()
    }

    fun setSource(plugin: String, source: String) =
        copy(sources = sources.put(plugin, source))

    private companion object {
        val Configuration.serverType
            get() = getString("server type", null)
                ?: throw InvalidConfigurationException("server type must be specified in config")
        val Configuration.offer: OfferConfig
            get() = OfferConfig(getSectionOrEmpty("offer"))
        val Configuration.sources: PersistentMap<String, String>
            get() {
                val section = getSectionOrEmpty("sources")
                val result = mutableMapOf<String, String>()
                for (pluginName in section.getKeys(false)) {
                    val source = section.getString(pluginName) ?: continue
                    result[pluginName] = source
                }
                return result.toPersistentMap()
            }

    }
}
