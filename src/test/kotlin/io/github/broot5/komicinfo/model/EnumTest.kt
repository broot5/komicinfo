package io.github.broot5.komicinfo.model

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/** Tests for enum classes and their fromValue methods */
class EnumTest {

  @Test
  fun `fromValue should be case insensitive for all enums`() {
    // YesNo
    assertEquals(YesNo.YES, YesNo.fromValue("yes"))
    assertEquals(YesNo.NO, YesNo.fromValue("NO"))

    // Manga
    assertEquals(Manga.YES, Manga.fromValue("YES"))
    assertEquals(Manga.YES_AND_RIGHT_TO_LEFT, Manga.fromValue("yesandrighttoleft"))

    // AgeRating
    assertEquals(AgeRating.EVERYONE, AgeRating.fromValue("everyone"))
    assertEquals(AgeRating.MATURE_17_PLUS, AgeRating.fromValue("MATURE 17+"))

    // ComicPageType
    assertEquals(ComicPageType.STORY, ComicPageType.fromValue("story"))
    assertEquals(ComicPageType.FRONT_COVER, ComicPageType.fromValue("FRONTCOVER"))
  }

  @Test
  fun `fromValue should return default for invalid or null values`() {
    // YesNo defaults to UNKNOWN
    assertEquals(YesNo.UNKNOWN, YesNo.fromValue("invalid"))
    assertEquals(YesNo.UNKNOWN, YesNo.fromValue(null))

    // Manga defaults to UNKNOWN
    assertEquals(Manga.UNKNOWN, Manga.fromValue("invalid"))
    assertEquals(Manga.UNKNOWN, Manga.fromValue(null))

    // AgeRating defaults to UNKNOWN
    assertEquals(AgeRating.UNKNOWN, AgeRating.fromValue("invalid"))
    assertEquals(AgeRating.UNKNOWN, AgeRating.fromValue(null))

    // ComicPageType defaults to STORY
    assertEquals(ComicPageType.STORY, ComicPageType.fromValue("invalid"))
    assertEquals(ComicPageType.STORY, ComicPageType.fromValue(null))
  }

  @Test
  fun `all enum values should round-trip correctly`() {
    YesNo.entries.forEach { assertEquals(it, YesNo.fromValue(it.value)) }
    Manga.entries.forEach { assertEquals(it, Manga.fromValue(it.value)) }
    AgeRating.entries.forEach { assertEquals(it, AgeRating.fromValue(it.value)) }
    ComicPageType.entries.forEach { assertEquals(it, ComicPageType.fromValue(it.value)) }
  }
}
