package models.contribution

import play.api.libs.json.Json

object ContributionDataTypes {

  case class ContributionDataTypeOne(location: Location, answer: String) extends ContributionData

  case class ContributionDataTypeTwo(image: String, caption: String) extends ContributionData

  case class ContributionDataTypeThree(answers: Seq[Answer]) extends ContributionData

  case class ContributionDataTypeFour(image: String, caption: String, location: Location) extends ContributionData

  case class Location(lat: Double, lng: Double)

  case class Answer(id: Int, ans: String)

  object Location {
    implicit val formatter = Json.format[Location]
  }

  object Answer {
    implicit val formatter = Json.format[Answer]
  }

  object ContributionDataTypeOne {
    implicit val formatter = Json.format[ContributionDataTypeOne]
  }

  object ContributionDataTypeTwo {
    implicit val formatter = Json.format[ContributionDataTypeTwo]
  }

  object ContributionDataTypeThree {
    implicit val formatter = Json.format[ContributionDataTypeThree]
  }

  object ContributionDataTypeFour {
    implicit val formatter = Json.format[ContributionDataTypeFour]
  }

}
