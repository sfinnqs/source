package org.sfinnqs.source

import net.jcip.annotations.Immutable
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

@Immutable
class SourceListener(private val config: SourceConfig) : Listener {
    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        event.player.tellRaw(config.joinMessage)
    }
}