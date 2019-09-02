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