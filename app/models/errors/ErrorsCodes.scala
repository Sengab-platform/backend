package models.errors

object ErrorsCodes {
  // these codes are sent in Error responses
  // for more info check our doc here : http://sengab-platform.github.io/API-docs/#errors

  val BUCKET_CLOSED_ERROR_CODE = 4018
  val COUCHBASE_ERROR_CODE = 4019
  val GENERAL_SERVER_ERROR_CODE = 4020
  val ASK_TIMEOUT_ERROR_CODE = 4020
}
