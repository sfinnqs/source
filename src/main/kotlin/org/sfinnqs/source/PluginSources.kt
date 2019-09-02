package org.sfinnqs.source

import net.jcip.annotations.NotThreadSafe

@NotThreadSafe
class PluginSources(private val sourcePlugin: SourcePlugin) {

    val sources: Map<String, String>
    get() {
        val result = mutableMapOf<String, String>()
        val config = sourcePlugin.sourceConfig
        val configSources = config.sources
        val serverType = config.serverType
        if (serverType != null) {
            val serverSource = configSources[serverType]
            if (serverSource != null) result[serverType] = serverSource
        }
        for (plugin in sourcePlugin.server.pluginManager.plugins) {
            val pluginName = plugin.name
            val pluginSource = configSources[pluginName] ?: (plugin as? OpenSource)?.source
            if (pluginSource != null) result[pluginName] = pluginSource
        }
        return result
    }

    fun getSource(pluginName: String): NameAndSource {
        val config = sourcePlugin.sourceConfig
        val manager = sourcePlugin.server.pluginManager
        val exactPlugin = manager.getPlugin(pluginName)
        val (plugin, officialName) = if (exactPlugin == null) {
            val serverType = config.serverType
            if (pluginName == serverType) {
                null to serverType
            } else {
                val inexactPlugin = manager.plugins.firstOrNull {
                    it.name.equals(pluginName, true)
                }
                if (inexactPlugin == null)
                    if (pluginName.equals(serverType, true))
                        null to serverType
                    else
                        return NameAndSource(null, null)
                else
                    inexactPlugin to inexactPlugin.name
            }
        } else {
            exactPlugin to exactPlugin.name
        }
        val source = config.sources[officialName] ?: (plugin as? OpenSource)?.source
        return NameAndSource(officialName, source)
    }

    val allPlugins: Set<String>
        get() {
            val result = mutableSetOf<String>()
            sourcePlugin.sourceConfig.serverType?.let { result.add(it) }
            result.addAll(sourcePlugin.server.pluginManager.plugins.map { it.name })
            return result
        }

    val plugins: Set<String>
        get() {
            val result = mutableSetOf<String>()
            val configSources = sourcePlugin.sourceConfig.sources
            val serverType = sourcePlugin.sourceConfig.serverType
            if (serverType != null && serverType in configSources)
                result.add(serverType)
            val pluginNames = sourcePlugin.server.pluginManager.plugins.filter {
                it is OpenSource || it.name in configSources
            }.map {
                it.name
            }
            result.addAll(pluginNames)
            return result
        }

}