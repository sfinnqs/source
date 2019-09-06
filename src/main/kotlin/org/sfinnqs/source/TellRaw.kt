package org.sfinnqs.source

import com.google.gson.Gson
import org.bukkit.command.CommandSender

fun CommandSender.tellRaw(message: Any) {
    val messageText = Gson().toJson(message)
    // https://stackoverflow.com/a/34636083
    server.dispatchCommand(server.consoleSender, "tellraw $name $messageText")
}

