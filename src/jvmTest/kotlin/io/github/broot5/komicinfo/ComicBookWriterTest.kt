package io.github.broot5.komicinfo

import io.github.broot5.komicinfo.exceptions.ComicBookFileNotFoundException
import io.github.broot5.komicinfo.exceptions.ComicBookWriteException
import java.nio.file.Path
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

@DisplayName("ComicBookWriter")
class ComicBookWriterTest {
  @TempDir lateinit var tempDir: Path

  @Nested
  @DisplayName("successful writes")
  inner class SuccessfulWrites {

    @Test
    @DisplayName("should write ComicInfo.xml and images into archive")
    fun writeComicInfoAndImages() {
      val imageFile = TestFileHelper.createImageFile(tempDir, "page.jpg", 1024, 768)
      val comicBook =
          ComicBook.create(
              TestFixtures.typicalComicInfo(title = "Test Comic", writer = listOf("Writer A")),
              listOf(imageFile),
          )
      val outputFile = tempDir.resolve("output.cbz").toFile()

      val writeResult = ComicBookWriter.write(comicBook, outputFile)

      assertTrue(writeResult.isSuccess)
      assertTrue(outputFile.exists())
      assertTrue(TestArchiveHelper.hasEntry(outputFile, "ComicInfo.xml"))
      assertTrue(TestArchiveHelper.hasEntry(outputFile, "page.jpg"))

      val parsed = TestArchiveHelper.readComicInfoFromArchive(outputFile)
      assertEquals("Test Comic", parsed.title)
      assertEquals(listOf("Writer A"), parsed.writer)
    }

    @Test
    @DisplayName("should overwrite existing file")
    fun overwriteExistingFile() {
      val image1 = TestFileHelper.createImageFile(tempDir, "page1.jpg", 800, 600)
      val image2 = TestFileHelper.createImageFile(tempDir, "page2.jpg", 1024, 768)
      val outputFile = tempDir.resolve("overwrite-test.cbz").toFile()

      // Write first version
      val comicBook1 =
          ComicBook.create(
              TestFixtures.minimalComicInfo(title = "First Version"),
              listOf(image1),
          )
      ComicBookWriter.write(comicBook1, outputFile).getOrThrow()

      // Overwrite with second version
      val comicBook2 =
          ComicBook.create(
              TestFixtures.minimalComicInfo(title = "Second Version"),
              listOf(image2),
          )
      ComicBookWriter.write(comicBook2, outputFile).getOrThrow()

      val parsed = TestArchiveHelper.readComicInfoFromArchive(outputFile)
      assertEquals("Second Version", parsed.title)
    }

    @Test
    @DisplayName("should write multiple images in order")
    fun writeMultipleImagesInOrder() {
      val images = TestFileHelper.createImageFiles(tempDir, 5, "page")
      val comicBook =
          ComicBook.create(
              TestFixtures.minimalComicInfo(title = "Multi-page"),
              images,
          )
      val outputFile = tempDir.resolve("multi.cbz").toFile()

      ComicBookWriter.write(comicBook, outputFile).getOrThrow()

      val entries = TestArchiveHelper.getEntryNames(outputFile)
      assertTrue(entries.contains("ComicInfo.xml"))
      (0 until 5).forEach { i -> assertTrue(entries.contains("page$i.jpg")) }
    }
  }

  @Nested
  @DisplayName("error handling")
  inner class ErrorHandling {

    @Test
    @DisplayName("should fail when image file is missing")
    fun failWhenImageFileMissing() {
      val missingFile = tempDir.resolve("missing.jpg").toFile()
      val comicBook = ComicBook(TestFixtures.minimalComicInfo(), listOf(missingFile))
      val outputFile = tempDir.resolve("output.cbz").toFile()

      val result = ComicBookWriter.write(comicBook, outputFile)

      assertTrue(result.isFailure)
      assertInstanceOf(ComicBookFileNotFoundException::class.java, result.exceptionOrNull())
      assertFalse(outputFile.exists())
    }

    @Test
    @DisplayName("should fail when destination is a directory")
    fun failWhenDestinationIsDirectory() {
      val imageFile = TestFileHelper.createImageFile(tempDir, "page.jpg", 800, 600)
      val comicBook = ComicBook.create(TestFixtures.minimalComicInfo(), listOf(imageFile))
      val destinationDir = tempDir.resolve("dest-dir").toFile().apply { mkdirs() }

      val result = ComicBookWriter.write(comicBook, destinationDir)

      assertTrue(result.isFailure)
      assertInstanceOf(ComicBookWriteException::class.java, result.exceptionOrNull())
      assertTrue(destinationDir.isDirectory)
    }
  }

  @Nested
  @DisplayName("atomicity")
  inner class Atomicity {

    @Test
    @DisplayName("should be atomic - no partial files and preserve original on failure")
    fun atomicWriteBehavior() {
      val goodImage = TestFileHelper.createImageFile(tempDir, "good.jpg", 800, 600)
      val missingImage = tempDir.resolve("missing.jpg").toFile()
      val outputFile = tempDir.resolve("atomic-test.cbz").toFile()

      // Case 1: New file - should not leave partial files
      val invalidComicBook =
          ComicBook(TestFixtures.minimalComicInfo(), listOf(goodImage, missingImage))
      assertTrue(ComicBookWriter.write(invalidComicBook, outputFile).isFailure)
      assertFalse(outputFile.exists(), "Partial file should not exist after failure")

      // Case 2: Existing file - should preserve original on failed overwrite
      val validComicBook =
          ComicBook.create(
              TestFixtures.minimalComicInfo(title = "Original"),
              listOf(goodImage),
          )
      ComicBookWriter.write(validComicBook, outputFile).getOrThrow()
      val originalSize = outputFile.length()

      val badOverwrite = ComicBook(TestFixtures.minimalComicInfo(), listOf(missingImage))
      assertTrue(ComicBookWriter.write(badOverwrite, outputFile).isFailure)
      assertEquals(originalSize, outputFile.length())
      assertEquals("Original", TestArchiveHelper.readComicInfoFromArchive(outputFile).title)
    }
  }
}
