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
package org.sfinnqs.source.config

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import net.jcip.annotations.Immutable
import org.bukkit.configuration.ConfigurationSection
import org.sfinnqs.source.logger

@Immutable
data class BookConfig(val title: String, val author: String, val firstPage: String) {
    constructor(config: ConfigurationSection) : this(config.title, config.author, config.firstPage)

    fun asMap() = mapOf(
            "title" to title,
            "author" to author,
            "first page" to adapter.fromJson(firstPage)!!
    )

    companion object {
        val adapter: JsonAdapter<Any> = Moshi.Builder().build().adapter(Any::class.java)
        private val ConfigurationSection.title: String
            get() {
                val result = getString("title", null)
                if (result != null) return result
                logger.warning("book title should be specified in your config")
                return defaultSection!!.getString("title")!!
            }
        private val ConfigurationSection.author: String
            get() {
                val result = getString("author", null)
                if (result != null) return result
                logger.warning("book author should be specified in your config")
                return defaultSection!!.getString("author")!!
            }
        private val ConfigurationSection.firstPage: String
            get() {
                val obj = this["first page"] ?: run {
                    logger.warning("book first page should be specified in your config")
                    defaultSection!!["first page"]!!
                }
                return adapter.toJson(obj)
            }
    }
}
