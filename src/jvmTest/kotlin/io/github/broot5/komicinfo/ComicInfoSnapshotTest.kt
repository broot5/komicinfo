package io.github.broot5.komicinfo

import io.github.broot5.komicinfo.model.*
import io.github.broot5.komicinfo.xml.ComicInfoXmlCodec
import java.math.BigDecimal
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("ComicInfo Snapshot")
class ComicInfoSnapshotTest {

  private val actualPath: Path = Path.of("build", "test-snapshots", "ComicInfo.actual.xml")

  @Test
  @DisplayName("should generate stable XML matching the expected snapshot")
  fun generatedXmlMatchesSnapshot() {
    val xmlBytes = ComicInfoXmlCodec.encode(snapshotComicInfo().toComicInfoXml())
    val xmlString = xmlBytes.decodeToString()

    // Write actual output for comparison
    Files.createDirectories(actualPath.parent)
    Files.writeString(actualPath, xmlString, StandardCharsets.UTF_8)

    val expected = loadExpectedSnapshot()

    assertEquals(
        expected.normalizeLineEndings().trim(),
        xmlString.normalizeLineEndings().trim(),
        "Generated ComicInfo XML differs from snapshot. " +
            "Inspect ${actualPath.toAbsolutePath()} to see the current output.",
    )
  }

  /**
   * Creates a ComicInfo specifically for snapshot testing. This should remain stable - any changes
   * will break the snapshot test.
   */
  private fun snapshotComicInfo(): ComicInfo =
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

  private fun loadExpectedSnapshot(): String =
      requireNotNull(javaClass.getResource("/snapshots/ComicInfo.expected.xml")) {
            "Missing test resource: /snapshots/ComicInfo.expected.xml"
          }
          .readText(StandardCharsets.UTF_8)

  private fun String.normalizeLineEndings(): String = replace("\r\n", "\n")
}
