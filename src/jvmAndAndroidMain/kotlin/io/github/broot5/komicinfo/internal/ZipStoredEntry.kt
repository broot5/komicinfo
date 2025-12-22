package io.github.broot5.komicinfo.internal

import java.io.InputStream
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

internal fun ZipOutputStream.putStoredEntry(
    name: String,
    openStream: () -> InputStream,
) {
  val crc32 = CRC32()
  var size = 0L

  // Compute size + CRC
  openStream().use { input ->
    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
    while (true) {
      val read = input.read(buffer)
      if (read < 0) break
      crc32.update(buffer, 0, read)
      size += read.toLong()
    }
  }

  val entry =
      ZipEntry(name).apply {
        method = ZipEntry.STORED
        this.size = size
        compressedSize = size
        crc = crc32.value
      }

  // Write data
  putNextEntry(entry)
  openStream().use { input -> input.copyTo(this) }
  closeEntry()
}
