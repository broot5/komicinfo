package io.github.broot5.komicinfo

import io.github.broot5.komicinfo.internal.putStoredEntry
import io.github.broot5.komicinfo.model.*
import io.github.broot5.komicinfo.xml.ComicInfoXmlCodec
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.BufferedOutputStream
import java.io.File
import java.math.BigDecimal
import java.nio.file.Path
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream
import javax.imageio.ImageIO
import kotlinx.datetime.LocalDate

object TestFixtures {
  fun minimalComicInfo(
      title: String = "Test Comic",
      pageCount: Int = 0,
  ) = ComicInfo(title = title, pageCount = pageCount)

  fun typicalComicInfo(
      title: String = "Sample Comic",
      series: String = "Sample Series",
      number: String = "1",
      writer: List<String> = listOf("Test Writer"),
      publisher: String = "Test Publisher",
      pageCount: Int = 1,
  ) =
      ComicInfo(
          title = title,
          series = series,
          number = number,
          writer = writer,
          publisher = publisher,
          pageCount = pageCount,
      )

  fun fullComicInfo() =
      ComicInfo(
          title = "Test Title",
          series = "Test Series",
          number = "1",
          count = 10,
          volume = 1,
          alternateSeries = "Alternate Series",
          alternateNumber = "1",
          alternateCount = 5,
          summary = "Test summary.",
          notes = "Test notes.",
          date = LocalDate(2020, 1, 1),
          writer = listOf("Writer A", "Writer B"),
          penciller = listOf("Penciller A"),
          inker = listOf("Inker A"),
          colorist = listOf("Colorist A"),
          letterer = listOf("Letterer A"),
          coverArtist = listOf("Cover Artist A"),
          editor = listOf("Editor A"),
          translator = listOf("Translator A"),
          publisher = "Test Publisher",
          imprint = "Test Imprint",
          genre = listOf("Genre A", "Genre B"),
          tags = listOf("tag1", "tag2"),
          web = listOf("https://example.com"),
          pageCount = 100,
          languageISO = "en",
          format = "Comic",
          blackAndWhite = YesNo.NO,
          manga = Manga.NO,
          characters = listOf("Character A", "Character B"),
          teams = listOf("Team A"),
          locations = listOf("Location A"),
          scanInformation = "Test scan info",
          storyArc = listOf("Arc A"),
          storyArcNumber = listOf("1"),
          seriesGroup = listOf("Group A"),
          ageRating = AgeRating.EVERYONE,
          pages =
              listOf(
                  ComicPage(image = 0, type = ComicPageType.FRONT_COVER),
                  ComicPage(image = 1, type = ComicPageType.STORY),
              ),
          communityRating = BigDecimal("3.0"),
          mainCharacterOrTeam = "Main Character",
          review = "Test review.",
          gtin = "1234567890123",
      )
}

object TestFileHelper {

  /**
   * Creates a valid image file for testing purposes.
   *
   * @param tempDir The temporary directory to create the file in
   * @param filename The name of the file to create
   * @param width Image width in pixels
   * @param height Image height in pixels
   * @param format Image format (jpg, png, etc.)
   * @return The created image file
   */
  fun createImageFile(
      tempDir: Path,
      filename: String,
      width: Int = 800,
      height: Int = 1200,
      format: String = "jpg",
  ): File {
    val file = tempDir.resolve(filename).toFile()
    val image = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
    val graphics = image.createGraphics()

    graphics.color = Color.WHITE
    graphics.fillRect(0, 0, width, height)

    graphics.color = Color.BLUE
    graphics.drawRect(10, 10, width - 20, height - 20)

    graphics.dispose()

    ImageIO.write(image, format, file)
    return file
  }

  /** Creates an invalid (non-image) file with given content */
  fun createInvalidImageFile(
      tempDir: Path,
      filename: String,
      content: String = "invalid image content",
  ): File = tempDir.resolve(filename).toFile().apply { writeText(content) }

  /** Creates an empty (zero-byte) file */
  fun createEmptyFile(tempDir: Path, filename: String): File =
      tempDir.resolve(filename).toFile().apply { writeBytes(byteArrayOf()) }

  /** Creates multiple image files with sequential names */
  fun createImageFiles(
      tempDir: Path,
      count: Int,
      prefix: String = "page",
      width: Int = 800,
      height: Int = 1200,
  ): List<File> =
      (0 until count).map { index -> createImageFile(tempDir, "$prefix$index.jpg", width, height) }
}

object TestArchiveHelper {

  /**
   * Creates a valid CBZ file with the given ComicInfo and images.
   *
   * @param tempDir Temporary directory
   * @param filename Output filename
   * @param info ComicInfo metadata
   * @param imageFiles List of image files to include
   * @return The created CBZ file
   */
  fun createCbzFile(
      tempDir: Path,
      filename: String,
      info: ComicInfo,
      imageFiles: List<File>,
  ): File {
    val cbzFile = tempDir.resolve(filename).toFile()
    val comicBook = ComicBook.create(info, imageFiles)
    ComicBookWriter.write(comicBook, cbzFile).getOrThrow()
    return cbzFile
  }

  /** Creates a CBZ file without ComicInfo.xml (only images). */
  fun createCbzWithoutComicInfo(
      tempDir: Path,
      filename: String,
      imageFiles: List<File>,
  ): File {
    val cbzFile = tempDir.resolve(filename).toFile()
    ZipOutputStream(BufferedOutputStream(cbzFile.outputStream())).use { zipStream ->
      imageFiles.forEach { imageFile ->
        zipStream.putStoredEntry(imageFile.name, imageFile.readBytes())
      }
    }
    return cbzFile
  }

  /** Creates a CBZ file with invalid/malformed ComicInfo.xml. */
  fun createCbzWithInvalidXml(
      tempDir: Path,
      filename: String,
      xmlContent: String = "not valid xml",
  ): File {
    val cbzFile = tempDir.resolve(filename).toFile()
    ZipOutputStream(BufferedOutputStream(cbzFile.outputStream())).use { zipStream ->
      zipStream.putStoredEntry("ComicInfo.xml", xmlContent.toByteArray())
    }
    return cbzFile
  }

  /** Reads ComicInfo from a CBZ archive file. */
  fun readComicInfoFromArchive(file: File): ComicInfo {
    ZipFile(file).use { zip ->
      val entry = requireNotNull(zip.getEntry("ComicInfo.xml")) { "ComicInfo.xml not found" }
      val bytes = zip.getInputStream(entry).use { it.readBytes() }
      return ComicInfoXmlCodec.decode(bytes).toComicInfo()
    }
  }

  /** Checks if a CBZ archive contains a specific entry. */
  fun hasEntry(file: File, entryName: String): Boolean =
      ZipFile(file).use { it.getEntry(entryName) != null }

  /** Gets the list of entry names in a CBZ archive. */
  fun getEntryNames(file: File): List<String> =
      ZipFile(file).use { zip -> zip.entries().toList().map { it.name } }
}
