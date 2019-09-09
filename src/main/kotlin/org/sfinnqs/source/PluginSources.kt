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
import org.bukkit.configuration.InvalidConfigurationException
import org.bukkit.plugin.Plugin
import java.net.MalformedURLException
import java.net.URL

@NotThreadSafe
class PluginSources(private val sourcePlugin: SourcePlugin) {

    val sources: Map<String, String>
        get() {
            val result = mutableMapOf<String, String>()
            val config = sourcePlugin.sourceConfig
            val configSources = config.sources
            val serverType = config.serverType
            val list = mutableListOf(getSource(serverType))
            sourcePlugin.server.pluginManager.plugins.mapTo(list) {
                getSource(it)
            }

            for (plugin in sourcePlugin.server.pluginManager.plugins) {
                val pluginName = plugin.name
                val pluginSource = configSources[pluginName]
                        ?: (plugin as? OpenSource)?.source
                        ?: throw InvalidConfigurationException("The source for $pluginName must be specified in your config")

                result[pluginName] = pluginSource
            }
            return result
        }

    fun getSource(pluginName: String): SourceOrFailure {
        val config = sourcePlugin.sourceConfig
        val manager = sourcePlugin.server.pluginManager
        val exactPlugin = manager.getPlugin(pluginName)
        return if (exactPlugin == null) {
            val serverType = config.serverType
            if (pluginName == serverType) {
                getSourceExact(serverType)
            } else {
                val inexactPlugin = manager.plugins.firstOrNull {
                    it.name.equals(pluginName, true)
                }
                if (inexactPlugin == null)
                    if (pluginName.equals(serverType, true))
                        getSourceExact(serverType)
                    else
                        UnrecognizedName
                else
                    getSource(inexactPlugin)
            }
        } else {
            getSource(exactPlugin)
        }
    }

    val plugins: Set<String>
        get() {
            val result = mutableSetOf(sourcePlugin.sourceConfig.serverType)
            val pluginNames = sourcePlugin.server.pluginManager.plugins.map {
                it.name
            }
            result.addAll(pluginNames)
            return result
        }

    private fun getSource(plugin: Plugin): SourceOrFailure {
        val pluginName = plugin.name
        val result = getSourceExact(pluginName)
        if (result !is SourceUnavailable) return result
        val pluginSource = (plugin as? OpenSource)?.source ?: return result
        return NameAndSource(pluginName, pluginSource)
    }

    // must be official name
    private fun getSourceExact(pluginName: String): SourceOrFailure {
        val result = sourcePlugin.sourceConfig.sources[pluginName]
                ?: return SourceUnavailable(pluginName)
        return try {
            NameAndSource(pluginName, URL(result).toString())
        } catch (e: MalformedURLException) {
            BadUrl(e)
        }
    }


}