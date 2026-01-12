package io.github.broot5.komicinfo

import io.github.broot5.komicinfo.exceptions.ComicBookFileNotFoundException
import io.github.broot5.komicinfo.exceptions.ComicInfoNotFoundException
import io.github.broot5.komicinfo.exceptions.ComicInfoParseException
import io.github.broot5.komicinfo.exceptions.CorruptedArchiveException
import io.github.broot5.komicinfo.exceptions.InvalidComicBookFormatException
import java.io.File
import java.nio.file.Path
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

@DisplayName("ComicBookReader")
class ComicBookReaderTest {
  @TempDir lateinit var tempDir: Path

  private lateinit var validCbzFile: File
  private lateinit var noInfoCbzFile: File
  private lateinit var dummyImage: File

  @BeforeEach
  fun setup() {
    dummyImage = TestFileHelper.createImageFile(tempDir, "page.jpg", 800, 1200)

    validCbzFile =
        TestArchiveHelper.createCbzFile(
            tempDir,
            "valid.cbz",
            TestFixtures.typicalComicInfo(title = "Valid Comic", writer = listOf("Writer A")),
            listOf(dummyImage),
        )

    noInfoCbzFile =
        TestArchiveHelper.createCbzWithoutComicInfo(
            tempDir,
            "no_info.cbz",
            listOf(dummyImage),
        )
  }

  @Nested
  @DisplayName("successful reads")
  inner class SuccessfulReads {

    @Test
    @DisplayName("should read ComicInfo with metadata and page dimensions from valid CBZ")
    fun readValidCbzFile() {
      val comicInfo = ComicBookReader.read(validCbzFile).getOrNull()

      assertNotNull(comicInfo)
      assertEquals("Valid Comic", comicInfo?.title)
      assertEquals(listOf("Writer A"), comicInfo?.writer)
      assertEquals(1, comicInfo?.pageCount)

      // Page dimensions should be parsed correctly
      assertEquals(1, comicInfo?.pages?.size)
      assertEquals(800, comicInfo?.pages?.get(0)?.imageWidth)
      assertEquals(1200, comicInfo?.pages?.get(0)?.imageHeight)
    }

    @Test
    @DisplayName("should accept both .cbz and .zip extensions (case-insensitive)")
    fun acceptValidExtensions() {
      // Uppercase CBZ
      val upperCbz =
          TestArchiveHelper.createCbzFile(
              tempDir,
              "UPPER.CBZ",
              TestFixtures.minimalComicInfo(title = "Upper CBZ"),
              listOf(dummyImage),
          )
      assertTrue(ComicBookReader.read(upperCbz).isSuccess)

      // ZIP extension
      val zipFile =
          TestArchiveHelper.createCbzFile(
              tempDir,
              "comic.zip",
              TestFixtures.minimalComicInfo(title = "Zip"),
              listOf(dummyImage),
          )
      assertTrue(ComicBookReader.read(zipFile).isSuccess)
    }

    @Test
    @DisplayName("should handle multiple pages correctly")
    fun handleMultiplePagesCorrectly() {
      val images = TestFileHelper.createImageFiles(tempDir, 3, "multi_page")
      val cbzFile =
          TestArchiveHelper.createCbzFile(
              tempDir,
              "multi_page.cbz",
              TestFixtures.typicalComicInfo(title = "Multi-page", series = "Test Series"),
              images,
          )

      val readInfo = ComicBookReader.read(cbzFile).getOrNull()

      assertNotNull(readInfo)
      assertEquals(3, readInfo?.pageCount)
      assertEquals(3, readInfo?.pages?.size)
    }
  }

  @Nested
  @DisplayName("error handling")
  inner class ErrorHandling {

    @Test
    @DisplayName("should fail with appropriate exception for each error case")
    fun failWithAppropriateExceptions() {
      // Non-existent file
      val nonExistent = tempDir.resolve("missing.cbz").toFile()
      assertInstanceOf(
          ComicBookFileNotFoundException::class.java,
          ComicBookReader.read(nonExistent).exceptionOrNull(),
      )

      // Unsupported format
      val txtFile = tempDir.resolve("file.txt").toFile().apply { writeText("text") }
      assertInstanceOf(
          InvalidComicBookFormatException::class.java,
          ComicBookReader.read(txtFile).exceptionOrNull(),
      )

      // Missing extension
      val noExt = tempDir.resolve("noext").toFile().apply { writeText("data") }
      assertInstanceOf(
          InvalidComicBookFormatException::class.java,
          ComicBookReader.read(noExt).exceptionOrNull(),
      )

      // Missing ComicInfo.xml
      assertInstanceOf(
          ComicInfoNotFoundException::class.java,
          ComicBookReader.read(noInfoCbzFile).exceptionOrNull(),
      )

      // Malformed XML
      val badXml = TestArchiveHelper.createCbzWithInvalidXml(tempDir, "bad.cbz", "not xml")
      assertInstanceOf(
          ComicInfoParseException::class.java,
          ComicBookReader.read(badXml).exceptionOrNull(),
      )

      // Corrupted archive
      val corrupted = tempDir.resolve("corrupted.cbz").toFile().apply { writeText("not zip") }
      assertInstanceOf(
          CorruptedArchiveException::class.java,
          ComicBookReader.read(corrupted).exceptionOrNull(),
      )
    }
  }
}
