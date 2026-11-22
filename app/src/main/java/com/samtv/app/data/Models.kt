package com.samtv.app.data

data class DirEntry(
    val name: String,
    val url: String,
    val isDirectory: Boolean,
    /** Epoch millis; 0 if unknown */
    val modifiedAt: Long = 0L,
)

data class MovieItem(
    val title: String,
    val folderUrl: String,
    /** Epoch millis; 0 if unknown */
    val modifiedAt: Long = 0L,
)

enum class MovieSort {
    NEWEST,
    NAME,
}
