package io.github.broot5.komicinfo.internal

import java.io.File

internal expect object ImageDimensions {
  fun read(file: File): Pair<Int, Int>?
}
