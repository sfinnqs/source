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
import org.bukkit.configuration.Configuration
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.InvalidConfigurationException
import org.sfinnqs.source.util.UnmodifiableMap

@Immutable
data class SourceConfig(val serverType: String, val offer: Offer, val chatOffer: String, val sources: UnmodifiableMap<String, String>) {
    constructor(config: Configuration) : this(config.serverType, config.offer, config.chatOffer, config.sources)

    fun asMap(): Map<String, Any> {
        val chatObject = adapter.fromJson(chatOffer)!!
        val result = mutableMapOf("server type" to serverType, "offer" to offer.toString(), "chat offer" to chatObject)
        val sourcesOrNull = sources.takeIf { it.isNotEmpty() }
        if (sourcesOrNull != null) result["sources"] = sourcesOrNull
        return result
    }

    private companion object {
        val adapter: JsonAdapter<Any> = Moshi.Builder().build().adapter(Any::class.java)
        val Configuration.serverType
            get() = getString("server type", null)
                    ?: throw InvalidConfigurationException("server type must be specified in config")
        val Configuration.offer: Offer
            get() {
                val offerString = getString("offer", null)
                return if (offerString == null) {
                    logger.warning("offer should be specified in config; defaulting to chat")
                    Offer.CHAT
                } else {
                    val result = Offer.fromString(offerString)
                    if (result == null) {
                        logger.warning {
                            "offer \"$offerString\" not recognized in config; defaulting to chat"
                        }
                        Offer.CHAT
                    } else {
                        result
                    }
                }
            }
        val Configuration.chatOffer: String
            get() {
                val obj = this["chat offer"] ?: defaultSection!!["chat offer"]!!
                return adapter.toJson(obj)
            }
        val Configuration.sources: UnmodifiableMap<String, String>
            get() {
                val section = getSectionOrSet("sources")
                val result = mutableMapOf<String, String>()
                for (pluginName in section.getKeys(false)) {
                    val source = section.getString(pluginName) ?: continue
                    result[pluginName] = source
                }
                return UnmodifiableMap(result)
            }

        fun ConfigurationSection.getSectionOrSet(path: String): ConfigurationSection {
            val result = getConfigurationSection(path)
                    ?: return createSection(path)
            return if (isSet(path) && isConfigurationSection(path)) {
                result
            } else {
                val default = result.defaultSection
                        ?: return createSection(path)
                createSection(path, default.getValues(true))
            }
        }
    }
}
