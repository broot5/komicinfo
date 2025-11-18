package io.github.broot5.komicinfo

import io.github.broot5.komicinfo.exceptions.ComicBookFileNotFoundException
import io.github.broot5.komicinfo.exceptions.ComicBookWriteException
import io.github.broot5.komicinfo.xml.ComicInfoXmlCodec
import nl.adaptivity.xmlutil.XmlException
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object ComicBookWriter {
  /**
   * Writes a ComicBook to a CBZ archive file.
   *
   * @param comicBook The ComicBook to write
   * @param destination The target file path for the CBZ archive
   * @return Result containing the destination file on success, or exception details on failure
   * @throws ComicBookFileNotFoundException if any image file doesn't exist
   * @throws ComicBookWriteException if writing fails
   */
  fun write(comicBook: ComicBook, destination: File): Result<File> {
    return runCatching {
      // Validate all image files exist before starting
      comicBook.imageFiles.forEachIndexed { index, imageFile ->
        if (!imageFile.exists()) {
          throw ComicBookFileNotFoundException(
              "Image file #$index not found: ${imageFile.absolutePath}"
          )
        }
      }

      // Create parent directory if it doesn't exist
      destination.parentFile?.mkdirs()

      // Use temporary file
      val tempFile =
          File.createTempFile(
              "komicinfo-",
              ".tmp",
              destination.parentFile ?: File(System.getProperty("java.io.tmpdir")),
          )

      try {
        writeToFile(comicBook, tempFile)

        // Atomic move
        if (destination.exists()) {
          destination.delete()
        }
        if (!tempFile.renameTo(destination)) {
          tempFile.copyTo(destination, overwrite = true)
          tempFile.delete()
        }

        destination
      } catch (e: Exception) {
        // Clean up temp file on failure
        tempFile.delete()
        throw ComicBookWriteException(destination.absolutePath, e)
      }
    }
  }

  private fun writeToFile(comicBook: ComicBook, file: File) {
    val comicInfoXml = comicBook.info.toComicInfoXml()

    try {
      val xmlBytes = ComicInfoXmlCodec.encode(comicInfoXml)

      ZipArchiveOutputStream(BufferedOutputStream(FileOutputStream(file))).use { zipStream ->
        // Write ComicInfo.xml
        val xmlEntry =
            ZipArchiveEntry("ComicInfo.xml").apply {
              method = ZipArchiveEntry.STORED
              size = xmlBytes.size.toLong()
              crc = java.util.zip.CRC32().apply { update(xmlBytes) }.value
            }
        zipStream.putArchiveEntry(xmlEntry)
        zipStream.write(xmlBytes)
        zipStream.closeArchiveEntry()

        // Write image files
        comicBook.imageFiles.forEach { imageFile ->
          val imageBytes = imageFile.readBytes()
          val imageEntry =
              ZipArchiveEntry(imageFile.name).apply {
                method = ZipArchiveEntry.STORED
                size = imageBytes.size.toLong()
                crc = java.util.zip.CRC32().apply { update(imageBytes) }.value
              }
          zipStream.putArchiveEntry(imageEntry)
          zipStream.write(imageBytes)
          zipStream.closeArchiveEntry()
        }

        zipStream.finish()
      }
    } catch (e: XmlException) {
      throw ComicBookWriteException(file.absolutePath, e)
    } catch (e: IOException) {
      throw ComicBookWriteException(file.absolutePath, e)
    }
  }
}
