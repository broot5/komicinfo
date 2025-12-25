package io.github.broot5.komicinfo.internal

import java.io.File
import javax.imageio.ImageIO

internal actual object ImageDimensions {
  actual fun read(file: File): Pair<Int, Int>? {
    return runCatching {
          val suffix = file.extension.takeIf { it.isNotBlank() } ?: return null

          val readerIterator = ImageIO.getImageReadersBySuffix(suffix)
          if (!readerIterator.hasNext()) return null

          val imageInputStream = ImageIO.createImageInputStream(file) ?: return null
          imageInputStream.use { iis ->
            while (readerIterator.hasNext()) {
              val reader = readerIterator.next()
              try {
                reader.setInput(iis, /* seekForwardOnly= */ false, /* ignoreMetadata= */ true)
                val index = runCatching { reader.minIndex }.getOrDefault(0)
                val width = reader.getWidth(index)
                val height = reader.getHeight(index)
                if (width > 0 && height > 0) return width to height
              } catch (_: Throwable) {
                // Try next reader
              } finally {
                runCatching { reader.dispose() }
                runCatching { iis.seek(0) }
              }
            }

            return null
          }
        }
        .getOrNull()
  }
}
