package io.github.broot5.komicinfo

import io.github.broot5.komicinfo.exceptions.ComicBookFileNotFoundException
import io.github.broot5.komicinfo.model.ComicInfo
import java.nio.file.Path
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

/** Tests for ComicBookWriter functionality */
class ComicBookWriterTest {
  @TempDir lateinit var tempDir: Path

  @Test
  fun `should successfully write and read CBZ file`() {
    val imageFile = TestHelper.createImageFile(tempDir, "page.jpg", 1024, 768)
    val info = ComicInfo(title = "Test Comic", writer = listOf("Test Author"))
    val comicBook = ComicBook.create(info, listOf(imageFile))
    val outputFile = tempDir.resolve("output.cbz").toFile()

    val writeResult = ComicBookWriter.write(comicBook, outputFile)
    assertTrue(writeResult.isSuccess)
    assertTrue(outputFile.exists())

    // Verify round-trip
    val readInfo = ComicBookReader.read(outputFile).getOrNull()
    assertNotNull(readInfo)
    assertEquals("Test Comic", readInfo?.title)
    assertEquals(listOf("Test Author"), readInfo?.writer)
    assertEquals(1024, readInfo?.pages?.get(0)?.imageWidth)
    assertEquals(768, readInfo?.pages?.get(0)?.imageHeight)
  }

  @Test
  fun `should fail when image file is missing`() {
    val missingFile = tempDir.resolve("missing.jpg").toFile()
    val info = ComicInfo(title = "Test")
    val comicBook = ComicBook(info, listOf(missingFile))
    val outputFile = tempDir.resolve("output.cbz").toFile()

    val result = ComicBookWriter.write(comicBook, outputFile)

    assertTrue(result.isFailure)
    assertInstanceOf(ComicBookFileNotFoundException::class.java, result.exceptionOrNull())
    assertFalse(outputFile.exists())
  }

  @Test
  fun `should be atomic - no partial file on failure`() {
    val goodImage = TestHelper.createImageFile(tempDir, "good.jpg", 800, 600)
    val badImage = tempDir.resolve("bad.jpg").toFile() // Non-existent file
    val info = ComicInfo(title = "Test")
    val comicBook = ComicBook(info, listOf(goodImage, badImage))
    val outputFile = tempDir.resolve("atomic-test.cbz").toFile()

    val result = ComicBookWriter.write(comicBook, outputFile)

    assertTrue(result.isFailure)
    assertFalse(outputFile.exists())
  }

  @Test
  fun `should overwrite existing file`() {
    val image1 = TestHelper.createImageFile(tempDir, "page1.jpg", 800, 600)
    val image2 = TestHelper.createImageFile(tempDir, "page2.jpg", 1024, 768)
    val outputFile = tempDir.resolve("overwrite-test.cbz").toFile()

    // Write first file
    val info1 = ComicInfo(title = "First Version")
    val comicBook1 = ComicBook.create(info1, listOf(image1))
    ComicBookWriter.write(comicBook1, outputFile).getOrThrow()

    // Overwrite with second file
    val info2 = ComicInfo(title = "Second Version")
    val comicBook2 = ComicBook.create(info2, listOf(image2))
    ComicBookWriter.write(comicBook2, outputFile).getOrThrow()

    // Verify overwrite
    val readInfo = ComicBookReader.read(outputFile).getOrNull()
    assertNotNull(readInfo)
    assertEquals("Second Version", readInfo?.title)
  }
}
