package actors.user

import actors.AbstractDBHandlerActor
import actors.AbstractDBHandlerActor.{QueryResult, Terminate}
import akka.actor.{ActorRef, Props}
import com.couchbase.client.java.document.json.JsonObject
import messages.UserManagerMessages.ListUserActivity
import models.Response
import models.errors.GeneralErrors.{CouldNotParseJSON, NotFoundError}
import play.Logger
import play.api.libs.json._

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
  override def onNext(): (JsonObject) => Unit = {
    doc: JsonObject => {
      self ! QueryResult(doc)
    }
  }


  override def receive = {
    case ListUserActivity(userID, offset, limit) =>
      Logger.info(s"actor ${self.path} - received msg : ${ListUserActivity(userID, offset, limit)} ")

      // Here we will send the result
      executeQuery(DBUtilities.Activity.getActivityWithId("activity::" + userID, 6, 0))

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

  // TODO reimplement this method

  def constructResponse(jsonObj: JsonObject): Option[Response] = {
    Logger.info("OBJECT: " + jsonObj.get("id"))

    //  try {
    val parsedJson = Json.parse(jsonObj.toString).as[JsObject]
    val modifiedParsedJson = parsedJson +
      ("id" -> JsString(jsonObj.getString("id"))) +
      ("entity_type" -> JsString("activity"))

    val jsonTransformer = (__ \ 'activities \ 'project).json.update(
      __.read[JsObject].map { o => o ++ Json.obj("project_url" ->
        JsString("AAA"))
      }
    )

    //    val jsonTransformer = (__ \ 'key25 \ 'key251).
    //      json.copyFrom( (__ \ 'key2 \ 'key21).json.pick )


    val fullResponse = modifiedParsedJson.transform(jsonTransformer).get
    //      val jsonArray: JsArray = (parsedJson \ "activities").as[JsArray]

    //val activities = fullResponse.as[Activities]

    Some(Response(Json.toJson(fullResponse)))

    //    } catch {
    //      case e: Exception =>
    //        Logger.info(e.getMessage)
    //        None
    //    }
  }


}

object ActivityRetriever {
  def props(out: ActorRef): Props = Props(new ActivityRetriever(out))
}
