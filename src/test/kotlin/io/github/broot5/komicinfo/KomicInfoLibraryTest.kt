package io.github.broot5.komicinfo

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path

class KomicInfoLibraryTest {
  @TempDir lateinit var tempDir: Path

  private lateinit var standardCbzFile: File
  private lateinit var noInfoCbzFile: File
  private lateinit var dummyImage: File

  @BeforeEach
  fun setup() {
    dummyImage = tempDir.resolve("00.jpg").toFile().apply { writeText("img") }

    standardCbzFile = tempDir.resolve("standard.cbz").toFile()
    val info = ComicInfo(title = "Standard File", writer = listOf("Setup"))
    val comicBook = ComicBook.create(info, listOf(dummyImage))
    ComicBookWriter.write(comicBook, standardCbzFile)

    noInfoCbzFile = tempDir.resolve("no_info.cbz").toFile()
    ZipArchiveOutputStream(noInfoCbzFile).use { zipStream ->
      val entry = ZipArchiveEntry(dummyImage.name)
      zipStream.putArchiveEntry(entry)
      dummyImage.inputStream().use { it.copyTo(zipStream) }
      zipStream.closeArchiveEntry()
    }
  }

  @Test
  fun `read function should parse ComicInfo from a valid cbz file`() {
    val comicInfo = ComicBookReader.read(standardCbzFile).getOrNull()

    assertNotNull(comicInfo)
    assertEquals("Standard File", comicInfo?.title)
    assertEquals(listOf("Setup"), comicInfo?.writer)
    assertEquals(1, comicInfo?.pageCount)
  }

  @Test
  fun `read function should return null for a cbz file without ComicInfo xml`() {
    val comicInfo = ComicBookReader.read(noInfoCbzFile).getOrNull()

    assertNull(comicInfo, "The result should be null.")
  }

  @Test
  fun `write and read round-trip should preserve all data`() {
    val originalInfo = ComicInfo(title = "Round-trip Test", writer = listOf("Tester"))
    val imageFile = tempDir.resolve("round-trip_img.jpg").toFile().apply { writeText("img") }
    val originalComicBook = ComicBook.create(originalInfo, listOf(imageFile))
    val outputFile = tempDir.resolve("round-trip.cbz").toFile()

    ComicBookWriter.write(originalComicBook, outputFile)
    val readInfo = ComicBookReader.read(outputFile).getOrNull()

    assertNotNull(readInfo)
    assertEquals(originalInfo.title, readInfo?.title)
    assertEquals(originalInfo.writer, readInfo?.writer)
  }
}
