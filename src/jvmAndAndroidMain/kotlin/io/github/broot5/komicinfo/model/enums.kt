package io.github.broot5.komicinfo.model

public enum class YesNo(public val value: String) {
  UNKNOWN("Unknown"),
  NO("No"),
  YES("Yes");

  public companion object {
    public fun fromValue(value: String?): YesNo {
      return entries.find { it.value.equals(value, ignoreCase = true) } ?: UNKNOWN
    }
  }
}

public enum class Manga(public val value: String) {
  UNKNOWN("Unknown"),
  NO("No"),
  YES("Yes"),
  YES_AND_RIGHT_TO_LEFT("YesAndRightToLeft");

  public companion object {
    public fun fromValue(value: String?): Manga {
      return entries.find { it.value.equals(value, ignoreCase = true) } ?: UNKNOWN
    }
  }
}

public enum class AgeRating(public val value: String) {
  UNKNOWN("Unknown"),
  ADULTS_ONLY_18_PLUS("Adults Only 18+"),
  EARLY_CHILDHOOD("Early Childhood"),
  EVERYONE("Everyone"),
  EVERYONE_10_PLUS("Everyone 10+"),
  G("G"),
  KIDS_TO_ADULTS("Kids to Adults"),
  M("M"),
  MA15_PLUS("MA15+"),
  MATURE_17_PLUS("Mature 17+"),
  PG("PG"),
  R18_PLUS("R18+"),
  RATING_PENDING("Rating Pending"),
  TEEN("Teen"),
  X18_PLUS("X18+");

  public companion object {
    public fun fromValue(value: String?): AgeRating {
      return entries.find { it.value.equals(value, ignoreCase = true) } ?: UNKNOWN
    }
  }
}

public enum class ComicPageType(public val value: String) {
  FRONT_COVER("FrontCover"),
  INNER_COVER("InnerCover"),
  ROUNDUP("Roundup"),
  STORY("Story"),
  ADVERTISEMENT("Advertisement"),
  EDITORIAL("Editorial"),
  LETTERS("Letters"),
  PREVIEW("Preview"),
  BACK_COVER("BackCover"),
  OTHER("Other"),
  DELETED("Deleted");

  public companion object {
    public fun fromValue(value: String?): ComicPageType {
      return entries.find { it.value.equals(value, ignoreCase = true) } ?: STORY
    }
  }
}
