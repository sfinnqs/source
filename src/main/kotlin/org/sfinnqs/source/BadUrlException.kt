package org.sfinnqs.source

import net.jcip.annotations.ThreadSafe
import org.bukkit.configuration.InvalidConfigurationException
import java.net.MalformedURLException

@ThreadSafe
class BadUrlException(val causes: Map<String, MalformedURLException>) : InvalidConfigurationException(causes.values.first())
