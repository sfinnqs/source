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
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.plugin.java.JavaPlugin
import org.sfinnqs.source.command.AdminExecutor
import org.sfinnqs.source.command.SourceExecutor

@NotThreadSafe
class SourcePlugin : JavaPlugin(), OpenSource {
    override fun getSource() = "https://github.com/sfinnqs/source"
    private var privateConfig: SourceConfig? = null
    val sourceConfig
        get() = privateConfig ?: reload()
    val pluginSources = PluginSources(this)

    override fun onEnable() {
        val sourceCommand = getCommand("source")!!
        val sourceExecutor = SourceExecutor(this)
        sourceCommand.setExecutor(sourceExecutor)
        sourceCommand.tabCompleter = sourceExecutor
        val adminCommand = getCommand("sourceadmin")!!
        val adminExecutor = AdminExecutor(this)
        adminCommand.setExecutor(adminExecutor)
        adminCommand.tabCompleter = adminExecutor
        server.pluginManager.registerEvents(SourceListener(sourceConfig), this)
    }

    fun reload(): SourceConfig {
        saveDefaultConfig()
        reloadConfig()
        val newConfig = SourceConfig(config)
        privateConfig = newConfig
        writeConfigToFile(newConfig)
        return newConfig
    }

    private fun writeConfigToFile(sourceConfig: SourceConfig) {
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
