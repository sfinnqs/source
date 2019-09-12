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
package org.sfinnqs.source.command

import net.jcip.annotations.NotThreadSafe
import org.bukkit.ChatColor.ITALIC
import org.bukkit.ChatColor.RED
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import org.sfinnqs.source.SourcePlugin
import org.sfinnqs.source.logger
import java.net.MalformedURLException

@NotThreadSafe
class AdminExecutor(private val sourcePlugin: SourcePlugin) : TabExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        val usage = "Usage: $ITALIC/$label <reload | set>"
        val subCommand = args.getOrNull(0)
        if (subCommand == null) {
            val error = "${RED}Not enough arguments"
            sender.sendMessage(arrayOf(error, usage))
            return true
        }
        when {
            subCommand.equals("reload", true) -> {
                if (args.size >= 2) {
                    val error = "${RED}Too many arguments"
                    val reloadUsage = "Usage: $ITALIC/$label reload"
                    sender.sendMessage(arrayOf(error, reloadUsage))
                    return true
                }
                sourcePlugin.reload()
                sender.sendMessage("Source reloaded")
            }
            subCommand.equals("set", true) -> {
                val setUsage = "Usage: $ITALIC/$label $subCommand <plugin> <source-code>"
                if (args.size < 3) {
                    val error = "${RED}Not enough arguments"
                    sender.sendMessage(arrayOf(error, setUsage))
                    return true
                } else if (args.size >= 4) {
                    val error = "${RED}Too many arguments"
                    sender.sendMessage(arrayOf(error, setUsage))
                    return true
                }
                val plugin = args[1]
                val source = args[2]
                val senderName = sender.name
                logger.info {
                    "Writing config to file because $senderName updated it"
                }
                try {
                    sourcePlugin.setSource(plugin, source)
                } catch (e: MalformedURLException) {
                    val error = "$RED\"$source\" was unrecognized as a URL"
                    val error2 = RED.toString() + e.message
                    sender.sendMessage(arrayOf(error, error2, setUsage))
                    return true
                }
                sender.sendMessage("Source config updated")
            }
            else -> {
                val error = "${RED}Unrecognized argument: \"$subCommand\""
                sender.sendMessage(arrayOf(error, usage))
                return true
            }
        }
        return true
    }

    override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<String>): List<String> {
        val subCommand = args[0]
        val pluginSources = sourcePlugin.pluginSources
        return when (args.size) {
            1 -> {
                val result = mutableListOf<String>()
                if (subCommand.matches("reload")) result.add("reload")
                if (subCommand.matches("set")) result.add("set")
                result
            }
            2 -> when {
                subCommand.equals("set", true) -> pluginSources.plugins.toList()
                else -> emptyList()
            }
            else -> emptyList()
        }
    }

    private companion object {
        fun String.matches(completion: String) = completion.startsWith(this, true) && !completion.equals(this, true)
    }

}
