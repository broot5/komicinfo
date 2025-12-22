package io.github.broot5.komicinfo

import io.github.broot5.komicinfo.exceptions.*
import io.github.broot5.komicinfo.model.ComicInfo
import io.github.broot5.komicinfo.xml.ComicInfoXmlCodec
import java.io.File
import java.io.IOException
import java.util.zip.ZipFile
import kotlinx.serialization.SerializationException
import nl.adaptivity.xmlutil.XmlException

object ComicBookReader {
  private val SUPPORTED_EXTENSIONS = listOf("cbz", "zip")

  /**
   * Reads ComicInfo metadata from a CBZ archive file.
   *
   * @param file The CBZ archive file to read
   * @return Result containing ComicInfo on success, or exception details on failure
   * @throws ComicBookFileNotFoundException if the file doesn't exist
   * @throws InvalidComicBookFormatException if the file format is not supported
   * @throws ComicInfoNotFoundException if ComicInfo.xml is not found in the archive
   * @throws ComicInfoParseException if ComicInfo.xml cannot be parsed
   * @throws CorruptedArchiveException if the archive is corrupted or cannot be read
   */
  fun read(file: File): Result<ComicInfo> {
    return runCatching {
      // Validate file existence
      if (!file.exists()) {
        throw ComicBookFileNotFoundException(file.absolutePath)
      }

      // Validate file format
      val extension = file.extension.lowercase()
      if (extension !in SUPPORTED_EXTENSIONS) {
        throw InvalidComicBookFormatException(extension, SUPPORTED_EXTENSIONS)
      }

      // Parse ComicInfo.xml
      try {
        ZipFile(file).use { zipFile ->
          val comicInfoEntry =
              zipFile.getEntry("ComicInfo.xml") ?: throw ComicInfoNotFoundException(file.name)

          try {
            zipFile
                .getInputStream(comicInfoEntry)
                .bufferedReader(ComicInfoXmlCodec.defaultCharset())
                .use { reader -> ComicInfoXmlCodec.decode(reader).toComicInfo() }
          } catch (e: XmlException) {
            throw ComicInfoParseException(e)
          } catch (e: SerializationException) {
            throw ComicInfoParseException(e)
          } catch (e: IllegalArgumentException) {
            throw ComicInfoParseException(e)
          }
        }
      } catch (e: IOException) {
        throw CorruptedArchiveException(file.absolutePath, e)
      }
    }
  }
}
