package models.errors

import play.api.libs.json.Json

case class ErrorMsg(msg: String, devMsg: DevMsg) {


  implicit val DevMsgF = Json.writes[DevMsg]

  implicit val ErrorMsgF = Json.writes[ErrorMsg]

  def toJson = Json.toJson(this)
}


case class DevMsg(error_code: Int,
                  msg: String,
                  class_name: String,
                  api_url: String = "http://sengab-platform.github.io/API-docs/#errors") {
}



