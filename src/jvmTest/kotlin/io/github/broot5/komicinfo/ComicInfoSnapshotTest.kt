package io.github.broot5.komicinfo

import io.github.broot5.komicinfo.model.*
import io.github.broot5.komicinfo.xml.ComicInfoXmlCodec
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path

class ComicInfoSnapshotTest {
  private val actualPath: Path = Path.of("build", "test-snapshots", "ComicInfo.actual.xml")

  @Test
  fun `generated ComicInfo xml stays stable`() {
    val xmlBytes = ComicInfoXmlCodec.encode(sampleComicInfo().toComicInfoXml())
    val xmlString = xmlBytes.decodeToString()

    Files.createDirectories(actualPath.parent)
    Files.writeString(actualPath, xmlString, StandardCharsets.UTF_8)

    val expected =
        requireNotNull(javaClass.getResource("/snapshots/ComicInfo.expected.xml")) {
              "Missing test resource: /snapshots/ComicInfo.expected.xml"
            }
            .readText(StandardCharsets.UTF_8)

    assertEquals(
        expected.normalizeLineEndings().trim(),
        xmlString.normalizeLineEndings().trim(),
        "Generated ComicInfo XML differs from snapshot. Inspect ${actualPath.toAbsolutePath()} to see the current output.",
    )
  }

  private fun sampleComicInfo(): ComicInfo =
      ComicInfo(
          title = "komicinfo #1",
          series = "komicinfo",
          number = "1",
          count = 8,
          volume = 1,
          summary = "Metadata used only for testing.",
          notes = "Generated for snapshot verification.",
          writer = listOf("A", "B"),
          publisher = "Press",
          tags = listOf("space", "adventure"),
          web = listOf("https://example.com", "https://example1.com"),
          pageCount = 42,
          languageISO = "en",
          blackAndWhite = YesNo.NO,
          manga = Manga.NO,
          ageRating = AgeRating.EVERYONE_10_PLUS,
          pages =
              listOf(
                  ComicPage(
                      image = 0,
                      type = ComicPageType.FRONT_COVER,
                      imageWidth = 1200,
                      imageHeight = 1800,
                      bookmark = "Cover",
                  ),
                  ComicPage(
                      image = 1,
                      type = ComicPageType.STORY,
                      imageWidth = 1200,
                      imageHeight = 1800,
                      bookmark = "Chapter 1",
                  ),
                  ComicPage(
                      image = 2,
                      type = ComicPageType.LETTERS,
                      imageWidth = 1200,
                      imageHeight = 1800,
                      bookmark = "Letters",
                  ),
              ),
          communityRating = BigDecimal("4.54"),
      )

  private fun String.normalizeLineEndings(): String = replace("\r\n", "\n")
}
