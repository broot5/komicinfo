package io.github.broot5.komicinfo

import io.github.broot5.komicinfo.model.*
import io.github.broot5.komicinfo.xml.*
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate

private fun String?.toList(delimiter: Char = ','): List<String> =
    this?.split(delimiter)?.map(String::trim)?.filter(String::isNotEmpty) ?: emptyList()

private fun List<String>.joinOrBlank(delimiter: String = ","): String =
    if (isEmpty()) "" else joinToString(delimiter)

private fun String?.nullIfBlank(): String? = this?.takeIf { it.isNotBlank() }

fun ComicInfo.toComicInfoXml(): ComicInfoXml {
  val pageContainer =
      if (pages.isEmpty()) {
        ArrayOfComicPageInfoXml()
      } else {
        ArrayOfComicPageInfoXml(Page = pages.map { it.toXml() })
      }

  return ComicInfoXml(
      Title = title.orEmpty(),
      Series = series.orEmpty(),
      Number = number.orEmpty(),
      Count = count ?: -1,
      Volume = volume ?: -1,
      AlternateSeries = alternateSeries.orEmpty(),
      AlternateNumber = alternateNumber.orEmpty(),
      AlternateCount = alternateCount ?: -1,
      Summary = summary.orEmpty(),
      Notes = notes.orEmpty(),
      Year = date?.year ?: -1,
      Month = date?.monthValue ?: -1,
      Day = date?.dayOfMonth ?: -1,
      Writer = writer.joinOrBlank(),
      Penciller = penciller.joinOrBlank(),
      Inker = inker.joinOrBlank(),
      Colorist = colorist.joinOrBlank(),
      Letterer = letterer.joinOrBlank(),
      CoverArtist = coverArtist.joinOrBlank(),
      Editor = editor.joinOrBlank(),
      Translator = translator.joinOrBlank(),
      Publisher = publisher.orEmpty(),
      Imprint = imprint.orEmpty(),
      Genre = genre.joinOrBlank(),
      Tags = tags.joinOrBlank(),
      Web = web.joinOrBlank(" "),
      PageCount = pageCount.coerceAtLeast(0),
      LanguageISO = languageISO.orEmpty(),
      Format = format.orEmpty(),
      BlackAndWhite = blackAndWhite.toXml(),
      Manga = manga.toXml(),
      Characters = characters.joinOrBlank(),
      Teams = teams.joinOrBlank(),
      Locations = locations.joinOrBlank(),
      ScanInformation = scanInformation.orEmpty(),
      StoryArc = storyArc.joinOrBlank(),
      StoryArcNumber = storyArcNumber.joinOrBlank(),
      SeriesGroup = seriesGroup.joinOrBlank(),
      AgeRating = ageRating.toXml(),
      Pages = pageContainer,
      CommunityRating = communityRating?.toRatingXml(),
      MainCharacterOrTeam = mainCharacterOrTeam.orEmpty(),
      Review = review.orEmpty(),
      GTIN = gtin.orEmpty(),
  )
}

