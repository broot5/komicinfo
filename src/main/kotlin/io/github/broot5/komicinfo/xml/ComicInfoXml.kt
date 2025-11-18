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
    @XmlElement(true) val Title: String = "",
    @XmlElement(true) val Series: String = "",
    @XmlElement(true) val Number: String = "",
    @XmlElement(true) val Count: Int = -1,
    @XmlElement(true) val Volume: Int = -1,
    @XmlElement(true) val AlternateSeries: String = "",
    @XmlElement(true) val AlternateNumber: String = "",
    @XmlElement(true) val AlternateCount: Int = -1,
    @XmlElement(true) val Summary: String = "",
    @XmlElement(true) val Notes: String = "",
    @XmlElement(true) val Year: Int = -1,
    @XmlElement(true) val Month: Int = -1,
    @XmlElement(true) val Day: Int = -1,
    @XmlElement(true) val Writer: String = "",
    @XmlElement(true) val Penciller: String = "",
    @XmlElement(true) val Inker: String = "",
    @XmlElement(true) val Colorist: String = "",
    @XmlElement(true) val Letterer: String = "",
    @XmlElement(true) val CoverArtist: String = "",
    @XmlElement(true) val Editor: String = "",
    @XmlElement(true) val Translator: String = "",
    @XmlElement(true) val Publisher: String = "",
    @XmlElement(true) val Imprint: String = "",
    @XmlElement(true) val Genre: String = "",
    @XmlElement(true) val Tags: String = "",
    @XmlElement(true) val Web: String = "",
    @XmlElement(true) val PageCount: Int = 0,
    @XmlElement(true) val LanguageISO: String = "",
    @XmlElement(true) val Format: String = "",
    @XmlElement(true)
    @XmlSerialName("BlackAndWhite")
    val BlackAndWhite: YesNoXml = YesNoXml.Unknown,
    @XmlElement(true) @XmlSerialName("Manga") val Manga: MangaXml = MangaXml.Unknown,
    @XmlElement(true) val Characters: String = "",
    @XmlElement(true) val Teams: String = "",
    @XmlElement(true) val Locations: String = "",
    @XmlElement(true) val ScanInformation: String = "",
    @XmlElement(true) val StoryArc: String = "",
    @XmlElement(true) val StoryArcNumber: String = "",
    @XmlElement(true) val SeriesGroup: String = "",
    @XmlElement(true)
    @XmlSerialName("AgeRating")
    val AgeRating: AgeRatingXml = AgeRatingXml.Unknown,
    @XmlElement(true)
    @XmlSerialName("Pages")
    val Pages: ArrayOfComicPageInfoXml = ArrayOfComicPageInfoXml(),
    @XmlElement(true) @XmlSerialName("CommunityRating") val CommunityRating: RatingXml? = null,
    @XmlElement(true) val MainCharacterOrTeam: String = "",
    @XmlElement(true) val Review: String = "",
    @XmlElement(true) val GTIN: String = "",
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

@Serializable
enum class AgeRatingXml {
  Unknown,
  @SerialName("Adults Only 18+") ADULTS_ONLY_18_PLUS,
  @SerialName("Early Childhood") EARLY_CHILDHOOD,
  Everyone,
  @SerialName("Everyone 10+") EVERYONE_10_PLUS,
  G,
  @SerialName("Kids to Adults") KIDS_TO_ADULTS,
  M,
  @SerialName("MA15+") MA15_PLUS,
  @SerialName("Mature 17+") MATURE_17_PLUS,
  PG,
  @SerialName("R18+") R18_PLUS,
  @SerialName("Rating Pending") RATING_PENDING,
  Teen,
  @SerialName("X18+") X18_PLUS,
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
    @XmlElement(false) @XmlSerialName("Type") val Type: ComicPageTypeXml = ComicPageTypeXml.Story,
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
