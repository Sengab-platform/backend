package models.contribution

import models.contribution.ContributionDataTypes.{ContributionDataTypeFour, ContributionDataTypeOne, ContributionDataTypeThree, ContributionDataTypeTwo}
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsValue, Json, Reads, Writes}

trait ContributionData

object ContributionData {

  implicit val tempBodyR: Reads[ContributionData] = Json.format[ContributionDataTypeOne].map(x => x: ContributionData) or
    Json.format[ContributionDataTypeThree].map(x => x: ContributionData) or
    Json.format[ContributionDataTypeFour].map(x => x: ContributionData) or
    Json.format[ContributionDataTypeTwo].map(x => x: ContributionData)


  implicit val tempBodyW = new Writes[ContributionData] {
    def writes(c: ContributionData): JsValue = {
      c match {
        case m: ContributionDataTypeOne => Json.toJson(m)
        case m: ContributionDataTypeTwo => Json.toJson(m)
        case m: ContributionDataTypeThree => Json.toJson(m)
        case m: ContributionDataTypeFour => Json.toJson(m)
        case _ => Json.obj("error" -> "wrong Json")
      }
    }
  }
}