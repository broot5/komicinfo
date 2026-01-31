package io.github.broot5.komicinfo.internal

import java.io.File
import java.util.zip.CRC32
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

private const val BUFFER_SIZE = 8192

private fun validateEntryName(name: String) {
  require(!name.contains("..")) { "Path traversal detected: $name" }
  require(!name.startsWith("/")) { "Absolute path not allowed: $name" }
  require(!name.contains("\\")) { "Backslash not allowed: $name" }
}

internal fun ZipOutputStream.putStoredEntry(name: String, bytes: ByteArray) {
  validateEntryName(name)
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

internal fun ZipOutputStream.putStoredEntry(name: String, file: File) {
  validateEntryName(name)
  val size = file.length()
  val crc32 = calculateCrc32(file)

  val entry =
      ZipEntry(name).apply {
        method = ZipEntry.STORED
        this.size = size
        compressedSize = size
        crc = crc32
      }

  putNextEntry(entry)
  file.inputStream().buffered().use { input ->
    val buffer = ByteArray(BUFFER_SIZE)
    var bytesRead: Int
    while (input.read(buffer).also { bytesRead = it } != -1) {
      write(buffer, 0, bytesRead)
    }
  }
  closeEntry()
}

private fun calculateCrc32(file: File): Long {
  val crc32 = CRC32()
  file.inputStream().buffered().use { input ->
    val buffer = ByteArray(BUFFER_SIZE)
    var bytesRead: Int
    while (input.read(buffer).also { bytesRead = it } != -1) {
      crc32.update(buffer, 0, bytesRead)
    }
  }
  return crc32.value
}
