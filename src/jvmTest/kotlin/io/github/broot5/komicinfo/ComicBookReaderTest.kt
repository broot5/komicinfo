package io.github.broot5.komicinfo

import io.github.broot5.komicinfo.exceptions.ComicBookFileNotFoundException
import io.github.broot5.komicinfo.exceptions.ComicInfoNotFoundException
import io.github.broot5.komicinfo.exceptions.InvalidComicBookFormatException
import io.github.broot5.komicinfo.internal.putStoredEntry
import io.github.broot5.komicinfo.model.ComicInfo
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.BufferedOutputStream
import java.io.File
import java.nio.file.Path
import java.util.zip.ZipOutputStream

/** Tests for ComicBookReader functionality */
class ComicBookReaderTest {
  @TempDir lateinit var tempDir: Path

  private lateinit var validCbzFile: File
  private lateinit var noInfoCbzFile: File

  @BeforeEach
  fun setup() {
    // Create a valid CBZ file with ComicInfo.xml
    val dummyImage = TestHelper.createImageFile(tempDir, "page.jpg", 800, 1200)

    validCbzFile = tempDir.resolve("valid.cbz").toFile()
    val info = ComicInfo(title = "Valid Comic", writer = listOf("Test Author"))
    val comicBook = ComicBook.create(info, listOf(dummyImage))
    ComicBookWriter.write(comicBook, validCbzFile).getOrThrow()

    // Create a CBZ file without ComicInfo.xml
    noInfoCbzFile = tempDir.resolve("no_info.cbz").toFile()
    ZipOutputStream(BufferedOutputStream(noInfoCbzFile.outputStream())).use { zipStream ->
      zipStream.putStoredEntry(dummyImage.name, dummyImage.readBytes())
    }
  }

  @Test
  fun `should successfully read ComicInfo from a valid CBZ file`() {
    val comicInfo = ComicBookReader.read(validCbzFile).getOrNull()

    assertNotNull(comicInfo)
    assertEquals("Valid Comic", comicInfo?.title)
    assertEquals(listOf("Test Author"), comicInfo?.writer)
    assertEquals(1, comicInfo?.pageCount)

    // Also verify basic image dimensions parsing
    assertNotNull(comicInfo?.pages)
    assertEquals(1, comicInfo?.pages?.size)
    assertEquals(800, comicInfo?.pages?.get(0)?.imageWidth)
    assertEquals(1200, comicInfo?.pages?.get(0)?.imageHeight)
  }

  @Test
  fun `should fail with ComicBookFileNotFoundException for non-existent file`() {
    val nonExistentFile = tempDir.resolve("does-not-exist.cbz").toFile()

    val result = ComicBookReader.read(nonExistentFile)

    assertTrue(result.isFailure)
    assertInstanceOf(ComicBookFileNotFoundException::class.java, result.exceptionOrNull())
  }

  @Test
  fun `should fail with InvalidComicBookFormatException for invalid format`() {
    val txtFile = tempDir.resolve("invalid.txt").toFile().apply { writeText("not valid format") }

    val result = ComicBookReader.read(txtFile)

    assertTrue(result.isFailure)
    assertInstanceOf(InvalidComicBookFormatException::class.java, result.exceptionOrNull())
  }

  @Test
  fun `should fail with ComicInfoNotFoundException when ComicInfo xml is missing`() {
    val result = ComicBookReader.read(noInfoCbzFile)

    assertTrue(result.isFailure)
    assertInstanceOf(ComicInfoNotFoundException::class.java, result.exceptionOrNull())
  }

  @Test
  fun `should handle multiple pages correctly`() {
    val image1 = TestHelper.createImageFile(tempDir, "page1.jpg", 1920, 1080)
    val image2 = TestHelper.createImageFile(tempDir, "page2.jpg", 1024, 768)
    val image3 = TestHelper.createImageFile(tempDir, "page3.jpg", 800, 600)

    val multiPageFile = tempDir.resolve("multi_page.cbz").toFile()
    val info = ComicInfo(title = "Multi-page Comic", series = "Test Series")
    val comicBook = ComicBook.create(info, listOf(image1, image2, image3))
    ComicBookWriter.write(comicBook, multiPageFile).getOrThrow()

    val readInfo = ComicBookReader.read(multiPageFile).getOrNull()

    assertNotNull(readInfo)
    assertEquals(3, readInfo?.pageCount)
    assertEquals(3, readInfo?.pages?.size)
    assertEquals("Multi-page Comic", readInfo?.title)
    assertEquals("Test Series", readInfo?.series)
  }
}
