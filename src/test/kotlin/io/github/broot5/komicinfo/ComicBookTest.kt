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
  fun `should correctly extract dimensions from different image formats`() {
    val jpgImage = TestHelper.createImageFile(tempDir, "page1.jpg", 1920, 1080)
    val pngImage = TestHelper.createImageFile(tempDir, "page2.png", 800, 600, format = "png")

    val info = ComicInfo(title = "Multi-format Test")
    val comicBook = ComicBook.create(info, listOf(jpgImage, pngImage))

    assertEquals(2, comicBook.info.pageCount)
    assertEquals(2, comicBook.info.pages.size)

    // Verify JPG dimensions
    assertEquals(1920, comicBook.info.pages[0].imageWidth)
    assertEquals(1080, comicBook.info.pages[0].imageHeight)

    // Verify PNG dimensions
    assertEquals(800, comicBook.info.pages[1].imageWidth)
    assertEquals(600, comicBook.info.pages[1].imageHeight)
  }

  @Test
  fun `should handle image processing errors gracefully`() {
    val invalidImage = tempDir.resolve("invalid.jpg").toFile().apply { writeText("invalid image") }
    val info = ComicInfo(title = "Test")

    // ImageIO.read returns null for invalid images - should not crash
    val comicBook = ComicBook.create(info, listOf(invalidImage))

    assertEquals(1, comicBook.info.pages.size)
    assertNull(comicBook.info.pages[0].imageWidth)
    assertNull(comicBook.info.pages[0].imageHeight)
  }

  @Test
  fun `should set correct page indices`() {
    val image1 = TestHelper.createImageFile(tempDir, "page1.jpg", 800, 600)
    val image2 = TestHelper.createImageFile(tempDir, "page2.jpg", 800, 600)
    val image3 = TestHelper.createImageFile(tempDir, "page3.jpg", 800, 600)

    val info = ComicInfo(title = "Test")
    val comicBook = ComicBook.create(info, listOf(image1, image2, image3))

    assertEquals(0, comicBook.info.pages[0].image)
    assertEquals(1, comicBook.info.pages[1].image)
    assertEquals(2, comicBook.info.pages[2].image)
  }

  @Test
  fun `should calculate image file sizes`() {
    val image = TestHelper.createImageFile(tempDir, "page.jpg", 800, 600)
    val info = ComicInfo(title = "Test")
    val comicBook = ComicBook.create(info, listOf(image))

    assertTrue(comicBook.info.pages[0].imageSize > 0)
    assertEquals(image.length(), comicBook.info.pages[0].imageSize)
  }

  @Test
  fun `should handle mixed valid and invalid images`() {
    val validImage = TestHelper.createImageFile(tempDir, "valid.jpg", 800, 600)
    val invalidImage = tempDir.resolve("invalid.jpg").toFile().apply { writeText("invalid image") }
    val anotherValidImage = TestHelper.createImageFile(tempDir, "valid2.jpg", 1024, 768)

    val info = ComicInfo(title = "Mixed Test")
    val comicBook = ComicBook.create(info, listOf(validImage, invalidImage, anotherValidImage))

    assertEquals(3, comicBook.info.pages.size)

    // First page should have valid dimensions
    assertEquals(800, comicBook.info.pages[0].imageWidth)
    assertEquals(600, comicBook.info.pages[0].imageHeight)

    // Second page should have null dimensions
    assertNull(comicBook.info.pages[1].imageWidth)
    assertNull(comicBook.info.pages[1].imageHeight)

    // Third page should have valid dimensions
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
  }

  @Test
  fun `should auto-update pageCount`() {
    val image1 = TestHelper.createImageFile(tempDir, "page1.jpg", 800, 600)
    val image2 = TestHelper.createImageFile(tempDir, "page2.jpg", 800, 600)
    val image3 = TestHelper.createImageFile(tempDir, "page3.jpg", 800, 600)

    val info = ComicInfo(title = "Test", pageCount = 99) // Wrong count
    val comicBook = ComicBook.create(info, listOf(image1, image2, image3))

    // Should be autocorrected to actual image count
    assertEquals(3, comicBook.info.pageCount)
  }
}
