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

import net.jcip.annotations.ThreadSafe
import org.bukkit.configuration.InvalidConfigurationException

@ThreadSafe
class SourcesUnavailableException(plugins: Set<String>) : InvalidConfigurationException(getMessage(plugins)) {
    private companion object {
        fun getMessage(plugins: Set<String>) = when (plugins.size) {
            0 -> throw IllegalArgumentException("plugins cannot be empty")
            1 -> "Source code for ${plugins.first()} must be specified in your config"
            2 -> "Source code for ${plugins.first()} and ${plugins.last()} must be specified in your config"
            else -> "Source code for the following must be specified in your config: ${plugins.joinToString()}"
        }
    }
}
