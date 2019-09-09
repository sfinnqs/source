/**
 * The Source plugin - A Bukkit plugin for sharing source code
 * Copyright (C) 2019 Finn Voichick
 *
 * This file is part of the Source plugin.
 *
 * The Source plugin is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published by the
 * Free Software Foundation, version 3.
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

import net.jcip.annotations.Immutable
import org.bukkit.configuration.Configuration
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.InvalidConfigurationException

@Immutable
data class SourceConfig(val serverType: String, val offer: Offer, val chatOffer: Any, val sources: Map<String, String>) {
    constructor(config: Configuration) : this(config.serverType, config.offer, config.chatOffer, config.sources)

    fun asMap(): Map<String, Any> {
        val result = mutableMapOf("server type" to serverType, "offer" to offer.toString(), "chat offer" to chatOffer)
        val sourcesOrNull = sources.takeIf { it.isNotEmpty() }
        if (sourcesOrNull != null) result["sources"] = sourcesOrNull
        return result
    }

    private companion object {
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
        val Configuration.chatOffer
            get() = this["chat offer"] ?: defaultSection!!["chat offer"]!!
        val Configuration.sources
            get() = createStringMap(getSectionOrSet("sources"))

        fun createStringMap(config: ConfigurationSection): Map<String, String> {
            val result = mutableMapOf<String, String>()
            for (pluginName in config.getKeys(false)) {
                val source = config.getString(pluginName) ?: continue
                result[pluginName] = source
            }
            return result
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