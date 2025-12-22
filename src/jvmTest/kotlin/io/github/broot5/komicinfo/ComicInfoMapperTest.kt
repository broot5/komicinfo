package io.github.broot5.komicinfo

import io.github.broot5.komicinfo.model.*
import io.github.broot5.komicinfo.xml.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class ComicInfoMapperTest {
  @Test
  fun `toComicInfoXml should drop blanks and round rating`() {
    val info =
        ComicInfo(
            title = "Mapper",
            publisher = "  ",
            writer = listOf("Writer One", "Writer Two"),
            tags = listOf("tag1", "tag2"),
            web = listOf("https://a.com", "https://b.com"),
            pageCount = 1,
            pages =
                listOf(
                    ComicPage(
                        image = 0,
                        type = ComicPageType.FRONT_COVER,
                        bookmark = "  ",
                        imageWidth = 100,
                        imageHeight = 200,
                        imageSize = 500L,
                    ),
                ),
            communityRating = BigDecimal("4.55"),
        )

    val xml = info.toComicInfoXml()

    assertEquals("Mapper", xml.Title)
    assertNull(xml.Publisher)
    assertEquals("Writer One,Writer Two", xml.Writer)
    assertEquals("tag1,tag2", xml.Tags)
    assertEquals("https://a.com https://b.com", xml.Web)
    assertEquals(1, xml.PageCount)
    assertEquals(BigDecimal("4.6"), xml.CommunityRating?.value)

    val pageXml = requireNotNull(xml.Pages).Page.single()
    assertEquals(0, pageXml.Image)
    assertEquals(ComicPageTypeXml.FrontCover, pageXml.Type)
    assertNull(pageXml.Bookmark)
    assertEquals(100, pageXml.ImageWidth)
    assertEquals(200, pageXml.ImageHeight)
    assertEquals(500L, pageXml.ImageSize)
  }

  @Test
  fun `toComicInfo should parse lists and ignore invalid date`() {
    val xml =
        ComicInfoXml(
            Title = "Source",
            PageCount = 1,
            Year = 2024,
            Month = 13,
            Day = 5,
            Writer = " Writer One , Writer Two ",
            Tags = "tag1, tag2",
            Web = "https://a.com https://b.com",
            BlackAndWhite = YesNoXml.Yes,
            Manga = MangaXml.YesAndRightToLeft,
            AgeRating = AgeRatingXml.Teen,
            Pages =
                ArrayOfComicPageInfoXml(
                    Page =
                        listOf(
                            ComicPageInfoXml(
                                Image = 0,
                                Type = ComicPageTypeXml.FrontCover,
                                DoublePage = true,
                                Bookmark = " Cover ",
                            ),
                        ),
                ),
            CommunityRating = RatingXml(BigDecimal("3.0")),
        )

    val info = xml.toComicInfo()

    assertNull(info.date)
    assertEquals(listOf("Writer One", "Writer Two"), info.writer)
    assertEquals(listOf("tag1", "tag2"), info.tags)
    assertEquals(listOf("https://a.com", "https://b.com"), info.web)
    assertEquals(YesNo.YES, info.blackAndWhite)
    assertEquals(Manga.YES_AND_RIGHT_TO_LEFT, info.manga)
    assertEquals(AgeRating.TEEN, info.ageRating)
    assertEquals(BigDecimal("3.0"), info.communityRating)
    assertEquals(1, info.pages.size)
    val page = info.pages.single()
    assertEquals(ComicPageType.FRONT_COVER, page.type)
    assertEquals(true, page.doublePage)
    assertEquals(" Cover ", page.bookmark)
  }
}
