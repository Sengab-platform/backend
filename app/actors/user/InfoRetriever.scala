package actors.user

import actors.AbstractDBHandlerActor
import actors.AbstractDBHandlerActor.{QueryResult, Terminate}
import akka.actor.{ActorRef, Props}
import com.couchbase.client.java.document.JsonDocument
import helpers.Helper
import messages.UserManagerMessages.GetUserProfile
import models.errors.GeneralErrors.{CouldNotParseJSON, NotFoundError}
import models.{Response, UserInfo}
import play.Logger
import play.api.libs.json.{JsObject, JsString, Json}

class InfoRetriever(out: ActorRef) extends AbstractDBHandlerActor(out) {

  override val ErrorMsg: String = "Retrieving user info failed"

  override def onComplete: () => Unit = {
    () => {
      self ! Terminate
    }
  }

  override def onNext(): (JsonDocument) => Unit = {
    doc: JsonDocument => {
      self ! QueryResult(doc)
    }
  }

  override def receive = {

    case GetUserProfile(userID) =>
      Logger.info(s"actor ${self.path} - received msg : ${GetUserProfile(userID)} ")
      executeQuery(DBUtilities.User.getUserWithId(userID))

    case Terminate =>
      Logger.info(s"actor ${self.path} - received msg : $Terminate ")
      context stop self

    case QueryResult(doc) =>
      Logger.info(s"actor ${self.path} - received msg : ${QueryResult(doc)} ")

      if (doc.content() != null) {
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

  override def constructResponse(doc: JsonDocument): Option[Response] = {
    try {
      val parsedJson = Json.parse(doc.content().toString).as[JsObject]
      val fullResponse = parsedJson + ("id" -> JsString(doc.id)) + ("url" -> JsString(Helper.USER_PATH + doc.id))
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
