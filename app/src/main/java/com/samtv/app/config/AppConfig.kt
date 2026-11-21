package com.samtv.app.config

import com.samtv.app.BuildConfig

/**
 * Runtime config. Server roots come from local.properties → BuildConfig.
 * Categories pick a serverId ("7" / "14"); paths stay relative.
 */
object AppConfig {
    fun baseUrl(serverId: String): String {
        val raw = when (serverId.trim()) {
            "14" -> BuildConfig.SERVER_14
            else -> BuildConfig.SERVER_7
        }
        return raw.trimEnd('/') + "/"
    }
}
