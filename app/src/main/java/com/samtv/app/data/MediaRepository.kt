package com.samtv.app.data

import com.samtv.app.config.AppConfig
import java.util.concurrent.ConcurrentHashMap

object MediaRepository {
    // ConcurrentHashMap disallows null values — use "" for missing
    private val posterCache = ConcurrentHashMap<String, String>()
    private val videoCache = ConcurrentHashMap<String, String>()

    fun listYears(serverId: String, categoryPath: String): List<DirEntry> {
        val url = DirectoryClient.joinUrl(AppConfig.baseUrl(serverId), categoryPath)
        return DirectoryClient.list(url)
            .filter { it.isDirectory }
            .sortedWith(
                compareByDescending<DirEntry> { yearSortKey(it.name) }
                    .thenByDescending { it.name },
            )
    }

    fun listMovies(yearUrl: String): List<MovieItem> {
        return DirectoryClient.list(yearUrl)
            .filter { it.isDirectory }
            .map { MovieItem(title = it.name, folderUrl = it.url) }
            .sortedBy { it.title.lowercase() }
    }

    fun posterUrl(folderUrl: String): String? {
        val cached = posterCache.getOrPut(folderUrl) {
            runCatching { DirectoryClient.findPosterUrl(folderUrl) }.getOrNull().orEmpty()
        }
        return cached.takeIf { it.isNotEmpty() }
    }

    fun videoUrl(folderUrl: String): String? {
        val cached = videoCache.getOrPut(folderUrl) {
            runCatching { DirectoryClient.findVideoUrl(folderUrl) }.getOrNull().orEmpty()
        }
        return cached.takeIf { it.isNotEmpty() }
    }

    /** Prefer real year folders like (2025) at the top. */
    private fun yearSortKey(name: String): Int {
        val match = Regex("""\((\d{4})""").find(name)
        return match?.groupValues?.get(1)?.toIntOrNull() ?: -1
    }
}
