package models.errors

import play.api.mvc.Results

object GeneralErrors {

  case class AskTimeoutError(msg: String, devMsg: String, class_name: String) extends
    Error(
      Results.RequestTimeout(
        ErrorMsg(
          msg, DevMsg(
            ErrorsCodes.GENERAL_SERVER_ERROR_CODE, devMsg, class_name)
        ).toJson))

  case class BadJSONError(msg: String, devMsg: String, class_name: String) extends
    Error(
      Results.BadRequest(
        ErrorMsg(
          msg, DevMsg(
            ErrorsCodes.GENERAL_SERVER_ERROR_CODE, devMsg, class_name)
        ).toJson))

  case class NotFoundError(msg: String, devMsg: String, class_name: String) extends
    Error(
      Results.NotFound(
        ErrorMsg(
          msg, DevMsg(
            ErrorsCodes.GENERAL_SERVER_ERROR_CODE, devMsg, class_name)
        ).toJson))

  case class CouldNotParseJSON(msg: String, devMsg: String, class_name: String) extends
    Error(
      Results.InternalServerError(
        ErrorMsg(
          msg, DevMsg(
            ErrorsCodes.GENERAL_SERVER_ERROR_CODE, devMsg, class_name)
        ).toJson))


  case class FORBIDDEN(msg: String, devMsg: String, class_name: String) extends
    Error(
      Results.Forbidden(
        ErrorMsg(
          msg, DevMsg(
            ErrorsCodes.GENERAL_SERVER_ERROR_CODE, devMsg, class_name)
        ).toJson))

}
