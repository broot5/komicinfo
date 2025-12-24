package io.github.broot5.komicinfo.internal

import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

internal object AtomicReplace {
  fun moveTempIntoPlace(tempFile: File, destination: File) {
    Files.move(
        tempFile.toPath(),
        destination.toPath(),
        StandardCopyOption.REPLACE_EXISTING,
        StandardCopyOption.ATOMIC_MOVE,
    )
  }
}
