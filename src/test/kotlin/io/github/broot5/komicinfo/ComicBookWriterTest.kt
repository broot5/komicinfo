package io.github.broot5.komicinfo

import io.github.broot5.komicinfo.exceptions.ComicBookFileNotFoundException
import io.github.broot5.komicinfo.model.ComicInfo
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

/** Tests for ComicBookWriter functionality */
class ComicBookWriterTest {
  @TempDir lateinit var tempDir: Path

  @Test
  fun `should successfully write CBZ file`() {
    val imageFile = TestHelper.createImageFile(tempDir, "page.jpg", 800, 1200)
    val info = ComicInfo(title = "Test Comic", writer = listOf("Test Author"))
    val comicBook = ComicBook.create(info, listOf(imageFile))
    val outputFile = tempDir.resolve("output.cbz").toFile()

    val result = ComicBookWriter.write(comicBook, outputFile)

    assertTrue(result.isSuccess)
    assertTrue(outputFile.exists())
    assertTrue(outputFile.length() > 0)
  }

  @Test
  fun `should write and read round-trip preserving all data`() {
    val originalInfo = ComicInfo(title = "Round-trip Test", writer = listOf("Tester"))
    val imageFile = TestHelper.createImageFile(tempDir, "round-trip.jpg", 1024, 768)
    val originalComicBook = ComicBook.create(originalInfo, listOf(imageFile))
    val outputFile = tempDir.resolve("round-trip.cbz").toFile()

    val writeResult = ComicBookWriter.write(originalComicBook, outputFile)
    assertTrue(writeResult.isSuccess)

    val readInfo = ComicBookReader.read(outputFile).getOrNull()

    assertNotNull(readInfo)
    assertEquals(originalInfo.title, readInfo?.title)
    assertEquals(originalInfo.writer, readInfo?.writer)
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

    val firstSize = outputFile.length()

    // Overwrite with second file
    val info2 = ComicInfo(title = "Second Version")
    val comicBook2 = ComicBook.create(info2, listOf(image2))
    ComicBookWriter.write(comicBook2, outputFile).getOrThrow()

    // Verify overwrite
    val readInfo = ComicBookReader.read(outputFile).getOrNull()
    assertNotNull(readInfo)
    assertEquals("Second Version", readInfo?.title)
    assertNotEquals(firstSize, outputFile.length())
  }

  @Test
  fun `should create parent directories if they do not exist`() {
    val imageFile = TestHelper.createImageFile(tempDir, "page.jpg", 800, 600)
    val info = ComicInfo(title = "Test")
    val comicBook = ComicBook.create(info, listOf(imageFile))
    val outputFile = tempDir.resolve("dir1/dir2/output.cbz").toFile()

    assertFalse(outputFile.parentFile.exists())

    val result = ComicBookWriter.write(comicBook, outputFile)

    assertTrue(result.isSuccess)
    assertTrue(outputFile.exists())
    assertTrue(outputFile.parentFile.exists())
  }
}
