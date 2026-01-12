package io.github.broot5.komicinfo

import io.github.broot5.komicinfo.model.ComicInfo
import io.github.broot5.komicinfo.model.ComicPage
import io.github.broot5.komicinfo.model.ComicPageType
import java.nio.file.Path
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

@DisplayName("ComicBook")
class ComicBookTest {
  @TempDir lateinit var tempDir: Path

  @Nested
  @DisplayName("create()")
  inner class Create {

    @Test
    @DisplayName("should extract dimensions from valid image files")
    fun extractDimensionsFromValidImages() {
      val jpgImage = TestFileHelper.createImageFile(tempDir, "page.jpg", 800, 600)
      val pngImage = TestFileHelper.createImageFile(tempDir, "page.png", 1024, 768, format = "png")

      val comicBook =
          ComicBook.create(
              TestFixtures.minimalComicInfo(),
              listOf(jpgImage, pngImage),
          )

      assertEquals(2, comicBook.info.pageCount)
      assertEquals(2, comicBook.info.pages.size)

      with(comicBook.info.pages[0]) {
        assertEquals(800, imageWidth)
        assertEquals(600, imageHeight)
        assertNotNull(imageSize)
        assertTrue(imageSize!! > 0)
      }

      with(comicBook.info.pages[1]) {
        assertEquals(1024, imageWidth)
        assertEquals(768, imageHeight)
      }
    }

    @Test
    @DisplayName("should handle invalid and empty images gracefully")
    fun handleInvalidImagesGracefully() {
      val validImage = TestFileHelper.createImageFile(tempDir, "valid.jpg", 800, 600)
      val invalidImage = TestFileHelper.createInvalidImageFile(tempDir, "invalid.jpg")
      val emptyFile = TestFileHelper.createEmptyFile(tempDir, "empty.jpg")

      val comicBook =
          ComicBook.create(
              TestFixtures.minimalComicInfo(),
              listOf(validImage, invalidImage, emptyFile),
          )

      assertEquals(3, comicBook.info.pageCount)

      // Valid image should have dimensions
      assertNotNull(comicBook.info.pages[0].imageWidth)

      // Invalid image should have null dimensions but still be included
      assertNull(comicBook.info.pages[1].imageWidth)
      assertNull(comicBook.info.pages[1].imageHeight)

      // Empty file should have null imageSize
      assertNull(comicBook.info.pages[2].imageSize)
    }

    @Test
    @DisplayName("should preserve original ComicInfo fields while updating pageCount")
    fun preserveOriginalComicInfoFields() {
      val image = TestFileHelper.createImageFile(tempDir, "page.jpg", 800, 600)
      val originalInfo =
          TestFixtures.typicalComicInfo(
              title = "Test Comic",
              series = "Test Series",
              number = "1",
              writer = listOf("Writer A", "Writer B"),
              publisher = "Test Publisher",
          )

      val comicBook = ComicBook.create(originalInfo, listOf(image))

      assertEquals("Test Comic", comicBook.info.title)
      assertEquals("Test Series", comicBook.info.series)
      assertEquals("1", comicBook.info.number)
      assertEquals(listOf("Writer A", "Writer B"), comicBook.info.writer)
      assertEquals("Test Publisher", comicBook.info.publisher)
      assertEquals(1, comicBook.info.pageCount) // Auto-updated from image count
    }

    @Test
    @DisplayName("should allow page customization via pageBuilder")
    fun pageBuilderAllowsCustomization() {
      val coverImage = TestFileHelper.createImageFile(tempDir, "cover.jpg", 800, 1200)
      val pageImage = TestFileHelper.createImageFile(tempDir, "page1.jpg", 800, 1200)

      val comicBook =
          ComicBook.create(
              TestFixtures.minimalComicInfo(),
              listOf(coverImage, pageImage),
          ) { page, file ->
            when {
              file.name.contains("cover") -> page.copy(type = ComicPageType.FRONT_COVER)
              else -> page.copy(bookmark = "Page ${page.image}")
            }
          }

      assertEquals(ComicPageType.FRONT_COVER, comicBook.info.pages[0].type)
      assertEquals("Page 1", comicBook.info.pages[1].bookmark)
    }
  }

  @Nested
  @DisplayName("metadata merging")
  inner class MetadataMerging {

    @Test
    @DisplayName("should prefer generated dimensions over existing metadata")
    fun preferGeneratedDimensionsOverExisting() {
      val image = TestFileHelper.createImageFile(tempDir, "page.jpg", 640, 480)
      val baseInfo =
          ComicInfo(
              title = "Merge Test",
              pages =
                  listOf(
                      ComicPage(
                          image = 0,
                          type = ComicPageType.BACK_COVER,
                          doublePage = true,
                          key = "custom-key",
                          bookmark = "Original Bookmark",
                          imageWidth = 10,
                          imageHeight = 20,
                      ),
                  ),
          )

      val comicBook = ComicBook.create(baseInfo, listOf(image))

      val mergedPage = comicBook.info.pages.single()
      assertEquals(ComicPageType.BACK_COVER, mergedPage.type)
      assertEquals(true, mergedPage.doublePage)
      assertEquals("custom-key", mergedPage.key)
      assertEquals("Original Bookmark", mergedPage.bookmark)

      // Generated values should override
      assertEquals(640, mergedPage.imageWidth)
      assertEquals(480, mergedPage.imageHeight)
      assertEquals(image.length(), mergedPage.imageSize)
    }

    @Test
    @DisplayName("should apply pageBuilder after merging")
    fun pageBuilderAppliedAfterMerging() {
      val image = TestFileHelper.createImageFile(tempDir, "page.jpg", 640, 480)
      val baseInfo =
          ComicInfo(
              title = "Builder Test",
              pages = listOf(ComicPage(image = 0, bookmark = "original")),
          )

      val comicBook =
          ComicBook.create(baseInfo, listOf(image)) { page, _ ->
            page.copy(bookmark = page.bookmark?.uppercase())
          }

      assertEquals("ORIGINAL", comicBook.info.pages.single().bookmark)
    }
  }
}
