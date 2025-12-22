# komicinfo

A Kotlin library for reading and writing ComicBook archives (CBZ)
with [ComicInfo.xml](https://anansi-project.github.io/docs/comicinfo/schemas/v2.1) metadata.

This project is Kotlin Multiplatform (KMP) and publishes artifacts for **JVM** and **Android**.

## Features

- **Read CBZ files** - Parse ComicInfo.xml metadata from CBZ archives
- **Write CBZ files** - Create CBZ archives with embedded ComicInfo.xml

## Example

### Reading a CBZ file

```kotlin
val result = ComicBookReader.read(File("comic.cbz"))

result
  .onSuccess { comicInfo ->
    println("Title: ${comicInfo.title}")
    println("Series: ${comicInfo.series}")
    println("Pages: ${comicInfo.pageCount}")
  }
  .onFailure { exception -> println("Failed to read: ${exception.message}") }
```

### Writing a CBZ file

```kotlin
// Create metadata
val info = ComicInfo(
  title = "My Comic",
  series = "My Series",
  number = "1",
  writer = listOf("A", "B"),
  publisher = "My Publisher",
  languageISO = "en",
)

// List of image files in page order
val imageFiles = listOf(
  File("page01.jpg"),
  File("page02.jpg"),
  File("page03.jpg"),
)

// Create ComicBook
val comicBook = ComicBook.create(info, imageFiles)

// Write to CBZ
val result = ComicBookWriter.write(comicBook, File("output.cbz"))

result
  .onSuccess { file -> println("Successfully created: ${file.absolutePath}") }
  .onFailure { exception -> println("Failed to write: ${exception.message}") }
```

### Customizing page information

```kotlin
val comicBook = ComicBook.create(info, imageFiles) { page, file ->
  when {
    file.name.contains("cover") -> page.copy(type = ComicPageType.FRONT_COVER)
    file.name.contains("credits") -> page.copy(bookmark = "Credits")
    else -> page
  }
}
```

## Building from Source

```bash
# Clone the repository
git clone https://github.com/broot5/komicinfo.git
cd komicinfo

# Build
./gradlew build

# Generate JVM JAR
./gradlew jvmJar
```
