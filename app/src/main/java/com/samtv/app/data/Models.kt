package com.samtv.app.data

data class DirEntry(
    val name: String,
    val url: String,
    val isDirectory: Boolean,
)

data class MovieItem(
    val title: String,
    val folderUrl: String,
)
