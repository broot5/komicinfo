package io.github.broot5.komicinfo

import io.github.broot5.komicinfo.model.*
import io.github.broot5.komicinfo.xml.*
import java.math.BigDecimal
import kotlinx.datetime.number
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("ComicInfo Mapper")
class ComicInfoMapperTest {

  @Nested
  @DisplayName("toComicInfoXml()")
  inner class ToComicInfoXml {

    @Test
    @DisplayName("should convert fields, join lists, and round rating correctly")
    fun convertFieldsCorrectly() {
      val info =
          ComicInfo(
              title = "Test",
              publisher = "   ",
              writer = listOf("Writer A", "Writer B"),
              tags = listOf("tag1", "tag2"),
              web = listOf("https://a.com", "https://b.com"),
              pageCount = 1,
              pages =
                  listOf(
                      ComicPage(image = 0, type = ComicPageType.FRONT_COVER, bookmark = "  "),
                  ),
              communityRating = BigDecimal("4.55"),
              blackAndWhite = YesNo.YES,
              manga = Manga.YES_AND_RIGHT_TO_LEFT,
              ageRating = AgeRating.MATURE_17_PLUS,
          )

      val xml = info.toComicInfoXml()

      assertEquals("Test", xml.Title)
      assertNull(xml.Publisher)
      assertEquals("Writer A,Writer B", xml.Writer)
      assertEquals("tag1,tag2", xml.Tags)
      assertEquals("https://a.com https://b.com", xml.Web)
      assertEquals(BigDecimal("4.6"), xml.CommunityRating?.value)
      assertEquals(YesNoXml.Yes, xml.BlackAndWhite)
      assertEquals(MangaXml.YesAndRightToLeft, xml.Manga)
      assertEquals(AgeRatingXml.MATURE_17_PLUS, xml.AgeRating)
      assertNull(requireNotNull(xml.Pages).Page.single().Bookmark)
    }

    @Test
    @DisplayName("should omit Pages when empty and reject invalid rating")
    fun edgeCases() {
      // Empty pages → null
      val noPages = ComicInfo(title = "Empty", pages = emptyList())
      assertNull(noPages.toComicInfoXml().Pages)

      // Out-of-range rating → exception
      val badRating = ComicInfo(title = "Bad", communityRating = BigDecimal("6"))
      val error = assertThrows(IllegalArgumentException::class.java) { badRating.toComicInfoXml() }
      assertEquals("Rating must be between 0.0 and 5.0", error.message)
    }
  }

  @Nested
  @DisplayName("toComicInfo()")
  inner class ToComicInfo {

    @Test
    @DisplayName("should parse lists, dates, enums, and pages correctly")
    fun parseAllFieldTypes() {
      val xml =
          ComicInfoXml(
              Title = "Source",
              Writer = " Writer A , Writer B ",
              Tags = "tag1, tag2",
              Web = "https://a.com https://b.com",
              Year = 2020,
              Month = 1,
              Day = 1,
              BlackAndWhite = YesNoXml.Yes,
              Manga = MangaXml.YesAndRightToLeft,
              AgeRating = AgeRatingXml.Teen,
              Pages =
                  ArrayOfComicPageInfoXml(
                      Page =
                          listOf(ComicPageInfoXml(Image = 0, Type = ComicPageTypeXml.FrontCover)),
                  ),
              CommunityRating = RatingXml(BigDecimal("3.0")),
          )

      val info = xml.toComicInfo()

      assertEquals(listOf("Writer A", "Writer B"), info.writer)
      assertEquals(listOf("tag1", "tag2"), info.tags)
      assertEquals(listOf("https://a.com", "https://b.com"), info.web)
      assertEquals(2020, info.date?.year)
      assertEquals(1, info.date?.month?.number)
      assertEquals(1, info.date?.day)
      assertEquals(YesNo.YES, info.blackAndWhite)
      assertEquals(Manga.YES_AND_RIGHT_TO_LEFT, info.manga)
      assertEquals(AgeRating.TEEN, info.ageRating)
      assertEquals(ComicPageType.FRONT_COVER, info.pages.single().type)
      assertEquals(BigDecimal("3.0"), info.communityRating)
    }

    @Test
    @DisplayName("should handle invalid date gracefully")
    fun handleInvalidDate() {
      val xml = ComicInfoXml(Title = "Bad Date", Year = 2020, Month = 13, Day = 5)
      assertNull(xml.toComicInfo().date)
    }
  }

  @Nested
  @DisplayName("round-trip")
  inner class RoundTrip {

    @Test
    @DisplayName("should preserve data through ComicInfo → XML → ComicInfo")
    fun preserveDataThroughRoundTrip() {
      val original = TestFixtures.fullComicInfo()

      val restored = original.toComicInfoXml().toComicInfo()

      assertEquals(original.title, restored.title)
      assertEquals(original.series, restored.series)
      assertEquals(original.writer, restored.writer)
      assertEquals(original.tags, restored.tags)
      assertEquals(original.ageRating, restored.ageRating)
      assertEquals(original.pages.size, restored.pages.size)
    }
  }
}
