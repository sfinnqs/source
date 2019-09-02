package org.sfinnqs.source.command

import org.bukkit.ChatColor.ITALIC
import org.bukkit.ChatColor.RED
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import org.sfinnqs.source.SourcePlugin

class AdminExecutor(private val sourcePlugin: SourcePlugin) : TabExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        val usage = "Usage: $ITALIC/$label <reload | add | remove>"
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
            subCommand.equals("add", true) -> {
                val addUsage = "Usage: $ITALIC/$label $subCommand <plugin> <source-code>"
                if (args.size < 3) {
                    val error = "${RED}Not enough arguments"
                    sender.sendMessage(arrayOf(error, addUsage))
                    return true
                } else if (args.size >= 4) {
                    val error = "${RED}Too many arguments"
                    sender.sendMessage(arrayOf(error, addUsage))
                    return true
                }
                val pluginName = args[1]
                val sourceCode = args[2]
                TODO()
            }
            subCommand.equals("remove", true) -> {
                val removeUsage = "Usage: $ITALIC/$label $subCommand <plugin>"
                if (args.size < 2) {
                    val error = "${RED}Not enough arguments"
                    sender.sendMessage(arrayOf(error, removeUsage))
                    return true
                } else if (args.size >= 3) {
                    val error = "${RED}Too many arguments"
                    sender.sendMessage(arrayOf(error, removeUsage))
                    return true
                }
                val pluginName = args[1]
                TODO()
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
                if (subCommand.matches("add")) result.add("add")
                if (subCommand.matches("remove")) result.add("remove")
                result
            }
            2 -> when {
                subCommand.equals("add", true) -> pluginSources.allPlugins.toList()
                subCommand.equals("remove", true) -> pluginSources.plugins.toList()
                else -> emptyList()
            }
            else -> emptyList()
        }
    }

    private companion object {
        fun String.matches(completion: String) = completion.startsWith(this, true) && !completion.equals(this, true)
    }

}