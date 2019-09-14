/**
 * The Source plugin - A Bukkit plugin for sharing source code
 * Copyright (C) 2019 sfinnqs
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

import net.jcip.annotations.Immutable
import org.bukkit.configuration.Configuration
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.InvalidConfigurationException
import org.sfinnqs.source.logger
import org.sfinnqs.source.util.UnmodifiableMap

@Immutable
data class SourceConfig(val serverType: String, val offer: OfferConfig, val sources: UnmodifiableMap<String, String>) {
    constructor(config: Configuration) : this(config.serverType, config.offer, config.sources)

    fun asMap(): Map<String, Any> {
        val result = mutableMapOf("server type" to serverType, "offer" to offer.asMap())
        val sourcesOrNull = sources.takeIf { it.isNotEmpty() }
        if (sourcesOrNull != null) result["sources"] = sourcesOrNull
        return result
    }

    fun setSource(plugin: String, source: String): SourceConfig {
        val newSources = sources.put(plugin, source)
        return SourceConfig(serverType, offer, newSources)
    }

    private companion object {
        val Configuration.serverType
            get() = getString("server type", null)
                    ?: throw InvalidConfigurationException("server type must be specified in config")
        val Configuration.offer: OfferConfig
            get() = OfferConfig(getSectionOrSet("offer"))
        val Configuration.sources: UnmodifiableMap<String, String>
            get() {
                val section = getSectionOrSet("sources")
                val result = mutableMapOf<String, String>()
                for (pluginName in section.getKeys(false)) {
                    val source = section.getString(pluginName) ?: continue
                    result[pluginName] = source
                }
                return UnmodifiableMap(result)
            }

    }
}