package io.github.broot5.komicinfo.xml

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import nl.adaptivity.xmlutil.ExperimentalXmlUtilApi
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlNamespaceDeclSpecs
import nl.adaptivity.xmlutil.serialization.XmlSerialName
import nl.adaptivity.xmlutil.serialization.XmlValue
import java.math.BigDecimal
import java.math.RoundingMode

@OptIn(ExperimentalXmlUtilApi::class)
@Suppress("PropertyName")
@Serializable
@XmlSerialName("ComicInfo")
@XmlNamespaceDeclSpecs(
    "xsd=http://www.w3.org/2001/XMLSchema",
    "xsi=http://www.w3.org/2001/XMLSchema-instance",
)
data class ComicInfoXml(
    @XmlElement(true) val Title: String? = null,
    @XmlElement(true) val Series: String? = null,
    @XmlElement(true) val Number: String? = null,
    @XmlElement(true) val Count: Int? = null,
    @XmlElement(true) val Volume: Int? = null,
    @XmlElement(true) val AlternateSeries: String? = null,
    @XmlElement(true) val AlternateNumber: String? = null,
    @XmlElement(true) val AlternateCount: Int? = null,
    @XmlElement(true) val Summary: String? = null,
    @XmlElement(true) val Notes: String? = null,
    @XmlElement(true) val Year: Int? = null,
    @XmlElement(true) val Month: Int? = null,
    @XmlElement(true) val Day: Int? = null,
    @XmlElement(true) val Writer: String? = null,
    @XmlElement(true) val Penciller: String? = null,
    @XmlElement(true) val Inker: String? = null,
    @XmlElement(true) val Colorist: String? = null,
    @XmlElement(true) val Letterer: String? = null,
    @XmlElement(true) val CoverArtist: String? = null,
    @XmlElement(true) val Editor: String? = null,
    @XmlElement(true) val Translator: String? = null,
    @XmlElement(true) val Publisher: String? = null,
    @XmlElement(true) val Imprint: String? = null,
    @XmlElement(true) val Genre: String? = null,
    @XmlElement(true) val Tags: String? = null,
    @XmlElement(true) val Web: String? = null,
    @XmlElement(true) val PageCount: Int = 0,
    @XmlElement(true) val LanguageISO: String? = null,
    @XmlElement(true) val Format: String? = null,
    @XmlElement(true) @XmlSerialName("BlackAndWhite") val BlackAndWhite: YesNoXml? = null,
    @XmlElement(true) @XmlSerialName("Manga") val Manga: MangaXml? = null,
    @XmlElement(true) val Characters: String? = null,
    @XmlElement(true) val Teams: String? = null,
    @XmlElement(true) val Locations: String? = null,
    @XmlElement(true) val ScanInformation: String? = null,
    @XmlElement(true) val StoryArc: String? = null,
    @XmlElement(true) val StoryArcNumber: String? = null,
    @XmlElement(true) val SeriesGroup: String? = null,
    @XmlElement(true) @XmlSerialName("AgeRating") val AgeRating: AgeRatingXml? = null,
    @XmlElement(true) @XmlSerialName("Pages") val Pages: ArrayOfComicPageInfoXml? = null,
    @XmlElement(true) @XmlSerialName("CommunityRating") val CommunityRating: RatingXml? = null,
    @XmlElement(true) val MainCharacterOrTeam: String? = null,
    @XmlElement(true) val Review: String? = null,
    @XmlElement(true) val GTIN: String? = null,
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
    @XmlElement(false) @XmlSerialName("Type") val Type: ComicPageTypeXml? = null,
    @SerialName("DoublePage") val DoublePage: Boolean? = null,
    @SerialName("ImageSize") val ImageSize: Long? = null,
    @SerialName("Key") val Key: String? = null,
    @SerialName("Bookmark") val Bookmark: String? = null,
    @SerialName("ImageWidth") val ImageWidth: Int? = null,
    @SerialName("ImageHeight") val ImageHeight: Int? = null,
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
