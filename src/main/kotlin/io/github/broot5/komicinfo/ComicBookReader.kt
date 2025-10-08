package io.github.broot5.komicinfo

import io.github.broot5.komicinfo.exceptions.*
import io.github.broot5.komicinfo.model.ComicInfo
import jakarta.xml.bind.JAXBContext
import jakarta.xml.bind.JAXBElement
import jakarta.xml.bind.JAXBException
import org.apache.commons.compress.archivers.zip.ZipFile
import java.io.File
import java.io.IOException
import javax.xml.transform.stream.StreamSource
import generated.ComicInfo as GeneratedComicInfo

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
        val jaxbContext = JAXBContext.newInstance(GeneratedComicInfo::class.java)
        val unmarshaller = jaxbContext.createUnmarshaller()

        ZipFile.builder().setFile(file).get().use { zipFile ->
          val comicInfoEntry =
              zipFile.getEntry("ComicInfo.xml") ?: throw ComicInfoNotFoundException(file.name)

          zipFile.getInputStream(comicInfoEntry).use { inputStream ->
            val source = StreamSource(inputStream)
            val jaxbElement: JAXBElement<GeneratedComicInfo> =
                unmarshaller.unmarshal(source, GeneratedComicInfo::class.java)
            jaxbElement.value.toComicInfo()
          }
        }
      } catch (e: JAXBException) {
        throw ComicInfoParseException(e)
      } catch (e: IOException) {
        throw CorruptedArchiveException(file.absolutePath, e)
      }
    }
  }
}
