package com.samtv.app.data

import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

object DirectoryClient {
    private val hrefRegex = Regex(
        """href\s*=\s*["']([^"'#]+)["']""",
        RegexOption.IGNORE_CASE,
    )

    private val junkNames = setOf(
        "h5ai", "_h5ai", "css", "js", "fonts", "images", "img",
        "assets", "static", "favicon.ico", "robots.txt",
    )

    fun joinUrl(base: String, vararg parts: String): String {
        var url = base.trimEnd('/')
        for (part in parts) {
            val clean = part.trim('/')
            if (clean.isEmpty()) continue
            val encoded = clean.split('/').joinToString("/") { segment ->
                URLEncoder.encode(segment, StandardCharsets.UTF_8)
                    .replace("+", "%20")
            }
            url = "$url/$encoded"
        }
        return "$url/"
    }

    fun list(url: String): List<DirEntry> {
        val fromApi = runCatching { listViaH5ai(url) }.getOrNull()
        if (!fromApi.isNullOrEmpty()) {
            return fromApi.filterVisible(url)
        }
        return parseHtml(fetch(url), url).filterVisible(url)
    }

    fun findPosterUrl(folderUrl: String): String? {
        val images = list(folderUrl).filter { entry ->
            !entry.isDirectory && isImageFile(entry.name)
        }
        return images.firstOrNull { entry ->
            entry.name.endsWith(".jpg", true) || entry.name.endsWith(".jpeg", true)
        }?.url ?: images.firstOrNull()?.url
    }

    fun findVideoUrl(folderUrl: String): String? {
        val videos = list(folderUrl).filter { entry ->
            !entry.isDirectory && isVideoFile(entry.name)
        }
        return videos.firstOrNull { entry ->
            val name = entry.name.lowercase()
            !name.contains("sample") && !name.contains("trailer")
        }?.url ?: videos.firstOrNull()?.url
    }

    private fun listViaH5ai(pageUrl: String): List<DirEntry> {
        val page = URL(pageUrl)
        val origin = "${page.protocol}://${page.authority}"
        val href = URLDecoder.decode(page.path, StandardCharsets.UTF_8).let {
            if (it.endsWith("/")) it else "$it/"
        }

        val endpoints = listOf(
            "$origin/_h5ai/public/php/api.php",
            "$origin/h5ai/public/php/api.php",
            "$origin/_h5ai/public/api.php",
        )

        var lastError: Throwable? = null
        for (endpoint in endpoints) {
            try {
                val body = JSONObject()
                    .put("action", "get")
                    .put(
                        "items",
                        JSONObject()
                            .put("href", href)
                            .put("what", 1),
                    )
                    .toString()

                val response = postJson(endpoint, body)
                val parsed = parseH5aiJson(response, origin)
                if (parsed.isNotEmpty()) return parsed
            } catch (t: Throwable) {
                lastError = t
            }
        }
        if (lastError != null) throw lastError
        return emptyList()
    }

    private fun parseH5aiJson(response: String, origin: String): List<DirEntry> {
        val root = JSONObject(response)
        val items = root.optJSONArray("items") ?: return emptyList()
        val result = mutableListOf<DirEntry>()

        for (i in 0 until items.length()) {
            val item = items.optJSONObject(i) ?: continue
            val itemHref = item.optString("href")
            if (itemHref.isBlank()) continue
            val name = URLDecoder.decode(
                itemHref.trimEnd('/').substringAfterLast('/'),
                StandardCharsets.UTF_8,
            )
            if (name.isEmpty() || name == "." || name == "..") continue

            val type = item.opt("type")
            val typeText = when (type) {
                is String -> type.lowercase()
                else -> type?.toString()?.lowercase().orEmpty()
            }
            val isDirectory = typeText.contains("folder") || itemHref.endsWith('/')

            val absolute = if (itemHref.startsWith("http")) {
                itemHref
            } else {
                origin.trimEnd('/') + "/" + itemHref.trimStart('/')
            }
            val normalized = if (isDirectory && !absolute.endsWith('/')) "$absolute/" else absolute
            result += DirEntry(
                name = name,
                url = normalized,
                isDirectory = isDirectory,
                modifiedAt = readModifiedAt(item),
            )
        }
        return result
    }

    private fun readModifiedAt(item: JSONObject): Long {
        if (!item.has("time")) return 0L
        val raw = item.opt("time") ?: return 0L
        val value = when (raw) {
            is Number -> raw.toLong()
            is String -> raw.toLongOrNull() ?: return 0L
            else -> return 0L
        }
        // h5ai usually sends unix seconds
        return if (value in 1..<1_000_000_000_000L) value * 1000L else value
    }

