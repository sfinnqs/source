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
import com.squareup.moshi.Types
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toImmutableMap
import net.jcip.annotations.Immutable
import okio.Okio
import org.bukkit.plugin.Plugin
import org.sfinnqs.source.config.SourceConfig
import java.net.MalformedURLException
import java.net.URL
import java.util.*

@Immutable
data class PluginSources(val map: ImmutableMap<String, String>, val bookData: String) {
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

        val fontWidths = run {
            val type = Types.newParameterizedType(Map::class.java, Character::class.java, Integer::class.java)
            val fontWidthAdapter = Moshi.Builder().build().adapter<Map<Char, Int>>(type)
            // https://bukkit.org/threads/formatting-plugin-output-text-into-columns.8481/#post-133295
            this::class.java.getResourceAsStream("/font-widths.json").use { stream ->
                Okio.source(stream).use { source ->
                    Okio.buffer(source).use { buffered ->
                        fontWidthAdapter.fromJson(buffered)
                    }
                }
            }!!
        }

        fun getSources(config: SourceConfig, plugins: Array<Plugin>): ImmutableMap<String, String> {
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
                        badUrls[plugin] = e
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
                throw BadUrlException(badUrls.toImmutableMap())
            return result.toImmutableMap()
        }

        fun getBookData(config: SourceConfig, sources: Map<String, String>): String {
            val bookConfig = config.offer.book
            val firstPage = bookConfig.firstPage
            val pages = mutableListOf(firstPage)
            pages.addAll(getSourcePages(sources.map { NameAndSource(it.key, it.value) }))
            val obj = mapOf(
                    "title" to bookConfig.title,
                    "author" to bookConfig.author,
                    "pages" to pages
            )
            return adapter.toJson(obj)
        }

        fun getSourcePages(plugins: List<NameAndSource>): List<String> {
            val pageText = mutableListOf<Any>()
            var linesLeft = TOTAL_LINES
            for ((i, nameAndSource) in plugins.withIndex()) {
                val (plugin, source) = nameAndSource
                val (text, lines) = getPluginLines(plugin, source)
                if (lines > linesLeft) {
                    val pageString = adapter.toJson(pageText)
                    return listOf(pageString) + getSourcePages(plugins.subList(i, plugins.size))
                } else {
                    if (i != 0) pageText.add('\n')
                    pageText.addAll(text)
                    linesLeft -= lines
                }
            }
            val pageString = adapter.toJson(pageText)
            return listOf(pageString)
        }

        fun getPluginLines(plugin: String, source: String): PluginText {
            val prefix = "- "
            val (text, lines) = getPluginText(plugin, source, plugin, TOTAL_WIDTH - prefix.width)
            return PluginText(listOf(prefix) + text, lines)
        }

        fun getPluginText(plugin: String, source: String, substring: String, lineWidth: Int): PluginText {
            val builder = StringBuilder()
            var widthLeft = lineWidth
            for ((i, char) in substring.withIndex()) {
                val boldWidth = char.boldWidth
                if (boldWidth > widthLeft) {
                    val nextLinePrefix = "   "
                    val nextSubstring = substring.substring(i)
                    val nextLineWidth = TOTAL_WIDTH - nextLinePrefix.width
                    val (nextText, nextLines) = getPluginText(plugin, source, nextSubstring, nextLineWidth)
                    val toPrepend = listOf(
                            stringToLink(plugin, source, builder.toString()),
                            '\n' + nextLinePrefix
                    )
                    return PluginText(toPrepend + nextText, 1 + nextLines)
                } else {
                    builder.append(char)
                    widthLeft -= boldWidth
                }
            }
            val link = stringToLink(plugin, source, substring)
            return PluginText(listOf(link), 1)
        }

        fun stringToLink(plugin: String, source: String, text: String) = mapOf(
                "text" to text,
                "bold" to true,
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

        val Char.width
            get() = fontWidths[this]
                    ?: throw IllegalArgumentException(this.toString())

        val Char.boldWidth
            get() = width + 1

        val String.width
            get() = sumBy { it.width }

        const val TOTAL_WIDTH = 114
        const val TOTAL_LINES = 14

    }

    private data class PluginText(val textObject: List<Any>, val lineCount: Int)

}
