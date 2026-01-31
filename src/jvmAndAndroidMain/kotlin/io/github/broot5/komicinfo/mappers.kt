package io.github.broot5.komicinfo

import io.github.broot5.komicinfo.internal.BiDirectionalEnumMapper
import io.github.broot5.komicinfo.model.*
import io.github.broot5.komicinfo.xml.*
import java.math.BigDecimal
import java.math.RoundingMode
import kotlinx.datetime.LocalDate
import kotlinx.datetime.number

private fun String?.toList(delimiter: Char = ','): List<String> =
    this?.split(delimiter)?.map(String::trim)?.filter(String::isNotEmpty) ?: emptyList()

private fun List<String>.joinOrNull(delimiter: String = ","): String? =
    if (isEmpty()) null else joinToString(delimiter)

private fun String?.nullIfBlank(): String? = this?.ifBlank { null }

internal fun ComicInfo.toComicInfoXml(): ComicInfoXml {
  val pageContainer =
      pages
          .takeIf { it.isNotEmpty() }
          ?.let { ArrayOfComicPageInfoXml(Page = it.map { page -> page.toXml() }) }

  return ComicInfoXml(
      Title = title.nullIfBlank(),
      Series = series.nullIfBlank(),
      Number = number.nullIfBlank(),
      Count = count?.takeIf { it > 0 },
      Volume = volume?.takeIf { it > 0 },
      AlternateSeries = alternateSeries.nullIfBlank(),
      AlternateNumber = alternateNumber.nullIfBlank(),
      AlternateCount = alternateCount?.takeIf { it > 0 },
      Summary = summary.nullIfBlank(),
      Notes = notes.nullIfBlank(),
      Year = date?.year,
      Month = date?.month?.number,
      Day = date?.day,
      Writer = writer.joinOrNull(),
      Penciller = penciller.joinOrNull(),
      Inker = inker.joinOrNull(),
      Colorist = colorist.joinOrNull(),
      Letterer = letterer.joinOrNull(),
      CoverArtist = coverArtist.joinOrNull(),
      Editor = editor.joinOrNull(),
      Translator = translator.joinOrNull(),
      Publisher = publisher.nullIfBlank(),
      Imprint = imprint.nullIfBlank(),
      Genre = genre.joinOrNull(),
      Tags = tags.joinOrNull(),
      Web = web.joinOrNull(" "),
      PageCount = pageCount,
      LanguageISO = languageISO.nullIfBlank(),
      Format = format.nullIfBlank(),
      BlackAndWhite = blackAndWhite?.toXml(),
      Manga = manga?.toXml(),
      Characters = characters.joinOrNull(),
      Teams = teams.joinOrNull(),
      Locations = locations.joinOrNull(),
      ScanInformation = scanInformation.nullIfBlank(),
      StoryArc = storyArc.joinOrNull(),
      StoryArcNumber = storyArcNumber.joinOrNull(),
      SeriesGroup = seriesGroup.joinOrNull(),
      AgeRating = ageRating?.toXml(),
      Pages = pageContainer,
      CommunityRating = communityRating?.toRatingXml(),
      MainCharacterOrTeam = mainCharacterOrTeam.nullIfBlank(),
      Review = review.nullIfBlank(),
      GTIN = gtin.nullIfBlank(),
  )
}

internal fun ComicInfoXml.toComicInfo(): ComicInfo {
  val year = Year
  val month = Month
  val day = Day
  val date =
      if (year != null && month != null && day != null) {
        runCatching { LocalDate(year, month, day) }.getOrNull()
      } else null

  return ComicInfo(
      title = Title.nullIfBlank(),
      series = Series.nullIfBlank(),
      number = Number.nullIfBlank(),
      count = Count?.takeIf { it > 0 },
      volume = Volume?.takeIf { it > 0 },
      alternateSeries = AlternateSeries.nullIfBlank(),
      alternateNumber = AlternateNumber.nullIfBlank(),
      alternateCount = AlternateCount?.takeIf { it > 0 },
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
      pageCount = PageCount,
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
      pages = Pages?.Page?.map { it.toModel() } ?: emptyList(),
      communityRating = CommunityRating?.value,
      mainCharacterOrTeam = MainCharacterOrTeam.nullIfBlank(),
      review = Review.nullIfBlank(),
      gtin = GTIN.nullIfBlank(),
  )
}

