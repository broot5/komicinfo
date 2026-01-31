package io.github.broot5.komicinfo.model

public data class ComicPage(
    public val image: Int,
    public val type: ComicPageType? = null, // null = Story (XSD default)
    public val doublePage: Boolean? = null, // null = false (XSD default, single page)
    public val imageSize: Long? = null, // null = unknown
    public val key: String? = null,
    public val bookmark: String? = null,
    public val imageWidth: Int? = null, // null = unknown
    public val imageHeight: Int? = null, // null = unknown
)
