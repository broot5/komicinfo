package io.github.broot5.komicinfo

import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import java.nio.file.Path
import javax.imageio.ImageIO

/** Helper utilities for testing comic book functionality */
object TestHelper {
  /**
   * Creates an image file for testing purposes
   *
   * @param tempDir The temporary directory to create the file in
   * @param filename The name of the file to create
   * @param width Image width in pixels
   * @param height Image height in pixels
   * @param format Image format (jpg, png, etc.)
   * @return The created image file
   */
  fun createImageFile(
      tempDir: Path,
      filename: String,
      width: Int = 800,
      height: Int = 1200,
      format: String = "jpg",
  ): File {
    val file = tempDir.resolve(filename).toFile()
    val image = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
    val graphics = image.createGraphics()

    graphics.color = Color.WHITE
    graphics.fillRect(0, 0, width, height)

    graphics.color = Color.BLUE
    graphics.drawRect(10, 10, width - 20, height - 20)

    graphics.dispose()

    ImageIO.write(image, format, file)
    return file
  }
}
