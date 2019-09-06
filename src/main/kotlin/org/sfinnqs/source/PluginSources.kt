/**
 * The Source plugin - A Bukkit plugin for sharing source code
 * Copyright (C) 2019 Finn Voichick
 *
 * This file is part of the Source plugin.
 *
 * The Source plugin is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published by the
 * Free Software Foundation, version 3.
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

import net.jcip.annotations.NotThreadSafe

@NotThreadSafe
class PluginSources(private val sourcePlugin: SourcePlugin) {

    val sources: Map<String, String>
    get() {
        val result = mutableMapOf<String, String>()
        val config = sourcePlugin.sourceConfig
        val configSources = config.sources
        val serverType = config.serverType
        if (serverType != null) {
            val serverSource = configSources[serverType]
            if (serverSource != null) result[serverType] = serverSource
        }
        for (plugin in sourcePlugin.server.pluginManager.plugins) {
            val pluginName = plugin.name
            val pluginSource = configSources[pluginName] ?: (plugin as? OpenSource)?.source?.toString()
            if (pluginSource != null) result[pluginName] = pluginSource
        }
        return result
    }

    fun getSource(pluginName: String): NameAndSource {
        val config = sourcePlugin.sourceConfig
        val manager = sourcePlugin.server.pluginManager
        val exactPlugin = manager.getPlugin(pluginName)
        val (plugin, officialName) = if (exactPlugin == null) {
            val serverType = config.serverType
            if (pluginName == serverType) {
                null to serverType
            } else {
                val inexactPlugin = manager.plugins.firstOrNull {
                    it.name.equals(pluginName, true)
                }
                if (inexactPlugin == null)
                    if (pluginName.equals(serverType, true))
                        null to serverType
                    else
                        return NameAndSource(null, null)
                else
                    inexactPlugin to inexactPlugin.name
            }
        } else {
            exactPlugin to exactPlugin.name
        }
        val source = config.sources[officialName] ?: (plugin as? OpenSource)?.source?.toString()
        return NameAndSource(officialName, source)
    }

    val allPlugins: Set<String>
        get() {
            val result = mutableSetOf<String>()
            sourcePlugin.sourceConfig.serverType?.let { result.add(it) }
            result.addAll(sourcePlugin.server.pluginManager.plugins.map { it.name })
            return result
        }

    val plugins: Set<String>
        get() {
            val result = mutableSetOf<String>()
            val configSources = sourcePlugin.sourceConfig.sources
            val serverType = sourcePlugin.sourceConfig.serverType
            if (serverType != null && serverType in configSources)
                result.add(serverType)
            val pluginNames = sourcePlugin.server.pluginManager.plugins.filter {
                it is OpenSource || it.name in configSources
            }.map {
                it.name
            }
            result.addAll(pluginNames)
            return result
        }

}