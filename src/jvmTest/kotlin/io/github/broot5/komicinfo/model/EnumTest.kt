package io.github.broot5.komicinfo.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Enums")
class EnumTest {

  @Test
  @DisplayName("fromValue should be case-insensitive and round-trip all values")
  fun fromValueCaseInsensitiveAndRoundTrip() {
    // Case insensitivity
    assertEquals(YesNo.YES, YesNo.fromValue("yes"))
    assertEquals(YesNo.NO, YesNo.fromValue("NO"))
    assertEquals(Manga.YES_AND_RIGHT_TO_LEFT, Manga.fromValue("yesandrighttoleft"))
    assertEquals(AgeRating.MATURE_17_PLUS, AgeRating.fromValue("MATURE 17+"))
    assertEquals(ComicPageType.FRONT_COVER, ComicPageType.fromValue("frontcover"))

    // Round-trip all values
    YesNo.entries.forEach { assertEquals(it, YesNo.fromValue(it.value)) }
    Manga.entries.forEach { assertEquals(it, Manga.fromValue(it.value)) }
    AgeRating.entries.forEach { assertEquals(it, AgeRating.fromValue(it.value)) }
    ComicPageType.entries.forEach { assertEquals(it, ComicPageType.fromValue(it.value)) }
  }

  @Test
  @DisplayName("fromValue should return appropriate default for invalid/null values")
  fun fromValueDefaultsForInvalid() {
    // YesNo, Manga, AgeRating → UNKNOWN
    assertEquals(YesNo.UNKNOWN, YesNo.fromValue("invalid"))
    assertEquals(YesNo.UNKNOWN, YesNo.fromValue(null))
    assertEquals(Manga.UNKNOWN, Manga.fromValue("invalid"))
    assertEquals(Manga.UNKNOWN, Manga.fromValue(null))
    assertEquals(AgeRating.UNKNOWN, AgeRating.fromValue("invalid"))
    assertEquals(AgeRating.UNKNOWN, AgeRating.fromValue(null))

    // ComicPageType → STORY (default)
    assertEquals(ComicPageType.STORY, ComicPageType.fromValue("invalid"))
    assertEquals(ComicPageType.STORY, ComicPageType.fromValue(null))
  }
}
