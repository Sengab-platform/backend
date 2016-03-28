package models.responses

import play.api.libs.json.Json
import play.api.mvc.Result

case class Error(result: Result)


case class ErrorMsg(msg: String, devMsg: String) {

  implicit val ErrorMsgF = Json.writes[ErrorMsg]

  def toJson = Json.toJson(this)
}