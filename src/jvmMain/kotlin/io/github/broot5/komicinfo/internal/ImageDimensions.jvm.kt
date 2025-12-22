package io.github.broot5.komicinfo.internal

import java.io.File
import javax.imageio.ImageIO

internal actual object ImageDimensions {
  actual fun read(file: File): Pair<Int, Int>? {
    return runCatching { ImageIO.read(file)?.let { it.width to it.height } }.getOrNull()
  }
}
