package io.github.broot5.komicinfo.xml

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

  fun encode(value: ComicInfoXml, charset: Charset = defaultCharset()): ByteArray {
    val xmlString = xml.encodeToString(value)
    return xmlString.toByteArray(charset)
  }

  fun decode(bytes: ByteArray, charset: Charset = defaultCharset()): ComicInfoXml {
    return xml.decodeFromString(String(bytes, charset))
  }

  fun defaultCharset(): Charset = Charsets.UTF_8
}
