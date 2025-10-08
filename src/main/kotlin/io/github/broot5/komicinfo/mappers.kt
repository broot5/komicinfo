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
  // Convert null to XSD default value -1
  generated.count = this.count ?: -1
  generated.volume = this.volume ?: -1
  generated.alternateSeries = this.alternateSeries
  generated.alternateNumber = this.alternateNumber
  generated.alternateCount = this.alternateCount ?: -1
  generated.summary = this.summary
  generated.notes = this.notes

  if (this.date != null) {
    generated.year = this.date.year
    generated.month = this.date.monthValue
    generated.day = this.date.dayOfMonth
  } else {
    // Set XSD default values when null
    generated.year = -1
    generated.month = -1
    generated.day = -1
  }

  generated.writer = this.writer.join()
  generated.penciller = this.penciller.join()
  generated.inker = this.inker.join()
  generated.colorist = this.colorist.join()
  generated.letterer = this.letterer.join()
  generated.coverArtist = this.coverArtist.join()
  generated.editor = this.editor.join()
  generated.translator = this.translator.join()

  generated.publisher = this.publisher
  generated.imprint = this.imprint

  generated.genre = this.genre.join()
  generated.tags = this.tags.join()
  generated.web = this.web.join(" ")

  generated.pageCount = this.pageCount
  generated.languageISO = this.languageISO
  generated.format = this.format

  generated.blackAndWhite = GeneratedYesNo.fromValue(this.blackAndWhite.value)
  generated.manga = GeneratedManga.fromValue(this.manga.value)

  generated.characters = this.characters.join()
  generated.teams = this.teams.join()
  generated.locations = this.locations.join()

  generated.scanInformation = this.scanInformation

  generated.storyArc = this.storyArc.join()
  generated.storyArcNumber = this.storyArcNumber.join()
  generated.seriesGroup = this.seriesGroup.join()

  generated.ageRating = GeneratedAgeRating.fromValue(this.ageRating.value)

  if (this.pages.isNotEmpty()) {
    val pageArray = ArrayOfComicPageInfo()
    this.pages.forEach { page ->
      val genPage =
          GeneratedComicPageInfo().apply {
            this.image = page.image
            this.type.add(page.type.value)
            this.isDoublePage = page.doublePage
            this.imageSize = page.imageSize
            this.key = page.key
            this.bookmark = page.bookmark
            // Convert null to XSD default value -1
            this.imageWidth = page.imageWidth ?: -1
            this.imageHeight = page.imageHeight ?: -1
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
      // Convert XSD default value -1 to null
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
      // PageCount: 0 is a valid value, so use it as is
      pageCount = this.pageCount?.let { if (it >= 0) it else 0 } ?: 0,
      languageISO = this.languageISO,
      format = this.format,
      blackAndWhite = YesNo.fromValue(this.blackAndWhite?.value()),
      manga = Manga.fromValue(this.manga?.value()),
      characters = this.characters.toList(),
      teams = this.teams.toList(),
      locations = this.locations.toList(),
      scanInformation = this.scanInformation,
      storyArc = this.storyArc.toList(),
      storyArcNumber = this.storyArcNumber.toList(),
      seriesGroup = this.seriesGroup.toList(),
      ageRating = AgeRating.fromValue(this.ageRating?.value()),
      pages =
          this.pages?.page?.map { genPage ->
            ComicPage(
                image = genPage.image,
                type = ComicPageType.fromValue(genPage.type.firstOrNull()),
                doublePage = genPage.isDoublePage,
                imageSize = genPage.imageSize,
                key = genPage.key,
                bookmark = genPage.bookmark,
                // Convert XSD default value -1 to null
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