private fun ComicPage.toXml(): ComicPageInfoXml =
    ComicPageInfoXml(
        Image = image,
        Type = type?.toXml(),
        DoublePage = doublePage,
        ImageSize = imageSize,
        Key = key.nullIfBlank(),
        Bookmark = bookmark.nullIfBlank(),
        ImageWidth = imageWidth,
        ImageHeight = imageHeight,
    )

private fun ComicPageInfoXml.toModel(): ComicPage =
    ComicPage(
        image = Image,
        type = Type?.toModel(),
        doublePage = DoublePage,
        imageSize = ImageSize,
        key = Key.nullIfBlank(),
        bookmark = Bookmark.nullIfBlank(),
        imageWidth = ImageWidth,
        imageHeight = ImageHeight,
    )

private val yesNoMapper =
    BiDirectionalEnumMapper(
        mapOf(YesNo.YES to YesNoXml.Yes, YesNo.NO to YesNoXml.No),
        defaultXml = YesNoXml.Unknown,
    )

private fun YesNo?.toXml(): YesNoXml = yesNoMapper.toXml(this)

private fun YesNoXml?.toModel(): YesNo? = yesNoMapper.toModel(this)

private val mangaMapper =
    BiDirectionalEnumMapper(
        mapOf(
            Manga.YES to MangaXml.Yes,
            Manga.NO to MangaXml.No,
            Manga.YES_AND_RIGHT_TO_LEFT to MangaXml.YesAndRightToLeft,
        ),
        defaultXml = MangaXml.Unknown,
    )

private fun Manga?.toXml(): MangaXml = mangaMapper.toXml(this)

private fun MangaXml?.toModel(): Manga? = mangaMapper.toModel(this)

private val ageRatingMapper =
    BiDirectionalEnumMapper(
        mapOf(
            AgeRating.ADULTS_ONLY_18_PLUS to AgeRatingXml.ADULTS_ONLY_18_PLUS,
            AgeRating.EARLY_CHILDHOOD to AgeRatingXml.EARLY_CHILDHOOD,
            AgeRating.EVERYONE to AgeRatingXml.Everyone,
            AgeRating.EVERYONE_10_PLUS to AgeRatingXml.EVERYONE_10_PLUS,
            AgeRating.G to AgeRatingXml.G,
            AgeRating.KIDS_TO_ADULTS to AgeRatingXml.KIDS_TO_ADULTS,
            AgeRating.M to AgeRatingXml.M,
            AgeRating.MA15_PLUS to AgeRatingXml.MA15_PLUS,
            AgeRating.MATURE_17_PLUS to AgeRatingXml.MATURE_17_PLUS,
            AgeRating.PG to AgeRatingXml.PG,
            AgeRating.R18_PLUS to AgeRatingXml.R18_PLUS,
            AgeRating.RATING_PENDING to AgeRatingXml.RATING_PENDING,
            AgeRating.TEEN to AgeRatingXml.Teen,
            AgeRating.X18_PLUS to AgeRatingXml.X18_PLUS,
        ),
        defaultXml = AgeRatingXml.Unknown,
    )

private fun AgeRating?.toXml(): AgeRatingXml = ageRatingMapper.toXml(this)

private fun AgeRatingXml?.toModel(): AgeRating? = ageRatingMapper.toModel(this)

private val comicPageTypeMapper =
    BiDirectionalEnumMapper(
        mapOf(
            ComicPageType.FRONT_COVER to ComicPageTypeXml.FrontCover,
            ComicPageType.INNER_COVER to ComicPageTypeXml.InnerCover,
            ComicPageType.ROUNDUP to ComicPageTypeXml.Roundup,
            ComicPageType.STORY to ComicPageTypeXml.Story,
            ComicPageType.ADVERTISEMENT to ComicPageTypeXml.Advertisement,
            ComicPageType.EDITORIAL to ComicPageTypeXml.Editorial,
            ComicPageType.LETTERS to ComicPageTypeXml.Letters,
            ComicPageType.PREVIEW to ComicPageTypeXml.Preview,
            ComicPageType.BACK_COVER to ComicPageTypeXml.BackCover,
            ComicPageType.OTHER to ComicPageTypeXml.Other,
            ComicPageType.DELETED to ComicPageTypeXml.Deleted,
        ),
        defaultXml = ComicPageTypeXml.Story,
    )

private fun ComicPageType?.toXml(): ComicPageTypeXml = comicPageTypeMapper.toXml(this)

private fun ComicPageTypeXml.toModel(): ComicPageType =
    comicPageTypeMapper.toModel(this) ?: ComicPageType.STORY

private fun BigDecimal.toRatingXml(): RatingXml = RatingXml(this.setScale(1, RoundingMode.HALF_UP))
