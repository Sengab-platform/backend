package actors.user

import actors.AbstractDBActor.Terminate
import actors.AbstractDBHandler
import actors.AbstractDBHandler.QueryResult
import akka.actor.{ActorRef, Props}
import com.couchbase.client.java.document.json.JsonObject
import messages.UserManagerMessages.ListUserActivity
import models.errors.GeneralErrors.{CouldNotParseJSON, NotFoundError}
import models.{Activities, Response}
import play.Logger
import play.api.libs.json._

class ActivityRetriever(out: ActorRef) extends AbstractDBHandler(out) {

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
  override def onNext(): (JsonObject) => Unit = {
    doc: JsonObject => {
      self ! QueryResult(doc)
    }
  }


  override def receive = {
    case ListUserActivity(userID, offset, limit) =>
      Logger.info(s"actor ${self.path} - received msg : ${ListUserActivity(userID, offset, limit)} ")

      // Here we will send the result
      executeQuery(DBUtilities.Activity.getActivityWithId("activity::" + userID, offset, limit))

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
            self ! CouldNotParseJSON("failed to get user activity",
              "couldn't parse json retrieved from the db ", this.getClass.toString)

        }
      } else {
        out ! NotFoundError("no such activity", "received null content document from DB", this.getClass.toString)
      }
  }

  /**
    * convert Json Document got from DB to a proper Response
    */

  // TODO add project URL

  def constructResponse(jsonObject: JsonObject): Option[Response] = {
    try {
      val parsedJson = Json.parse(jsonObject.toString)

      val activities = (parsedJson \ "activities").as[Seq[Activities]]
      Some(Response(Json.toJson(activities)))
    }
    catch {
      case e: Exception => None
    }
  }


}

object ActivityRetriever {
  def props(out: ActorRef): Props = Props(new ActivityRetriever(out))
}
