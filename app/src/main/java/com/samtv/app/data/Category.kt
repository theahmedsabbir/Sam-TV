package com.samtv.app.data

data class Category(
    val id: String,
    val title: String,
    /** Server id resolved via [com.samtv.app.config.AppConfig.baseUrl] */
    val serverId: String,
    /** Path relative to that server root (may include nested folders) */
    val path: String,
)

data class CategoryRow(
    val title: String,
    val categories: List<Category>,
)

object Catalog {
    val rows: List<CategoryRow> = listOf(
        CategoryRow(
            title = "English",
            categories = listOf(
                Category("en", "English Movies", "7", "English Movies"),
                Category("en1080", "English Movies (1080p)", "14", "English Movies (1080p)"),
                Category("imdb", "IMDb Top-250 Movies", "14", "IMDb Top-250 Movies"),
            ),
        ),
        CategoryRow(
            title = "Hindi & South",
            categories = listOf(
                Category("hi", "Hindi Movies", "14", "Hindi Movies"),
                Category(
                    "south_dub",
                    "South-Movie Hindi Dubbed",
                    "14",
                    "SOUTH INDIAN MOVIES/Hindi Dubbed",
                ),
                Category("south", "SOUTH INDIAN MOVIES", "14", "SOUTH INDIAN MOVIES"),
            ),
        ),
        CategoryRow(
            title = "Bangla & Foreign",
            categories = listOf(
                Category("foreign", "Foreign Language Movies", "7", "Foreign Language Movies"),
                Category("kolkata", "Kolkata Bangla Movies", "7", "Kolkata Bangla Movies"),
                Category("satyajit", "Satyajit Ray Films", "7", "Satyajit Ray Films"),
            ),
        ),
        CategoryRow(
            title = "Series",
            categories = listOf(
                Category("tv", "TV & WEB Series", "12", "TV-WEB-Series"),
                Category("korean", "KOREAN TV & WEB Series", "14", "KOREAN TV & WEB Series"),
                Category(
                    "cartoon_tv",
                    "Anime & Cartoon TV Series",
                    "10",
                    "Anime & Cartoon TV Series",
                ),
            ),
        ),
        CategoryRow(
            title = "Animation & 3D",
            categories = listOf(
                Category("anim", "Animation Movies", "14", "Animation Movies"),
                Category("anim1080", "Animation Movies (1080p)", "14", "Animation Movies (1080p)"),
                Category("3d", "3D Movies", "7", "3D Movies"),
            ),
        ),
        CategoryRow(
            title = "More",
            categories = listOf(
                Category("doc", "Documentary", "10", "Documentary"),
                Category("wwe", "WWE & AEW Wrestling", "10", "WWE & AEW Wrestling"),
                Category("awards", "Awards & TV Shows", "10", "Awards & TV Shows"),
                Category("tutorial", "Tutorial", "10", "Tutorial"),
            ),
        ),
    )
}
