package io.github.broot5.komicinfo.xml

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName
import nl.adaptivity.xmlutil.serialization.XmlValue
import java.math.BigDecimal
import java.math.RoundingMode

@Suppress("PropertyName")
@Serializable
@XmlSerialName("ComicInfo")
data class ComicInfoXml(
    val Title: String = "",
    val Series: String = "",
    val Number: String = "",
    val Count: Int = -1,
    val Volume: Int = -1,
    val AlternateSeries: String = "",
    val AlternateNumber: String = "",
    val AlternateCount: Int = -1,
    val Summary: String = "",
    val Notes: String = "",
    val Year: Int = -1,
    val Month: Int = -1,
    val Day: Int = -1,
    val Writer: String = "",
    val Penciller: String = "",
    val Inker: String = "",
    val Colorist: String = "",
    val Letterer: String = "",
    val CoverArtist: String = "",
    val Editor: String = "",
    val Translator: String = "",
    val Publisher: String = "",
    val Imprint: String = "",
    val Genre: String = "",
    val Tags: String = "",
    val Web: String = "",
    val PageCount: Int = 0,
    val LanguageISO: String = "",
    val Format: String = "",
    val BlackAndWhite: YesNoXml = YesNoXml.Unknown,
    val Manga: MangaXml = MangaXml.Unknown,
    val Characters: String = "",
    val Teams: String = "",
    val Locations: String = "",
    val ScanInformation: String = "",
    val StoryArc: String = "",
    val StoryArcNumber: String = "",
    val SeriesGroup: String = "",
    val AgeRating: AgeRatingXml = AgeRatingXml.Unknown,
    @XmlSerialName("Pages") val Pages: ArrayOfComicPageInfoXml = ArrayOfComicPageInfoXml(),
    val CommunityRating: RatingXml? = null,
    val MainCharacterOrTeam: String = "",
    val Review: String = "",
    val GTIN: String = "",
)

@Serializable
enum class YesNoXml {
  Unknown,
  No,
  Yes,
}

@Serializable
enum class MangaXml {
  Unknown,
  No,
  Yes,
  YesAndRightToLeft,
}

@Serializable
data class RatingXml(
    @XmlValue @Serializable(with = BigDecimalAsStringSerializer::class) val value: BigDecimal
) {
  init {
    require(value >= BigDecimal.ZERO && value <= BigDecimal("5.0")) {
      "Rating must be between 0.0 and 5.0"
    }
    require(value.scale() <= 1) { "Rating must have at most 1 decimal place" }
  }

  object BigDecimalAsStringSerializer : KSerializer<BigDecimal> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("BigDecimal", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: BigDecimal) {
      val roundedValue = value.setScale(1, RoundingMode.HALF_UP)
      encoder.encodeString(roundedValue.toString())
    }

    override fun deserialize(decoder: Decoder): BigDecimal {
      val stringValue = decoder.decodeString()
      return BigDecimal(stringValue).setScale(1, RoundingMode.HALF_UP)
    }
  }
}

@Suppress("EnumEntryName")
@Serializable
enum class AgeRatingXml {
  Unknown,
  `Adults Only 18+`,
  `Early Childhood`,
  Everyone,
  `Everyone 10+`,
  G,
  `Kids to Adults`,
  M,
  `MA15+`,
  `Mature 17+`,
  PG,
  `R18+`,
  `Rating Pending`,
  Teen,
  `X18+`,
}

@Serializable
@XmlSerialName("ArrayOfComicPageInfo")
data class ArrayOfComicPageInfoXml(
    @Suppress("PropertyName")
    @XmlElement(true)
    @XmlSerialName("Page")
    val Page: List<ComicPageInfoXml> = emptyList()
)

@Suppress("PropertyName")
@Serializable
data class ComicPageInfoXml(
    @SerialName("Image") val Image: Int,
    @SerialName("Type") val Type: ComicPageTypeXml = ComicPageTypeXml.Story,
    @SerialName("DoublePage") val DoublePage: Boolean = false,
    @SerialName("ImageSize") val ImageSize: Long = 0,
    @SerialName("Key") val Key: String = "",
    @SerialName("Bookmark") val Bookmark: String = "",
    @SerialName("ImageWidth") val ImageWidth: Int = -1,
    @SerialName("ImageHeight") val ImageHeight: Int = -1,
)

@Serializable
enum class ComicPageTypeXml {
  FrontCover,
  InnerCover,
  Roundup,
  Story,
  Advertisement,
  Editorial,
  Letters,
  Preview,
  BackCover,
  Other,
  Deleted,
}
