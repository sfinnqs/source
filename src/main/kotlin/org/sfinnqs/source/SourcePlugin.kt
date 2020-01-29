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
package org.sfinnqs.source

import net.jcip.annotations.NotThreadSafe
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.InvalidConfigurationException
import org.bukkit.plugin.java.JavaPlugin
import org.sfinnqs.source.command.AdminExecutor
import org.sfinnqs.source.command.SourceExecutor
import org.sfinnqs.source.config.SourceConfig
import java.net.URL
import java.util.logging.Level

@NotThreadSafe
class SourcePlugin : JavaPlugin(), OpenSource {
    override fun getSource() = "https://gitlab.com/sfinnqs/source"
    lateinit var sourceConfig: SourceConfig
    lateinit var pluginSources: PluginSources
        private set

    override fun onEnable() {
        org.sfinnqs.source.logger = logger
        try {
            reload()
        } catch (e: BadUrlException) {
            for ((plugin, cause) in e.causes)
                logger.log(Level.SEVERE, "Please ensure that the URL for $plugin is formatted correctly", cause)
            logger.severe("Disabling Source because not all sources are available")
            isEnabled = false
            return
        } catch (e: InvalidConfigurationException) {
            logger.log(Level.SEVERE, "Disabling Source because not all sources are available", e)
            isEnabled = false
            return
        }
        val sourceCommand = getCommand("source")!!
        val sourceExecutor = SourceExecutor(this)
        sourceCommand.setExecutor(sourceExecutor)
        sourceCommand.tabCompleter = sourceExecutor
        val adminCommand = getCommand("sourceadmin")!!
        val adminExecutor = AdminExecutor(this)
        adminCommand.setExecutor(adminExecutor)
        adminCommand.tabCompleter = adminExecutor
        server.pluginManager.registerEvents(SourceListener(this), this)
    }

    fun reload() {
        saveDefaultConfig()
        reloadConfig()
        sourceConfig = SourceConfig(config)
        pluginSources = PluginSources(sourceConfig, server.pluginManager.plugins)
        writeConfigToFile()
    }

    fun setSource(plugin: String, source: String) {
        val url = URL(source).toExternalForm()
        val newConfig = sourceConfig.setSource(plugin, url)
        val newSources = PluginSources(newConfig, server.pluginManager.plugins)
        sourceConfig = newConfig
        pluginSources = newSources
        writeConfigToFile()
    }

    private fun writeConfigToFile() {
        config.setAll(sourceConfig.asMap())
        saveConfig()
    }

    private companion object {
        fun ConfigurationSection.setAll(map: Map<String, Any>) {
            for (entry in map)
                this[entry.key] = entry.value
            for (key in getKeys(false))
                if (key !in map)
                    this[key] = null
        }
    }
}
