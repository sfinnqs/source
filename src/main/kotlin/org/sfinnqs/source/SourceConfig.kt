package org.sfinnqs.source

import net.jcip.annotations.Immutable
import org.bukkit.configuration.Configuration
import org.bukkit.configuration.ConfigurationSection

@Immutable
class SourceConfig(config: Configuration) {
    val serverType = config.getString("server type", null)
    val joinMessage = config.getString("join message")!!
    val sources = createStringMap(config.getSectionOrSet("sources"))
    fun asMap(): Map<String, Any> {
        val result = mutableMapOf<String, Any>()
        if (serverType != null) result["server type"] = serverType
        result["join message"] = joinMessage
        val sourcesOrNull = sources.takeIf { it.isNotEmpty() }
        if (sourcesOrNull != null) result["sources"] = sourcesOrNull
        return result
    }

    private companion object {
        fun createStringMap(config: ConfigurationSection): Map<String, String> {
            val result = mutableMapOf<String, String>()
            for (pluginName in config.getKeys(false)) {
                val source = config.getString(pluginName) ?: continue
                result[pluginName] = source
            }
            return result
        }

        fun ConfigurationSection.getSectionOrSet(path: String): ConfigurationSection {
            val result = getConfigurationSection(path) ?: return createSection(path)
            return if (isSet(path) && isConfigurationSection(path)) {
                result
            } else {
                val default = result.defaultSection ?: return createSection(path)
                createSection(path, default.getValues(true))
            }
        }

    }
}