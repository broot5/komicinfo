package io.github.broot5.komicinfo.xml

import java.io.Reader
import java.nio.charset.Charset
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import nl.adaptivity.xmlutil.XmlDeclMode
import nl.adaptivity.xmlutil.core.XmlVersion
import nl.adaptivity.xmlutil.serialization.XML

internal object ComicInfoXmlCodec {
  private val xml: XML = XML {
    indentString = "  "
    repairNamespaces = true
    xmlDeclMode = XmlDeclMode.Charset
    xmlVersion = XmlVersion.XML10
  }

  fun encode(value: ComicInfoXml): ByteArray {
    val xmlString = xml.encodeToString(value)
    return xmlString.toByteArray(Charsets.UTF_8)
  }

  fun decode(reader: Reader): ComicInfoXml = xml.decodeFromString(reader.readText())

  fun defaultCharset(): Charset = Charsets.UTF_8
}
