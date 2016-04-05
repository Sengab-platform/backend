package actors.user

import actors.AbstractDBActor.Terminate
import actors.AbstractDBHandler
import actors.AbstractDBHandler.QueryResult
import akka.actor.{ActorRef, Props}
import com.couchbase.client.java.document.json.JsonObject
import helpers.Helper
import messages.UserManagerMessages.GetUserProfile
import models.errors.GeneralErrors.{CouldNotParseJSON, NotFoundError}
import models.{Response, UserInfo}
import play.Logger
import play.api.libs.json.{JsObject, JsString, Json}

class InfoRetriever(out: ActorRef) extends AbstractDBHandler(out) {

  override val ErrorMsg: String = "Retrieving user info failed"

  override def receive = {

    case GetUserProfile(userID) =>
      Logger.info(s"actor ${self.path} - received msg : ${GetUserProfile(userID)} ")
      executeQuery(DBUtilities.User.getUserWithId(userID))

    case Terminate =>
      Logger.info(s"actor ${self.path} - received msg : $Terminate ")
      context stop self

    case QueryResult(doc) =>
      Logger.info(s"actor ${self.path} - received msg : ${QueryResult(doc)} ")

      if (doc.getString("id") != DBUtilities.DBConfig.EMPTY_JSON_DOC) {
        val response = constructResponse(doc)
        response match {
          case Some(response) =>
            out ! response

          case None =>
            self ! CouldNotParseJSON("failed to get user info",
              "couldn't parse json retrieved from the db ", this.getClass.toString)

        }
      } else {
        out ! NotFoundError("no such user", "received null content document from DB", this.getClass.toString)
      }

  }

  override def constructResponse(jsonObject: JsonObject): Option[Response] = {
    try {
      val parsedJson = Json.parse(jsonObject.toString).as[JsObject]
      val id = jsonObject.getString("id")
      val fullResponse = parsedJson + ("id" -> JsString(id)) + ("url" -> JsString(Helper.UserPath + id))
      val user = Json.toJson(fullResponse.as[UserInfo])
      Some(Response(Json.toJson(user)))
    } catch {
      case e: Exception =>
        Logger.info(e.getMessage)
        None
    }

  }

}

object InfoRetriever {
  def props(out: ActorRef): Props = Props(new InfoRetriever(out))
}
