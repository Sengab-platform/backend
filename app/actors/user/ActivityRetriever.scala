package actors.user

import actors.AbstractDBHandlerActor
import actors.AbstractDBHandlerActor.{QueryResult, Terminate}
import akka.actor.{ActorRef, Props}
import com.couchbase.client.java.document.JsonDocument
import messages.UserManagerMessages.ListUserActivity
import models.errors.GeneralErrors.CouldNotParseJSON
import models.responses.Response
import play.Logger
import play.api.libs.json.{JsArray, JsValue, Json}

class ActivityRetriever(out: ActorRef) extends AbstractDBHandlerActor(out) {

  override val ErrorMsg: String = "Retrieving user activity failed"

  /**
    * called when the db query completes and all Json Documents retrieved
    */
  override def onComplete: () => Unit = {
    () => {
      self ! Terminate
    }
  }

  /**
    * called when the db query get data back as JsonDocument
    */
  override def onNext(): (JsonDocument) => Unit = {
    doc: JsonDocument => {
      self ! QueryResult(doc)
    }
  }

  override def receive = {
    case ListUserActivity(userID, offset, limit) =>
      Logger.info(s"actor ${self.path} - received msg : ${ListUserActivity(userID, offset, limit)} ")

      // Here we will send the result
      executeQuery(DBUtilities.Activity.getActivityWithId("activity::" + userID))

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
            self ! CouldNotParseJSON("failed to get user activity",
              "couldn't parse json retrieved from the db ", this.getClass.toString)

        }
      }
  }

  /**
    * convert Json Document got from DB to a proper Response
    */
  override def constructResponse(doc: JsonDocument): Option[Response] = {

    try {
      import models.responses.ActivityResults._
      val parsedJson: JsValue = Json.parse(doc.content().toString)
      val jsonArray: JsArray = (parsedJson \ "activities").as[JsArray]
      val activities = jsonArray.as[Seq[Activities]]

      Some(UserActivityResponse(activities))

    } catch {
      case e: Exception => None
    }
  }
}

object ActivityRetriever {
  def props(out: ActorRef): Props = Props(new ActivityRetriever(out))
}
