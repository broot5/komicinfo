package io.github.broot5.komicinfo.internal

import java.util.zip.CRC32
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

internal fun ZipOutputStream.putStoredEntry(name: String, bytes: ByteArray) {
  val crc32 = CRC32().apply { update(bytes) }
  val size = bytes.size.toLong()

  val entry =
      ZipEntry(name).apply {
        method = ZipEntry.STORED
        this.size = size
        compressedSize = size
        crc = crc32.value
      }

  putNextEntry(entry)
  write(bytes)
  closeEntry()
}
