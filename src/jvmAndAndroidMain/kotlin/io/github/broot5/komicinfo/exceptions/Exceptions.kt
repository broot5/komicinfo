package io.github.broot5.komicinfo.exceptions

/** Base exception */
public sealed class ComicBookException(message: String, cause: Throwable? = null) :
    Exception(message, cause)

/** Thrown when a file is not found or cannot be accessed */
public class ComicBookFileNotFoundException(path: String, cause: Throwable? = null) :
    ComicBookException("Comic book file not found: $path", cause)

/** Thrown when file format is invalid or unsupported */
public class InvalidComicBookFormatException(
    format: String,
    supportedFormats: List<String> = listOf("cbz", "zip"),
) :
    ComicBookException(
        "Invalid file format: $format. Supported formats: ${supportedFormats.joinToString()}"
    )

/** Thrown when ComicInfo.xml is missing from the archive */
public class ComicInfoNotFoundException(filename: String) :
    ComicBookException("ComicInfo.xml not found in archive: $filename")

/** Thrown when ComicInfo.xml cannot be parsed */
public class ComicInfoParseException(cause: Throwable) :
    ComicBookException("Failed to parse ComicInfo.xml", cause)

/** Thrown when writing a comic book archive fails */
public class ComicBookWriteException(path: String, cause: Throwable? = null) :
    ComicBookException("Failed to write comic book to: $path", cause)

/** Thrown when archive is corrupted or cannot be read */
public class CorruptedArchiveException(path: String, cause: Throwable? = null) :
    ComicBookException("Archive file is corrupted or cannot be read: $path", cause)
