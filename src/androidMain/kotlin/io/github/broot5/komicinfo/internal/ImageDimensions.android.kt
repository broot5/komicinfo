package io.github.broot5.komicinfo.internal

import android.graphics.BitmapFactory
import java.io.File

internal actual object ImageDimensions {
  actual fun read(file: File): Pair<Int, Int>? {
    val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(file.absolutePath, options)

    val width = options.outWidth
    val height = options.outHeight
    return if (width > 0 && height > 0) width to height else null
  }
}
