package models.responses

import play.api.mvc.Result

case class Error(result: Result)

case class ErrorMsg(msg: String, devMsg: String)
