package actors.user

import actors.AbstractDBHandlerActor
import actors.AbstractDBHandlerActor.{QueryResult, Terminate}
import akka.actor.{ActorRef, Props}
import com.couchbase.client.java.document.JsonDocument
import messages.UserManagerMessages.GetUserProfile
import models.errors.GeneralErrors.CouldNotParseJSON
import models.responses.Response
import models.responses.UserResponses._
import play.Logger
import play.api.libs.json.{JsValue, Json}

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
      }
  }

  override def constructResponse(doc: JsonDocument): Option[Response] =
    try {
      val parsedJson: JsValue = Json.parse(doc.content().toString)
      val first_name = (parsedJson \ "first_name").asOpt[String]
      val last_name = (parsedJson \ "last_name").asOpt[String]
      val image = (parsedJson \ "image").asOpt[String]
      val about = (parsedJson \ "about").as[About]
      val stats = (parsedJson \ "stats").as[Stats]
      val url = s"api.sengab.com/v1/users/${doc.id()}"
      val projects = s"api.sengab.com/v1/users/${doc.id()}/projects"
      val contributions = s"api.sengab.com/v1/users/${doc.id()}/contributions"

      Some(UserInfoResponse(
        doc.id,
        url,
        first_name,
        last_name,
        image,
        about,
        stats,
        projects,
        contributions
      ))

    } catch {
      case e: Exception => None
    }
}

object InfoRetriever {
  def props(out: ActorRef): Props = Props(new InfoRetriever(out))
}
