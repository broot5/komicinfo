package io.github.broot5.komicinfo

import io.github.broot5.komicinfo.exceptions.ComicBookException
import io.github.broot5.komicinfo.exceptions.ComicBookFileNotFoundException
import io.github.broot5.komicinfo.exceptions.ComicBookWriteException
import io.github.broot5.komicinfo.internal.AtomicReplace
import io.github.broot5.komicinfo.internal.putStoredEntry
import io.github.broot5.komicinfo.xml.ComicInfoXmlCodec
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.zip.ZipOutputStream
import nl.adaptivity.xmlutil.XmlException

public object ComicBookWriter {
  /**
   * Writes a ComicBook to a CBZ archive file.
   *
   * @param comicBook The ComicBook to write
   * @param destination The target file path for the CBZ archive
   * @return [Result] containing the destination [File] on success.
   *
   * On failure, returns `Result.failure(exception)` where `exception` is typically one of:
   * - [ComicBookFileNotFoundException] if any image file doesn't exist
   * - [ComicBookWriteException] if writing fails
   */
  public fun write(comicBook: ComicBook, destination: File): Result<File> {
    return runCatching {
          val destinationFile = destination.absoluteFile

          // Validate all image files exist before starting
          comicBook.imageFiles.forEach { imageFile ->
            if (!imageFile.exists()) {
              throw ComicBookFileNotFoundException(imageFile.absolutePath)
            }
          }

          // Create parent directory if it doesn't exist
          destinationFile.parentFile?.mkdirs()

          // Use temporary file
          val tmpDir =
              destinationFile.parentFile
                  ?: File(
                      requireNotNull(System.getProperty("java.io.tmpdir")) {
                        "System property 'java.io.tmpdir' is not set"
                      }
                  )
          val tempFile = File.createTempFile("komicinfo-", ".tmp", tmpDir)

          runCatching {
                writeToFile(comicBook, tempFile)
                AtomicReplace.moveTempIntoPlace(tempFile, destinationFile)
                destination
              }
              .onFailure { tempFile.delete() }
              .getOrThrow()
        }
        .recoverCatching { e -> throw e.toComicBookException(destination) }
  }

  private fun writeToFile(comicBook: ComicBook, file: File) {
    val comicInfoXml = comicBook.info.toComicInfoXml()
    val xmlBytes = ComicInfoXmlCodec.encode(comicInfoXml)

    ZipOutputStream(BufferedOutputStream(FileOutputStream(file))).use { zipStream ->
      // Write ComicInfo.xml
      zipStream.putStoredEntry(name = "ComicInfo.xml", bytes = xmlBytes)

      // Write image files
      comicBook.imageFiles.forEach { imageFile ->
        zipStream.putStoredEntry(imageFile.name, imageFile.readBytes())
      }
    }
  }

  private fun Throwable.toComicBookException(destination: File): ComicBookException =
      when (this) {
        is ComicBookException -> this
        is XmlException -> ComicBookWriteException(destination.absolutePath, this)
        is IOException -> ComicBookWriteException(destination.absolutePath, this)
        else -> ComicBookWriteException(destination.absolutePath, this)
      }
}
