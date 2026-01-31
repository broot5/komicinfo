package io.github.broot5.komicinfo.internal

import java.io.ByteArrayOutputStream
import java.nio.file.Path
import java.util.zip.ZipOutputStream
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

@DisplayName("ZipStoredEntry")
class ZipStoredEntryTest {

  @Nested
  @DisplayName("entry name validation")
  inner class EntryNameValidation {

    @Test
    @DisplayName("should reject path traversal with double dots")
    fun rejectPathTraversalWithDoubleDots() {
      val exception =
          assertThrows(IllegalArgumentException::class.java) { createZipWithEntry("../etc/passwd") }
      assertTrue(exception.message?.contains("Path traversal") == true)
    }

    @Test
    @DisplayName("should reject absolute paths starting with forward slash")
    fun rejectAbsolutePathsWithForwardSlash() {
      val exception =
          assertThrows(IllegalArgumentException::class.java) { createZipWithEntry("/etc/passwd") }
      assertTrue(exception.message?.contains("Absolute path") == true)
    }

    @Test
    @DisplayName("should reject paths containing backslash")
    fun rejectPathsWithBackslash() {
      val exception =
          assertThrows(IllegalArgumentException::class.java) {
            createZipWithEntry("foo\\bar\\image.jpg")
          }
      assertTrue(exception.message?.contains("Backslash") == true)
    }

    @Test
    @DisplayName("should reject Windows-style path traversal with backslash")
    fun rejectWindowsStylePathTraversal() {
      val exception =
          assertThrows(IllegalArgumentException::class.java) {
            createZipWithEntry("foo\\..\\..\\etc\\passwd")
          }
      // Either "Path traversal" or "Backslash" error is acceptable - both block the attack
      assertTrue(
          exception.message?.contains("Path traversal") == true ||
              exception.message?.contains("Backslash") == true
      )
    }

    @Test
    @DisplayName("should reject absolute Windows paths with backslash")
    fun rejectAbsoluteWindowsPaths() {
      val exception =
          assertThrows(IllegalArgumentException::class.java) {
            createZipWithEntry("\\Windows\\System32\\config")
          }
      assertTrue(exception.message?.contains("Backslash") == true)
    }

    @Test
    @DisplayName("should accept valid entry names")
    fun acceptValidEntryNames() {
      assertDoesNotThrow { createZipWithEntry("image.jpg") }
      assertDoesNotThrow { createZipWithEntry("folder/image.jpg") }
      assertDoesNotThrow { createZipWithEntry("ComicInfo.xml") }
      assertDoesNotThrow { createZipWithEntry("chapter1/page01.png") }
    }
  }

  @Nested
  @DisplayName("putStoredEntry with file")
  inner class PutStoredEntryWithFile {
    @TempDir lateinit var tempDir: Path

    @Test
    @DisplayName("should reject path traversal when writing file")
    fun rejectPathTraversalWithFile() {
      val testFile = tempDir.resolve("test.jpg").toFile()
      testFile.writeBytes(ByteArray(10))

      val exception =
          assertThrows(IllegalArgumentException::class.java) {
            ByteArrayOutputStream().use { baos ->
              ZipOutputStream(baos).use { zos -> zos.putStoredEntry("../test.jpg", testFile) }
            }
          }
      assertTrue(exception.message?.contains("Path traversal") == true)
    }

    @Test
    @DisplayName("should reject backslash paths when writing file")
    fun rejectBackslashWithFile() {
      val testFile = tempDir.resolve("test.jpg").toFile()
      testFile.writeBytes(ByteArray(10))

      val exception =
          assertThrows(IllegalArgumentException::class.java) {
            ByteArrayOutputStream().use { baos ->
              ZipOutputStream(baos).use { zos -> zos.putStoredEntry("folder\\test.jpg", testFile) }
            }
          }
      assertTrue(exception.message?.contains("Backslash") == true)
    }
  }

  private fun createZipWithEntry(entryName: String) {
    ByteArrayOutputStream().use { baos ->
      ZipOutputStream(baos).use { zos -> zos.putStoredEntry(entryName, ByteArray(10)) }
    }
  }
}
