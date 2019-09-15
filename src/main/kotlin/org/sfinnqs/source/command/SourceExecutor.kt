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
import org.bukkit.ChatColor.*
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player
import org.sfinnqs.source.SourcePlugin
import org.sfinnqs.source.tellRaw

@NotThreadSafe
class SourceExecutor(private val sourcePlugin: SourcePlugin) : TabExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        val numArgs = args.size
        val usage = "Usage: /$label [plugin]"
        if (numArgs >= 2) {
            val error = "${RED}Too many arguments"
            sender.sendMessage(arrayOf(error, usage))
            return true
        }
        val firstArg = args.getOrNull(0)
        if (firstArg == null)
            sendAllSources(sender)
        else
            sendSource(sender, firstArg, usage)
        return true
    }

    private fun sendAllSources(sender: CommandSender) {
        if (sender is Player) {
            val messageObject = mutableListOf<Any>("Click the links below to see the corresponding source code:")
            sourcePlugin.pluginSources.map.flatMapTo(messageObject) { (pluginName, source) ->
                listOf(
                        "\n- ",
                        mapOf(
                                "text" to pluginName,
                                "bold" to true,
                                "color" to "blue",
                                "clickEvent" to mapOf("action" to "open_url", "value" to source),
                                "hoverEvent" to mapOf("action" to "show_text", "value" to "$pluginName source code")
                        )
                )
            }
            sender.tellRaw(messageObject)
        } else {
            val messages = mutableListOf("The source code is available at the links below:")
            sourcePlugin.pluginSources.map.mapTo(messages) { (pluginName, source) ->
                "- $BOLD$pluginName$RESET: $UNDERLINE$source"
            }
            sender.sendMessage(messages.toTypedArray())
        }
    }

    private fun sendSource(sender: CommandSender, pluginName: String, usage: String) {
        val nameAndSource = sourcePlugin.pluginSources[pluginName]
        if (nameAndSource == null) {
            val error = "${RED}\"$pluginName\" not found"
            sender.sendMessage(arrayOf(error, usage))
            return
        }
        val (name, source) = nameAndSource
        if (sender is Player) {
            val messageObject = listOf(
                    "The source code of ",
                    mapOf("text" to name, "bold" to true),
                    " is available at ",
                    mapOf(
                            "text" to source,
                            "underlined" to true,
                            "color" to "blue",
                            "clickEvent" to mapOf("action" to "open_url", "value" to source),
                            "hoverEvent" to mapOf("action" to "show_text", "value" to "$name source code")
                    )
            )
            sender.tellRaw(messageObject)
        } else {
            val message = "The source code of $BOLD$name$RESET is available at $UNDERLINE$source"
            sender.sendMessage(message)
        }
    }

    override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<String>): List<String> {
        if (args.size != 1) return emptyList()
        val pluginName = args[0]
        return sourcePlugin.pluginSources.plugins.filter { pluginName.matches(it) }
    }


    private companion object {
        fun String.matches(completion: String) = completion.startsWith(this, true) && !completion.equals(this, true)
    }
}
