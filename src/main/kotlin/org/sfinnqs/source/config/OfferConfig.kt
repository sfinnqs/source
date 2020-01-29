/**
 * The Source plugin - A Bukkit plugin for sharing source code
 * Copyright (C) 2020 sfinnqs
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
import org.sfinnqs.source.Offer
import org.sfinnqs.source.logger

@Immutable
data class OfferConfig(
    val type: Offer,
    val chat: String,
    val book: BookConfig
) {
    constructor(config: ConfigurationSection) : this(
        config.type,
        config.chat,
        config.book
    )

    fun asMap(): Map<String, Any> = mapOf(
        "type" to type.toString(),
        "chat" to adapter.fromJson(chat)!!,
        "book" to book.asMap()
    )

    private companion object {
        val adapter: JsonAdapter<Any> =
            Moshi.Builder().build().adapter(Any::class.java)
        val ConfigurationSection.type: Offer
            get() {
                val offerString = getString("type", null) ?: run {
                    logger.warning("offer should be specified in config; defaulting to chat")
                    defaultSection!!.getString("type")!!
                }
                val result = Offer.fromString(offerString)
                if (result != null) return result
                logger.warning {
                    "offer \"$offerString\" not recognized in config"
                }
                return Offer.CHAT
            }
        val ConfigurationSection.chat: String
            get() {
                val obj = this["chat"] ?: defaultSection!!["chat"]!!
                return adapter.toJson(obj)
            }
        val ConfigurationSection.book
            get() = BookConfig(getSectionOrEmpty("book"))
    }
}
