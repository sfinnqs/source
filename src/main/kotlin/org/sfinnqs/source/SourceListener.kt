package org.sfinnqs.source

import jdk.nashorn.internal.ir.annotations.Immutable
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

@Immutable
class SourceListener(private val config: SourceConfig): Listener {
    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        event.player.sendMessage(config.joinMessage)
    }
}