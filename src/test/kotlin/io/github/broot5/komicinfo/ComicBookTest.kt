package io.github.broot5.komicinfo

import io.github.broot5.komicinfo.model.ComicInfo
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

/** Tests for ComicBook creation and image processing */
class ComicBookTest {
  @TempDir lateinit var tempDir: Path

  @Test
  fun `should handle valid and invalid images together`() {
    val validImage = TestHelper.createImageFile(tempDir, "valid.jpg", 800, 600)
    val invalidImage = tempDir.resolve("invalid.jpg").toFile().apply { writeText("invalid") }
    val pngImage = TestHelper.createImageFile(tempDir, "page.png", 1024, 768, format = "png")

    val info = ComicInfo(title = "Mixed Test")
    val comicBook = ComicBook.create(info, listOf(validImage, invalidImage, pngImage))

    // Verify page count and basic structure
    assertEquals(3, comicBook.info.pageCount)
    assertEquals(3, comicBook.info.pages.size)

    // Valid JPG should have dimensions
    assertEquals(800, comicBook.info.pages[0].imageWidth)
    assertEquals(600, comicBook.info.pages[0].imageHeight)
    assertTrue(comicBook.info.pages[0].imageSize > 0)

    // Invalid image should have null dimensions but still exist
    assertNull(comicBook.info.pages[1].imageWidth)
    assertNull(comicBook.info.pages[1].imageHeight)

    // Valid PNG should have dimensions
    assertEquals(1024, comicBook.info.pages[2].imageWidth)
    assertEquals(768, comicBook.info.pages[2].imageHeight)
  }

  @Test
  fun `should preserve original ComicInfo fields`() {
    val image = TestHelper.createImageFile(tempDir, "page.jpg", 800, 600)
    val originalInfo =
        ComicInfo(
            title = "Test Comic",
            series = "Test Series",
            number = "1",
            writer = listOf("Author 1", "Author 2"),
            publisher = "Test Publisher",
        )

    val comicBook = ComicBook.create(originalInfo, listOf(image))

    assertEquals("Test Comic", comicBook.info.title)
    assertEquals("Test Series", comicBook.info.series)
    assertEquals("1", comicBook.info.number)
    assertEquals(listOf("Author 1", "Author 2"), comicBook.info.writer)
    assertEquals("Test Publisher", comicBook.info.publisher)
    assertEquals(1, comicBook.info.pageCount) // Auto-updated
  }

  @Test
  fun `pageBuilder should allow customization`() {
    val image1 = TestHelper.createImageFile(tempDir, "cover.jpg", 800, 1200)
    val image2 = TestHelper.createImageFile(tempDir, "page1.jpg", 800, 1200)

    val info = ComicInfo(title = "Test")
    val comicBook =
        ComicBook.create(info, listOf(image1, image2)) { page, file ->
          if (file.name.contains("cover")) {
            page.copy(type = io.github.broot5.komicinfo.model.ComicPageType.FRONT_COVER)
          } else {
            page.copy(bookmark = "Page ${page.image}")
          }
        }

    assertEquals(
        io.github.broot5.komicinfo.model.ComicPageType.FRONT_COVER, comicBook.info.pages[0].type)
    assertEquals("Page 1", comicBook.info.pages[1].bookmark)
  }
}
