package io.github.broot5.komicinfo.model

data class ComicPage(
    val image: Int,
    val type: ComicPageType? = null, // null = Story (XSD default)
    val doublePage: Boolean? = null, // null = false (XSD default, single page)
    val imageSize: Long? = null, // null = unknown
    val key: String? = null,
    val bookmark: String? = null,
    val imageWidth: Int? = null, // null = unknown
    val imageHeight: Int? = null, // null = unknown
)
