package io.github.broot5.komicinfo

import io.github.broot5.komicinfo.exceptions.ComicBookFileNotFoundException
import io.github.broot5.komicinfo.exceptions.ComicBookWriteException
import jakarta.xml.bind.JAXBContext
import jakarta.xml.bind.JAXBElement
import jakarta.xml.bind.JAXBException
import jakarta.xml.bind.Marshaller
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream
import java.io.*
import javax.xml.namespace.QName
import generated.ComicInfo as GeneratedComicInfo

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
              "komic-info-",
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
    val generatedComicInfo = comicBook.info.toGeneratedComicInfo()

    try {
      val jaxbContext = JAXBContext.newInstance(GeneratedComicInfo::class.java)
      val marshaller =
          jaxbContext.createMarshaller().apply {
            setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true)
          }

      val jaxbElement =
          JAXBElement(
              QName("ComicInfo"),
              GeneratedComicInfo::class.java,
              generatedComicInfo,
          )

      val stringWriter = StringWriter()
      marshaller.marshal(jaxbElement, stringWriter)
      val xmlString = stringWriter.toString()

      ZipArchiveOutputStream(BufferedOutputStream(FileOutputStream(file))).use { zipStream ->
        // Write ComicInfo.xml
        val xmlEntry = ZipArchiveEntry("ComicInfo.xml")
        zipStream.putArchiveEntry(xmlEntry)
        zipStream.write(xmlString.toByteArray(Charsets.UTF_8))
        zipStream.closeArchiveEntry()

        // Write image files
        comicBook.imageFiles.forEach { imageFile ->
          val imageEntry = ZipArchiveEntry(imageFile.name)
          zipStream.putArchiveEntry(imageEntry)
          imageFile.inputStream().use { it.copyTo(zipStream) }
          zipStream.closeArchiveEntry()
        }

        zipStream.finish()
      }
    } catch (e: JAXBException) {
      throw ComicBookWriteException(file.absolutePath, e)
    } catch (e: IOException) {
      throw ComicBookWriteException(file.absolutePath, e)
    }
  }
}
