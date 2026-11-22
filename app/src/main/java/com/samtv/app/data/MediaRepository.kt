package com.samtv.app.data

import com.samtv.app.config.AppConfig
import java.util.concurrent.ConcurrentHashMap

object MediaRepository {
    // ConcurrentHashMap disallows null values — use "" for missing
    private val posterCache = ConcurrentHashMap<String, String>()
    private val videoCache = ConcurrentHashMap<String, String>()

    private val yearNameRegex = Regex("""^\(\d{4}""")
    /** e.g. "TV Series ★ 0 — 9", "… A — L" */
    private val indexRangeRegex = Regex("""[0-9A-Za-z]\s*[—–\-]\s*[0-9A-Za-z]""")

    fun categoryUrl(serverId: String, categoryPath: String): String =
        DirectoryClient.joinUrl(AppConfig.baseUrl(serverId), categoryPath)

    fun listYears(serverId: String, categoryPath: String): List<DirEntry> {
        return DirectoryClient.list(categoryUrl(serverId, categoryPath))
            .filter { it.isDirectory }
            .sortedWith(
                compareByDescending<DirEntry> { yearSortKey(it.name) }
                    .thenByDescending { it.name },
            )
    }

    fun listMovies(yearUrl: String): List<MovieItem> {
        return DirectoryClient.list(yearUrl)
            .filter { it.isDirectory }
            .map {
                MovieItem(
                    title = it.name,
                    folderUrl = it.url,
                    modifiedAt = it.modifiedAt,
                )
            }
    }

    fun sortedMovies(movies: List<MovieItem>, sort: MovieSort): List<MovieItem> {
        return when (sort) {
            MovieSort.NEWEST -> movies.sortedWith(
                compareByDescending<MovieItem> { it.modifiedAt }
                    .thenBy { it.title.lowercase() },
            )
            MovieSort.NAME -> movies.sortedBy { it.title.lowercase() }
        }
    }

    /**
     * True when most folders are browse buckets:
     * year-style "(2025)" OR index-range "A — L" / "0 — 9" (TV Series).
     */
    fun looksLikeYearList(entries: List<DirEntry>): Boolean {
        if (entries.isEmpty()) return true
        val browseLike = entries.count {
            isYearFolderName(it.name) || isIndexRangeFolderName(it.name)
        }
        return browseLike * 2 >= entries.size
    }

    fun isYearFolderName(name: String): Boolean {
        val trimmed = name.trim()
        if (yearNameRegex.containsMatchIn(trimmed)) return true
        return trimmed.contains("Before", ignoreCase = true) && trimmed.startsWith("(")
    }

    fun isIndexRangeFolderName(name: String): Boolean =
        indexRangeRegex.containsMatchIn(name)

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

    private fun yearSortKey(name: String): Int {
        val match = Regex("""\((\d{4})""").find(name)
        return match?.groupValues?.get(1)?.toIntOrNull() ?: -1
    }
}
