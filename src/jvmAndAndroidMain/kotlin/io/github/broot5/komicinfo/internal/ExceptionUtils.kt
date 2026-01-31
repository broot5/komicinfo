package io.github.broot5.komicinfo.internal

import io.github.broot5.komicinfo.exceptions.ComicBookException
import io.github.broot5.komicinfo.exceptions.ComicBookWriteException
import io.github.broot5.komicinfo.exceptions.ComicInfoParseException
import io.github.broot5.komicinfo.exceptions.CorruptedArchiveException
import java.io.File
import java.io.IOException
import kotlinx.serialization.SerializationException
import nl.adaptivity.xmlutil.XmlException

internal fun Throwable.toReaderException(file: File): ComicBookException =
    when (this) {
      is ComicBookException -> this
      is XmlException -> ComicInfoParseException(this)
      is SerializationException -> ComicInfoParseException(this)
      is IllegalArgumentException -> ComicInfoParseException(this)
      is IOException -> CorruptedArchiveException(file.absolutePath, this)
      else -> CorruptedArchiveException(file.absolutePath, this)
    }

internal fun Throwable.toWriterException(destination: File): ComicBookException =
    when (this) {
      is ComicBookException -> this
      is XmlException -> ComicBookWriteException(destination.absolutePath, this)
      is IOException -> ComicBookWriteException(destination.absolutePath, this)
      else -> ComicBookWriteException(destination.absolutePath, this)
    }
