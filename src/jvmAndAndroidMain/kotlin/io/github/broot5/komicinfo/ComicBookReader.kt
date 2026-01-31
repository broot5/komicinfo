package io.github.broot5.komicinfo

import io.github.broot5.komicinfo.exceptions.ComicBookFileNotFoundException
import io.github.broot5.komicinfo.exceptions.ComicInfoNotFoundException
import io.github.broot5.komicinfo.exceptions.ComicInfoParseException
import io.github.broot5.komicinfo.exceptions.CorruptedArchiveException
import io.github.broot5.komicinfo.exceptions.InvalidComicBookFormatException
import io.github.broot5.komicinfo.internal.toReaderException
import io.github.broot5.komicinfo.model.ComicInfo
import io.github.broot5.komicinfo.xml.ComicInfoXmlCodec
import java.io.File
import java.util.zip.ZipFile

public object ComicBookReader {
  private val SUPPORTED_EXTENSIONS = listOf("cbz", "zip")

  /**
   * Reads ComicInfo metadata from a CBZ archive file.
   *
   * @param file The CBZ archive file to read
   * @return [Result] containing [ComicInfo] on success.
   *
   * On failure, returns `Result.failure(exception)` where `exception` is typically one of:
   * - [ComicBookFileNotFoundException] if the file doesn't exist
   * - [InvalidComicBookFormatException] if the file format is not supported
   * - [ComicInfoNotFoundException] if ComicInfo.xml is not found in the archive
   * - [ComicInfoParseException] if ComicInfo.xml cannot be parsed
   * - [CorruptedArchiveException] if the archive is corrupted or cannot be read
   */
  public fun read(file: File): Result<ComicInfo> {
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
          ZipFile(file).use { zipFile ->
            val comicInfoEntry =
                zipFile.getEntry("ComicInfo.xml") ?: throw ComicInfoNotFoundException(file.name)

            zipFile.getInputStream(comicInfoEntry).use { inputStream ->
              ComicInfoXmlCodec.decode(inputStream.readBytes()).toComicInfo()
            }
          }
        }
        .recoverCatching { e -> throw e.toReaderException(file) }
  }
}