fun ComicInfoXml.toComicInfo(): ComicInfo {
  val date =
      runCatching {
            if (Year > 0 && Month > 0 && Day > 0) {
              LocalDate.of(Year, Month, Day)
            } else null
          }
          .getOrNull()

  return ComicInfo(
      title = Title.nullIfBlank(),
      series = Series.nullIfBlank(),
      number = Number.nullIfBlank(),
      count = Count.takeIf { it > 0 },
      volume = Volume.takeIf { it > 0 },
      alternateSeries = AlternateSeries.nullIfBlank(),
      alternateNumber = AlternateNumber.nullIfBlank(),
      alternateCount = AlternateCount.takeIf { it > 0 },
      summary = Summary.nullIfBlank(),
      notes = Notes.nullIfBlank(),
      date = date,
      writer = Writer.toList(),
      penciller = Penciller.toList(),
      inker = Inker.toList(),
      colorist = Colorist.toList(),
      letterer = Letterer.toList(),
      coverArtist = CoverArtist.toList(),
      editor = Editor.toList(),
      translator = Translator.toList(),
      publisher = Publisher.nullIfBlank(),
      imprint = Imprint.nullIfBlank(),
      genre = Genre.toList(),
      tags = Tags.toList(),
      web = Web.toList(' '),
      pageCount = PageCount.coerceAtLeast(0),
      languageISO = LanguageISO.nullIfBlank(),
      format = Format.nullIfBlank(),
      blackAndWhite = BlackAndWhite.toModel(),
      manga = Manga.toModel(),
      characters = Characters.toList(),
      teams = Teams.toList(),
      locations = Locations.toList(),
      scanInformation = ScanInformation.nullIfBlank(),
      storyArc = StoryArc.toList(),
      storyArcNumber = StoryArcNumber.toList(),
      seriesGroup = SeriesGroup.toList(),
      ageRating = AgeRating.toModel(),
      pages = Pages.Page.map { it.toModel() },
      communityRating = CommunityRating?.value?.toFloat(),
      mainCharacterOrTeam = MainCharacterOrTeam.nullIfBlank(),
      review = Review.nullIfBlank(),
      gtin = GTIN.nullIfBlank(),
  )
}

private fun ComicPage.toXml(): ComicPageInfoXml =
    ComicPageInfoXml(
        Image = image,
        Type = type?.toXml() ?: ComicPageTypeXml.Story,
        DoublePage = doublePage == true,
        ImageSize = imageSize ?: 0,
        Key = key.orEmpty(),
        Bookmark = bookmark.orEmpty(),
        ImageWidth = imageWidth ?: -1,
        ImageHeight = imageHeight ?: -1,
    )

private fun ComicPageInfoXml.toModel(): ComicPage =
    ComicPage(
        image = Image,
        type = Type.takeUnless { it == ComicPageTypeXml.Story }?.toModel(),
        doublePage = if (DoublePage) true else null,
        imageSize = ImageSize.takeIf { it > 0 },
        key = Key.nullIfBlank(),
        bookmark = Bookmark.nullIfBlank(),
        imageWidth = ImageWidth.takeIf { it > 0 },
        imageHeight = ImageHeight.takeIf { it > 0 },
    )

private fun YesNo?.toXml(): YesNoXml =
    when (this) {
      YesNo.YES -> YesNoXml.Yes
      YesNo.NO -> YesNoXml.No
      else -> YesNoXml.Unknown
    }

private fun YesNoXml?.toModel(): YesNo? =
    when (this) {
      YesNoXml.Yes -> YesNo.YES
      YesNoXml.No -> YesNo.NO
      else -> null
    }

private fun Manga?.toXml(): MangaXml =
    when (this) {
      Manga.YES -> MangaXml.Yes
      Manga.NO -> MangaXml.No
      Manga.YES_AND_RIGHT_TO_LEFT -> MangaXml.YesAndRightToLeft
      else -> MangaXml.Unknown
    }

private fun MangaXml?.toModel(): Manga? =
    when (this) {
      MangaXml.Yes -> Manga.YES
      MangaXml.No -> Manga.NO
      MangaXml.YesAndRightToLeft -> Manga.YES_AND_RIGHT_TO_LEFT
      else -> null
    }

private fun AgeRating?.toXml(): AgeRatingXml =
    when (this) {
      AgeRating.ADULTS_ONLY_18_PLUS -> AgeRatingXml.ADULTS_ONLY_18_PLUS
      AgeRating.EARLY_CHILDHOOD -> AgeRatingXml.EARLY_CHILDHOOD
      AgeRating.EVERYONE -> AgeRatingXml.Everyone
      AgeRating.EVERYONE_10_PLUS -> AgeRatingXml.EVERYONE_10_PLUS
      AgeRating.G -> AgeRatingXml.G
      AgeRating.KIDS_TO_ADULTS -> AgeRatingXml.KIDS_TO_ADULTS
      AgeRating.M -> AgeRatingXml.M
      AgeRating.MA15_PLUS -> AgeRatingXml.MA15_PLUS
      AgeRating.MATURE_17_PLUS -> AgeRatingXml.MATURE_17_PLUS
      AgeRating.PG -> AgeRatingXml.PG
      AgeRating.R18_PLUS -> AgeRatingXml.R18_PLUS
      AgeRating.RATING_PENDING -> AgeRatingXml.RATING_PENDING
      AgeRating.TEEN -> AgeRatingXml.Teen
      AgeRating.X18_PLUS -> AgeRatingXml.X18_PLUS
      else -> AgeRatingXml.Unknown
    }

