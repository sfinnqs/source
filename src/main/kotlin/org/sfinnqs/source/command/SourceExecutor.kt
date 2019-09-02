package org.sfinnqs.source.command

import com.google.gson.Gson
import net.jcip.annotations.NotThreadSafe
import org.bukkit.ChatColor.*
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import org.sfinnqs.source.SourcePlugin

@NotThreadSafe
class SourceExecutor(private val sourcePlugin: SourcePlugin) : TabExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String?>): Boolean {
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
        val messageObject = mutableListOf<Any>("Click the links below to see the corresponding source: \n")
        var first = true
        for ((pluginName, source) in sourcePlugin.pluginSources.sources) {
            if (first) {
                first = false
            } else {
                messageObject.add(", ")
            }
            val pluginObject = mapOf(
                    "text" to pluginName,
                    "bold" to true,
                    "underline" to true,
                    "color" to "blue",
                    "clickEvent" to mapOf("action" to "open_url", "value" to source),
                    "hoverEvent" to mapOf(
                            "action" to "show_text",
                            "value" to listOf(mapOf("text" to pluginName, "bold" to false), " source code")
                    )
            )
            messageObject.add(pluginObject)
        }
        sender.tellRaw(messageObject)
    }

    private fun sendSource(sender: CommandSender, pluginName: String, usage: String) {
        val (name, source) = sourcePlugin.pluginSources.getSource(pluginName)
        if (name == null) {
            val error = "${RED}\"$pluginName\" not found"
            sender.sendMessage(arrayOf(error, usage))
            return
        }
        if (source == null) {
            val error = "${RED}The source code of $BOLD$name$RESET$RED could not be found."
            val suggestion = "Please contact the server administrator if you believe that this is in error."
            sender.sendMessage(arrayOf(error, suggestion))
            return
        }
        val messageObject = listOf(
                "The source code of ",
                mapOf("text" to name, "bold" to true),
                " is available at ",
                mapOf(
                        "text" to source,
                        "underlined" to true,
                        "color" to "blue",
                        "clickEvent" to mapOf("action" to "open_url", "value" to source),
                        "hoverEvent" to mapOf(
                                "action" to "show_text",
                                "value" to listOf(mapOf("text" to name, "bold" to true), " source code")
                        )
                )
        )
        sender.tellRaw(messageObject)
    }

    private fun CommandSender.tellRaw(message: Any) {
        val messageText = Gson().toJson(message)
        // https://stackoverflow.com/a/34636083
        server.dispatchCommand(server.consoleSender, "tellraw $name $messageText")
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