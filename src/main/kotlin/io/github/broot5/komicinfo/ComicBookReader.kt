package io.github.broot5.komicinfo

import jakarta.xml.bind.JAXBContext
import jakarta.xml.bind.JAXBElement
import org.apache.commons.compress.archivers.zip.ZipFile
import java.io.File
import javax.xml.transform.stream.StreamSource
import generated.ComicInfo as GeneratedComicInfo

object ComicBookReader {
  fun read(file: File): Result<ComicInfo> {
    return runCatching {
      require(file.exists()) { "File not found: ${file.absolutePath}" }
      require(file.extension.lowercase() in listOf("cbz", "zip")) {
        "Invalid file format: ${file.extension}"
      }

      val jaxbContext = JAXBContext.newInstance(GeneratedComicInfo::class.java)
      val unmarshaller = jaxbContext.createUnmarshaller()

      ZipFile.builder().setFile(file).get().use { zipFile ->
        val comicInfoEntry =
            zipFile.getEntry("ComicInfo.xml")
                ?: throw IllegalArgumentException("ComicInfo.xml not found")

        zipFile.getInputStream(comicInfoEntry).use { inputStream ->
          val source = StreamSource(inputStream)
          val jaxbElement: JAXBElement<GeneratedComicInfo> =
              unmarshaller.unmarshal(source, GeneratedComicInfo::class.java)
          jaxbElement.value.toComicInfo()
        }
      }
    }
  }
}
