package actors.project

import actors.AbstractDBHandlerActor
import actors.AbstractDBHandlerActor.{QueryResult, Terminate}
import akka.actor.{ActorRef, Props}
import com.couchbase.client.java.document.json.JsonObject
import messages.ProjectManagerMessages.GetProjectDetails
import models.Response
import models.errors.Error
import models.errors.GeneralErrors.{CouldNotParseJSON, NotFoundError}
import models.project.Project.DetailedProject
import play.api.Logger
import play.api.libs.json._


class ProjectDetailsRetriever(out: ActorRef) extends AbstractDBHandlerActor(out) {

  // this msg would sent to user when error happens while querying from db
  override val ErrorMsg: String = "failed to get project details"


  override def onComplete: () => Unit = { () => {
    self ! Terminate
  }
  }

  override def onNext(): (JsonObject) => Unit = { doc: JsonObject => {
    self ! QueryResult(doc)
  }
  }

  override def receive: Receive = {
    case GetProjectDetails(projectID) =>
      Logger.info(s"actor ${self.path} - received msg : ${GetProjectDetails(projectID)} ")

      executeQuery(DBUtilities.Project.getProjectWithId(projectID))

    case Terminate =>
      Logger.info(s"actor ${self.path} - received msg : $Terminate ")
      context.stop(self)

    case err: Error =>
      Logger.info(s"actor ${self.path} - received msg : $err")
      out ! err

    case QueryResult(jsonObj) =>
      Logger.info(s"actor ${self.path} - received msg : ${QueryResult(jsonObj)} ")

      if (jsonObj.get("id") != DBUtilities.DBConfig.EMPTY_JSON_DOC) {
        val response = constructResponse(jsonObj)
        response match {
          case Some(response) =>
            out ! response

          // TODO self or out? hmm.
          case None =>
            out ! CouldNotParseJSON("failed to get project details",
              "couldn't parse json retrieved from the db ", this.getClass.toString)

        }
      } else {
        out ! NotFoundError("no such project", "received empty Json Object from DB", this.getClass.toString)
      }


  }

  override def constructResponse(jsonObj: JsonObject): Option[Response] = {

    try {
      val parsedJson = Json.parse(jsonObj.toString).as[JsObject]

      // add project url to the json retrieved
      val modifiedJson = parsedJson + ("url" -> JsString(helpers.Helper.PROJECT_PATH + jsonObj.get("id")))

      // add owner url to the json retrieved
      val jsonTransformer = (__ \ 'owner).json.update(
        __.read[JsObject].map { o => o ++ Json.obj("url" ->
          JsString(helpers.Helper.USER_PATH + jsonObj.getObject("owner").get("id")))
        }
      )
      // add category url to the json retrieved
      val jsonTransformer_2 = (__ \ 'category).json.update(
        __.read[JsObject].map { o => o ++ Json.obj("url" ->
          JsString(helpers.Helper.CATEGORY_PATH + jsonObj.getObject("category").get("category_id")))
        }
      )

      val fullResponse = modifiedJson
        .transform(jsonTransformer).get
        .transform(jsonTransformer_2).get

      // TODO
      //      val project = fullResponse.as[DetailedProject]

      Some(Response(Json.toJson(fullResponse)))

    } catch {
      case e: Exception => None
    }
  }
}


object ProjectDetailsRetriever {
  def props(out: ActorRef): Props = Props(new ProjectDetailsRetriever(out))
}