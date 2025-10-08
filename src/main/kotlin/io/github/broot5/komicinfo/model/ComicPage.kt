package io.github.broot5.komicinfo.model

data class ComicPage(
    val image: Int,
    val type: ComicPageType = ComicPageType.STORY,
    val doublePage: Boolean = false, // false = single page
    val imageSize: Long = 0L, // 0 = unknown
    val key: String? = null,
    val bookmark: String? = null,
    val imageWidth: Int? = null, // null = unknown
    val imageHeight: Int? = null, // null = unknown
)
