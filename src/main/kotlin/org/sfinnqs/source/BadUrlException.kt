package org.sfinnqs.source

import kotlinx.collections.immutable.ImmutableMap
import net.jcip.annotations.ThreadSafe
import org.bukkit.configuration.InvalidConfigurationException
import java.net.MalformedURLException

@ThreadSafe
class BadUrlException(val causes: ImmutableMap<String, MalformedURLException>) : InvalidConfigurationException(causes.values.first())
