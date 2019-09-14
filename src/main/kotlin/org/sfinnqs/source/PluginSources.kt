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
package org.sfinnqs.source

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import net.jcip.annotations.Immutable
import org.bukkit.plugin.Plugin
import org.sfinnqs.source.config.SourceConfig
import org.sfinnqs.source.util.UnmodifiableMap
import java.net.MalformedURLException
import java.net.URL
import java.util.*

@Immutable
data class PluginSources(val map: UnmodifiableMap<String, String>, val bookData: String) {
    constructor(config: SourceConfig, plugins: Array<Plugin>) : this(getSources(config, plugins), getBookData(config, getSources(config, plugins)))

    private val capitalization = map.keys.associateBy { it.toLowerCase(Locale.ROOT) }

    operator fun get(pluginName: String): NameAndSource? {
        val result = map[pluginName]
        if (result != null) return NameAndSource(pluginName, result)
        val realName = capitalization[pluginName.toLowerCase(Locale.ROOT)]
                ?: return null
        return NameAndSource(realName, map[realName]!!)
    }

    val plugins: Set<String>
        get() = map.keys

    private companion object {
        val adapter: JsonAdapter<Any> = Moshi.Builder().build().adapter(Any::class.java)
        fun getSources(config: SourceConfig, plugins: Array<Plugin>): UnmodifiableMap<String, String> {
            val serverType = config.serverType
            val configSources = config.sources
            val missing = mutableSetOf<String>()
            val badUrls = mutableMapOf<String, MalformedURLException>()
            val result = mutableMapOf<String, String>()

            fun addSource(plugin: String, source: String?) {
                if (source == null)
                    missing.add(plugin)
                else
                    try {
                        result[plugin] = URL(source).toExternalForm()
                    } catch (e: MalformedURLException) {
                        badUrls[serverType] = e
                    }

            }

            addSource(serverType, configSources[serverType])
            for (plugin in plugins) {
                val pluginName = plugin.name
                val pluginSource = configSources[pluginName]
                        ?: (plugin as? OpenSource)?.source
                addSource(pluginName, pluginSource)
            }
            if (missing.isNotEmpty())
                throw SourcesUnavailableException(missing)
            if (badUrls.isNotEmpty())
                throw BadUrlException(badUrls)
            return UnmodifiableMap(result)
        }

        fun getBookData(config: SourceConfig, sources: UnmodifiableMap<String, String>): String {
            val bookConfig = config.offer.book
            val firstPage = bookConfig.firstPage
            val pages = mutableListOf(firstPage)
            val sourcePage = mutableListOf<Any>()
            sources.flatMapTo(sourcePage) { (plugin, source) ->
                listOf(
                        "\n- ",
                        mapOf(
                                "text" to plugin,
                                "bold" to true,
                                "underlined" to true,
                                "color" to "blue",
                                "clickEvent" to mapOf(
                                        "action" to "open_url",
                                        "value" to source
                                ),
                                "hoverEvent" to mapOf(
                                        "action" to "show_text",
                                        "value" to "$plugin source code"
                                )
                        )
                )
            }
            pages.add(adapter.toJson(sourcePage))
            val obj = mapOf(
                    "title" to bookConfig.title,
                    "author" to bookConfig.author,
                    "pages" to pages
            )
            return adapter.toJson(obj)
        }
    }

}
