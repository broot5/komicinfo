package io.github.broot5.komicinfo

import jakarta.xml.bind.JAXBContext
import jakarta.xml.bind.JAXBElement
import jakarta.xml.bind.Marshaller
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.StringWriter
import javax.xml.namespace.QName
import generated.ComicInfo as GeneratedComicInfo

object ComicBookWriter {
  fun write(comicBook: ComicBook, destination: File): File {
    val generatedComicInfo = comicBook.info.toGeneratedComicInfo()

    val jaxbContext = JAXBContext.newInstance(GeneratedComicInfo::class.java)
    val marshaller =
        jaxbContext.createMarshaller().apply { setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true) }

    val jaxbElement =
        JAXBElement(
            QName("ComicInfo"),
            GeneratedComicInfo::class.java,
            generatedComicInfo,
        )

    val stringWriter = StringWriter()
    marshaller.marshal(jaxbElement, stringWriter)
    val xmlString = stringWriter.toString()

    ZipArchiveOutputStream(BufferedOutputStream(FileOutputStream(destination))).use { zipStream ->
      val xmlEntry = ZipArchiveEntry("ComicInfo.xml")
      zipStream.putArchiveEntry(xmlEntry)
      zipStream.write(xmlString.toByteArray())
      zipStream.closeArchiveEntry()

      comicBook.imageFiles.forEach { imageFile ->
        val imageEntry = ZipArchiveEntry(imageFile.name)
        zipStream.putArchiveEntry(imageEntry)
        imageFile.inputStream().use { it.copyTo(zipStream) }
        zipStream.closeArchiveEntry()
      }

      zipStream.finish()
    }

    return destination
  }
}
