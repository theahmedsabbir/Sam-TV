package com.samtv.app.data

data class Category(
    val id: String,
    val title: String,
    /** Path relative to [com.samtv.app.config.AppConfig.baseUrl] */
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
                Category("en", "English Movies", "English Movies"),
                Category("en1080", "English Movies-1080p", "English Movies-1080p"),
                Category("imdb", "IMDb Top-250 Movies", "IMDb Top-250 Movies"),
            ),
        ),
        CategoryRow(
            title = "Hindi & South",
            categories = listOf(
                Category("hi", "Hindi Movies", "Hindi Movies"),
                Category("south_dub", "South-Movie Hindi Dubbed", "South-Movie Hindi Dubbed"),
                Category("south", "South Indian Movies", "South Indian Movies"),
            ),
        ),
        CategoryRow(
            title = "Bangla & Foreign",
            categories = listOf(
                Category("foreign", "Foreign Movies", "Foreign Movies"),
                Category("kolkata", "Kolkata Bangla Movies", "Kolkata Bangla Movies"),
                Category("satyajit", "Satyajit Ray Films", "Satyajit Ray Films"),
            ),
        ),
        CategoryRow(
            title = "Series",
            categories = listOf(
                Category("tv", "TV & WEB Series", "TV & WEB Series"),
                Category("korean", "KOREAN TV & WEB Series", "KOREAN TV & WEB Series"),
                Category("cartoon_tv", "Cartoon TV Series", "Cartoon TV Series"),
            ),
        ),
        CategoryRow(
            title = "Animation & 3D",
            categories = listOf(
                Category("anim", "Animation Movies", "Animation Movies"),
                Category("anim1080", "Animation Movies-1080p", "Animation Movies-1080p"),
                Category("3d", "3D Movies", "3D Movies"),
            ),
        ),
        CategoryRow(
            title = "More",
            categories = listOf(
                Category("doc", "Documentary", "Documentary"),
                Category("wwe", "WWE & AEW Wrestling", "WWE & AEW Wrestling"),
                Category("awards", "AWARDS & TV SHOWS", "AWARDS & TV SHOWS"),
            ),
        ),
    )
}
