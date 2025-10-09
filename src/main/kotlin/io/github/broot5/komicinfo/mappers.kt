package io.github.broot5.komicinfo

import generated.ArrayOfComicPageInfo
import io.github.broot5.komicinfo.model.*
import java.time.LocalDate
import generated.AgeRating as GeneratedAgeRating
import generated.ComicInfo as GeneratedComicInfo
import generated.ComicPageInfo as GeneratedComicPageInfo
import generated.Manga as GeneratedManga
import generated.YesNo as GeneratedYesNo

private fun String?.toList(delimiter: Char = ','): List<String> =
    this?.split(delimiter)?.map(String::trim)?.filter(String::isNotEmpty) ?: emptyList()

private fun List<String>.join(delimiter: String = ","): String = this.joinToString(delimiter)

fun ComicInfo.toGeneratedComicInfo(): GeneratedComicInfo {
  val generated = GeneratedComicInfo()

  generated.title = this.title
  generated.series = this.series
  generated.number = this.number

  generated.count = this.count
  generated.volume = this.volume

  generated.alternateSeries = this.alternateSeries
  generated.alternateNumber = this.alternateNumber

  generated.alternateCount = this.alternateCount

  generated.summary = this.summary
  generated.notes = this.notes

  if (this.date != null) {
    generated.year = this.date.year
    generated.month = this.date.monthValue
    generated.day = this.date.dayOfMonth
  }

  if (this.writer.isNotEmpty()) generated.writer = this.writer.join()
  if (this.penciller.isNotEmpty()) generated.penciller = this.penciller.join()
  if (this.inker.isNotEmpty()) generated.inker = this.inker.join()
  if (this.colorist.isNotEmpty()) generated.colorist = this.colorist.join()
  if (this.letterer.isNotEmpty()) generated.letterer = this.letterer.join()
  if (this.coverArtist.isNotEmpty()) generated.coverArtist = this.coverArtist.join()
  if (this.editor.isNotEmpty()) generated.editor = this.editor.join()
  if (this.translator.isNotEmpty()) generated.translator = this.translator.join()

  generated.publisher = this.publisher
  generated.imprint = this.imprint

  if (this.genre.isNotEmpty()) generated.genre = this.genre.join()
  if (this.tags.isNotEmpty()) generated.tags = this.tags.join()
  if (this.web.isNotEmpty()) generated.web = this.web.join(" ")

  generated.pageCount = this.pageCount // Always included (0 is valid)

  generated.languageISO = this.languageISO
  generated.format = this.format

  generated.blackAndWhite = this.blackAndWhite?.let { GeneratedYesNo.fromValue(it.value) }
  generated.manga = this.manga?.let { GeneratedManga.fromValue(it.value) }

  if (this.characters.isNotEmpty()) generated.characters = this.characters.join()
  if (this.teams.isNotEmpty()) generated.teams = this.teams.join()
  if (this.locations.isNotEmpty()) generated.locations = this.locations.join()

  generated.scanInformation = this.scanInformation

  if (this.storyArc.isNotEmpty()) generated.storyArc = this.storyArc.join()
  if (this.storyArcNumber.isNotEmpty()) generated.storyArcNumber = this.storyArcNumber.join()
  if (this.seriesGroup.isNotEmpty()) generated.seriesGroup = this.seriesGroup.join()

  generated.ageRating = this.ageRating?.let { GeneratedAgeRating.fromValue(it.value) }

  if (this.pages.isNotEmpty()) {
    val pageArray = ArrayOfComicPageInfo()
    this.pages.forEach { page ->
      val genPage =
          GeneratedComicPageInfo().apply {
            this.image = page.image
            // Type: omit if null (XSD default = "Story")
            page.type?.let { this.type.add(it.value) }
            // DoublePage: omit if null or false (XSD default = false)
            if (page.doublePage == true) {
              this.isDoublePage = true
            }
            // ImageSize: set only if not null
            page.imageSize?.let { this.imageSize = it }
            this.key = page.key
            this.bookmark = page.bookmark
            // Dimensions: set only if not null
            page.imageWidth?.let { this.imageWidth = it }
            page.imageHeight?.let { this.imageHeight = it }
          }
      pageArray.page.add(genPage)
    }
    generated.pages = pageArray
  }

  generated.communityRating = this.communityRating?.toBigDecimal()

  generated.mainCharacterOrTeam = this.mainCharacterOrTeam
  generated.review = this.review
  generated.gtin = this.gtin

  return generated
}

fun GeneratedComicInfo.toComicInfo(): ComicInfo {
  // Convert Year, Month, Day to LocalDate
  val date =
      runCatching {
            if (
                this.year != null &&
                    this.month != null &&
                    this.day != null &&
                    this.year > 0 &&
                    this.month > 0 &&
                    this.day > 0
            ) {
              LocalDate.of(this.year, this.month, this.day)
            } else null
          }
          .getOrNull()

  return ComicInfo(
      title = this.title,
      series = this.series,
      number = this.number,
      // Integers: convert XSD default -1 to null
      count = this.count?.takeIf { it > 0 },
      volume = this.volume?.takeIf { it > 0 },
      alternateSeries = this.alternateSeries,
      alternateNumber = this.alternateNumber,
      alternateCount = this.alternateCount?.takeIf { it > 0 },
      summary = this.summary,
      notes = this.notes,
      date = date,
      writer = this.writer.toList(),
      penciller = this.penciller.toList(),
      inker = this.inker.toList(),
      colorist = this.colorist.toList(),
      letterer = this.letterer.toList(),
      coverArtist = this.coverArtist.toList(),
      editor = this.editor.toList(),
      translator = this.translator.toList(),
      publisher = this.publisher,
      imprint = this.imprint,
      genre = this.genre.toList(),
      tags = this.tags.toList(),
      web = this.web.toList(' '),
      pageCount = this.pageCount?.let { if (it >= 0) it else 0 } ?: 0, // 0 is valid
      languageISO = this.languageISO,
      format = this.format,
      // Enums: keep null if not present (no UNKNOWN fallback)
      blackAndWhite = this.blackAndWhite?.value()?.let { YesNo.fromValue(it) },
      manga = this.manga?.value()?.let { Manga.fromValue(it) },
      characters = this.characters.toList(),
      teams = this.teams.toList(),
      locations = this.locations.toList(),
      scanInformation = this.scanInformation,
      storyArc = this.storyArc.toList(),
      storyArcNumber = this.storyArcNumber.toList(),
      seriesGroup = this.seriesGroup.toList(),
      ageRating = this.ageRating?.value()?.let { AgeRating.fromValue(it) },
      pages =
          this.pages?.page?.map { genPage ->
            ComicPage(
                image = genPage.image,
                // Type: keep null if empty (XSD default = "Story")
                type = genPage.type.firstOrNull()?.let { ComicPageType.fromValue(it) },
                // DoublePage: convert false to null (XSD default = false)
                doublePage = if (genPage.isDoublePage) true else null,
                // ImageSize: keep null if <= 0
                imageSize = genPage.imageSize.takeIf { it > 0 },
                key = genPage.key,
                bookmark = genPage.bookmark,
                // Dimensions: convert -1 to null
                imageWidth = genPage.imageWidth.takeIf { it > 0 },
                imageHeight = genPage.imageHeight.takeIf { it > 0 },
            )
          } ?: emptyList(),
      communityRating = this.communityRating?.toFloat(),
      mainCharacterOrTeam = this.mainCharacterOrTeam,
      review = this.review,
      gtin = this.gtin,
  )
}
