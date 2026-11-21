package com.samtv.app.config

import com.samtv.app.BuildConfig

/**
 * Runtime config. Sensitive values come from local.properties → BuildConfig.
 * Switch servers by changing BASE_URL only.
 */
object AppConfig {
    val baseUrl: String
        get() = BuildConfig.BASE_URL.trimEnd('/') + "/"
}