private fun AgeRatingXml?.toModel(): AgeRating? =
    when (this) {
      AgeRatingXml.ADULTS_ONLY_18_PLUS -> AgeRating.ADULTS_ONLY_18_PLUS
      AgeRatingXml.EARLY_CHILDHOOD -> AgeRating.EARLY_CHILDHOOD
      AgeRatingXml.Everyone -> AgeRating.EVERYONE
      AgeRatingXml.EVERYONE_10_PLUS -> AgeRating.EVERYONE_10_PLUS
      AgeRatingXml.G -> AgeRating.G
      AgeRatingXml.KIDS_TO_ADULTS -> AgeRating.KIDS_TO_ADULTS
      AgeRatingXml.M -> AgeRating.M
      AgeRatingXml.MA15_PLUS -> AgeRating.MA15_PLUS
      AgeRatingXml.MATURE_17_PLUS -> AgeRating.MATURE_17_PLUS
      AgeRatingXml.PG -> AgeRating.PG
      AgeRatingXml.R18_PLUS -> AgeRating.R18_PLUS
      AgeRatingXml.RATING_PENDING -> AgeRating.RATING_PENDING
      AgeRatingXml.Teen -> AgeRating.TEEN
      AgeRatingXml.X18_PLUS -> AgeRating.X18_PLUS
      else -> null
    }

private fun ComicPageType?.toXml(): ComicPageTypeXml =
    when (this) {
      ComicPageType.FRONT_COVER -> ComicPageTypeXml.FrontCover
      ComicPageType.INNER_COVER -> ComicPageTypeXml.InnerCover
      ComicPageType.ROUNDUP -> ComicPageTypeXml.Roundup
      ComicPageType.ADVERTISEMENT -> ComicPageTypeXml.Advertisement
      ComicPageType.EDITORIAL -> ComicPageTypeXml.Editorial
      ComicPageType.LETTERS -> ComicPageTypeXml.Letters
      ComicPageType.PREVIEW -> ComicPageTypeXml.Preview
      ComicPageType.BACK_COVER -> ComicPageTypeXml.BackCover
      ComicPageType.OTHER -> ComicPageTypeXml.Other
      ComicPageType.DELETED -> ComicPageTypeXml.Deleted
      else -> ComicPageTypeXml.Story
    }

private fun ComicPageTypeXml.toModel(): ComicPageType =
    when (this) {
      ComicPageTypeXml.FrontCover -> ComicPageType.FRONT_COVER
      ComicPageTypeXml.InnerCover -> ComicPageType.INNER_COVER
      ComicPageTypeXml.Roundup -> ComicPageType.ROUNDUP
      ComicPageTypeXml.Story -> ComicPageType.STORY
      ComicPageTypeXml.Advertisement -> ComicPageType.ADVERTISEMENT
      ComicPageTypeXml.Editorial -> ComicPageType.EDITORIAL
      ComicPageTypeXml.Letters -> ComicPageType.LETTERS
      ComicPageTypeXml.Preview -> ComicPageType.PREVIEW
      ComicPageTypeXml.BackCover -> ComicPageType.BACK_COVER
      ComicPageTypeXml.Other -> ComicPageType.OTHER
      ComicPageTypeXml.Deleted -> ComicPageType.DELETED
    }

private fun Float.toRatingXml(): RatingXml {
  val scaled = BigDecimal.valueOf(this.toDouble()).setScale(1, RoundingMode.HALF_UP)
  return RatingXml(scaled)
}