    private fun postJson(url: String, jsonBody: String): String {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            connectTimeout = 15_000
            readTimeout = 20_000
            requestMethod = "POST"
            doOutput = true
            setRequestProperty("Content-Type", "application/json; charset=utf-8")
            setRequestProperty("Accept", "application/json")
            setRequestProperty("User-Agent", "SamTV/0.1")
        }
        try {
            connection.outputStream.bufferedWriter(StandardCharsets.UTF_8).use { it.write(jsonBody) }
            val code = connection.responseCode
            val stream = if (code in 200..299) connection.inputStream else connection.errorStream
            val text = stream?.bufferedReader(StandardCharsets.UTF_8)?.use { it.readText() }.orEmpty()
            if (code !in 200..299) error("HTTP $code for $url")
            return text
        } finally {
            connection.disconnect()
        }
    }

    private fun fetch(url: String): String {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            connectTimeout = 15_000
            readTimeout = 20_000
            requestMethod = "GET"
            setRequestProperty("User-Agent", "SamTV/0.1")
            instanceFollowRedirects = true
        }
        try {
            val code = connection.responseCode
            if (code !in 200..299) {
                error("HTTP $code for $url")
            }
            return connection.inputStream.bufferedReader(StandardCharsets.UTF_8).use { it.readText() }
        } finally {
            connection.disconnect()
        }
    }

    private fun parseHtml(html: String, pageUrl: String): List<DirEntry> {
        val fromFallback = parseFallbackTable(html, pageUrl)
        if (fromFallback.isNotEmpty()) return fromFallback

        val base = URL(pageUrl)
        val seen = LinkedHashSet<String>()
        val result = mutableListOf<DirEntry>()

        for (match in hrefRegex.findAll(html)) {
            val raw = match.groupValues[1].trim()
            if (raw.isEmpty() || raw.startsWith("?") || raw.startsWith("javascript:", true)) {
                continue
            }

            val absolute = runCatching { URL(base, raw) }.getOrNull() ?: continue
            val absoluteUrl = absolute.toString()
            val path = absolute.path
            val name = path.trimEnd('/').substringAfterLast('/').let {
                URLDecoder.decode(it, StandardCharsets.UTF_8)
            }

            if (name.isEmpty() || name == ".." || name == ".") continue
            if (!seen.add(absoluteUrl)) continue

            val isDirectory = path.endsWith('/') || !name.contains('.')
            val normalized = if (isDirectory && !absoluteUrl.endsWith('/')) {
                "$absoluteUrl/"
            } else {
                absoluteUrl
            }

            result += DirEntry(name = name, url = normalized, isDirectory = isDirectory)
        }

        return result
    }

    /**
     * h5ai noscript fallback table includes Last modified in fb-d cells.
     * API is often blocked (404) on these servers, so this is the date source.
     */
    private fun parseFallbackTable(html: String, pageUrl: String): List<DirEntry> {
        val base = URL(pageUrl)
        val rowRegex = Regex(
            """<td class="fb-n">\s*<a href="([^"]+)">([^<]*)</a>\s*</td>\s*<td class="fb-d">([^<]*)</td>""",
            setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL),
        )
        val seen = LinkedHashSet<String>()
        val result = mutableListOf<DirEntry>()

        for (match in rowRegex.findAll(html)) {
            val rawHref = match.groupValues[1].trim()
            val linkText = match.groupValues[2].trim()
            val dateText = match.groupValues[3].trim()

            if (rawHref == ".." || linkText.equals("Parent Directory", true)) continue

            val absolute = runCatching { URL(base, rawHref) }.getOrNull() ?: continue
            val absoluteUrl = absolute.toString()
            if (!seen.add(absoluteUrl)) continue

            val path = absolute.path
            val name = linkText.ifBlank {
                path.trimEnd('/').substringAfterLast('/').let {
                    URLDecoder.decode(it, StandardCharsets.UTF_8)
                }
            }
            if (name.isEmpty() || name == "." || name == "..") continue

            val isDirectory = path.endsWith('/') || !name.contains('.')
            val normalized = if (isDirectory && !absoluteUrl.endsWith('/')) {
                "$absoluteUrl/"
            } else {
                absoluteUrl
            }

            result += DirEntry(
                name = name,
                url = normalized,
                isDirectory = isDirectory,
                modifiedAt = parseFbDate(dateText),
            )
        }
        return result
    }

    private fun parseFbDate(text: String): Long {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return 0L
        return runCatching {
            val format = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            format.parse(trimmed)?.time ?: 0L
        }.getOrDefault(0L)
    }

    private fun List<DirEntry>.filterVisible(parentUrl: String): List<DirEntry> {
        return asSequence()
            .filter { isDirectChild(parentUrl, it.url) }
            .filterNot { isJunk(it) }
            .distinctBy { it.url.trimEnd('/') }
            .toList()
    }

    private fun isJunk(entry: DirEntry): Boolean {
        val name = entry.name.lowercase()
        val path = runCatching { URL(entry.url).path.lowercase() }.getOrDefault("")
        if (name in junkNames) return true
        if (name.startsWith("_")) return true
        if (path.contains("/_h5ai/") || path.contains("/h5ai/")) return true
        return false
    }

    private fun isDirectChild(parentUrl: String, childUrl: String): Boolean {
        val parentPath = runCatching {
            URLDecoder.decode(URL(parentUrl).path, StandardCharsets.UTF_8)
        }.getOrDefault("").trimEnd('/')
        val childPath = runCatching {
            URLDecoder.decode(URL(childUrl).path, StandardCharsets.UTF_8)
        }.getOrDefault("").trimEnd('/')

        if (parentPath.isEmpty() || childPath.isEmpty()) return false
        if (childPath == parentPath) return false
        if (!childPath.startsWith("$parentPath/")) return false

        val rest = childPath.removePrefix("$parentPath/").trim('/')
        return rest.isNotEmpty() && !rest.contains('/')
    }

    private fun isImageFile(name: String): Boolean {
        val lower = name.lowercase()
        if (lower in junkNames) return false
        if (lower.contains("folder") || lower.contains("icon") || lower.contains("blank")) {
            return false
        }
        return lower.endsWith(".jpg") ||
            lower.endsWith(".jpeg") ||
            lower.endsWith(".png") ||
            lower.endsWith(".webp")
    }

    private fun isVideoFile(name: String): Boolean {
        val lower = name.lowercase()
        return lower.endsWith(".mkv") ||
            lower.endsWith(".mp4") ||
            lower.endsWith(".avi") ||
            lower.endsWith(".m4v") ||
            lower.endsWith(".mov") ||
            lower.endsWith(".webm") ||
            lower.endsWith(".ts") ||
            lower.endsWith(".m2ts")
    }
}
