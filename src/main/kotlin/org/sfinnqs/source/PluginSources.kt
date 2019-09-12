/**
 * The Source plugin - A Bukkit plugin for sharing source code
 * Copyright (C) 2019 sfinnqs
 *
 * This file is part of the Source plugin.
 *
 * The Source plugin is free software; you can redistribute it and/or modify it
 * under the terms of version 3 of the the GNU Affero General Public License as
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
package org.sfinnqs.source

import net.jcip.annotations.Immutable
import org.bukkit.plugin.Plugin
import org.sfinnqs.source.util.UnmodifiableMap
import java.util.*

@Immutable
data class PluginSources(val map: UnmodifiableMap<String, String>) {
    constructor(config: SourceConfig, plugins: Array<Plugin>) : this(getSources(config, plugins))

    private val capitalization = map.keys.associateBy { it.toLowerCase(Locale.ROOT) }

    operator fun get(pluginName: String): NameAndSource? {
        val result = map[pluginName]
        if (result != null) return NameAndSource(pluginName, result)
        val realName = capitalization[pluginName.toLowerCase(Locale.ROOT)]
                ?: return null
        return NameAndSource(realName, map[realName]!!)
    }

    val plugins: Set<String>
        get() = map.keys

    private companion object {
        fun getSources(config: SourceConfig, plugins: Array<Plugin>): UnmodifiableMap<String, String> {
            val serverType = config.serverType
            val configSources = config.sources
            val missing = mutableSetOf<String>()
            val serverSource = configSources[serverType]
            val result = mutableMapOf<String, String>()
            if (serverSource == null)
                missing.add(serverType)
            else
                result[serverType] = serverSource
            for (plugin in plugins) {
                val pluginName = plugin.name
                val pluginSource = configSources[pluginName]
                        ?: (plugin as? OpenSource)?.source
                if (pluginSource == null)
                    missing.add(pluginName)
                else
                    result[pluginName] = pluginSource
            }
            if (missing.isNotEmpty())
                throw SourcesUnavailableException(missing)
            return UnmodifiableMap(result)
        }
    }

}
