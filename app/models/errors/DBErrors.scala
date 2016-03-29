package models.errors

import play.api.mvc.Results

object DBErrors {

  case class BucketClosedError(msg: String, devMsg: String, class_name: String) extends
    Error(
      Results.ServiceUnavailable(
        ErrorMsg(
          msg, DevMsg(
            ErrorsCodes.BUCKET_CLOSED_ERROR_CODE, devMsg, class_name)
        ).toJson))


  case class CouchbaseError(msg: String, devMsg: String, class_name: String) extends
    Error(
      Results.ServiceUnavailable(
        ErrorMsg(
          msg, DevMsg(
            ErrorsCodes.COUCHBASE_ERROR_CODE, devMsg, class_name)
        ).toJson))


  case class GeneralServerError(msg: String, devMsg: String, class_name: String) extends
    Error(
      Results.InternalServerError(
        ErrorMsg(
          msg, DevMsg(
            ErrorsCodes.GENERAL_SERVER_ERROR_CODE, devMsg, class_name)
        ).toJson))


}