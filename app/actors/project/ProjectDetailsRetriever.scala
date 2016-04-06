package actors.project

import actors.AbstractDBActor.Terminate
import actors.AbstractDBHandler
import actors.AbstractDBHandler.QueryResult
import akka.actor.{ActorRef, Props}
import com.couchbase.client.java.document.json.JsonObject
import helpers.Helper._
import messages.ProjectManagerMessages.GetProjectDetails
import models.Response
import models.errors.Error
import models.errors.GeneralErrors.{CouldNotParseJSON, NotFoundError}
import models.project.Project.DetailedProject
import play.api.Logger
import play.api.libs.json._


class ProjectDetailsRetriever(out: ActorRef) extends AbstractDBHandler(out) {

  // this msg would sent to user when error happens while querying from db
  override val ErrorMsg: String = "failed to get project details"


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
          case Some(Response(jsonResult)) =>
            out ! Response(jsonResult)

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
      val modifiedJson = addField(parsedJson, "url", helpers.Helper.ProjectPath + (parsedJson \ "id").as[String])

      // add owner url to the json retrieved
      val jsonTransformer =
        addTransformer(__ \ 'owner, "url", helpers.Helper.UserPath +
          (parsedJson \ "owner" \ "id").as[String])

          // add category url to the json retrieved
          .compose(addTransformer(__ \ 'category, "url", helpers.Helper.CategoryPath +
          (parsedJson \ "category" \ "category_id").as[String]))

      val fullResponse = modifiedJson
        .transform(jsonTransformer).get

      val project = fullResponse.as[DetailedProject]

      Some(Response(Json.toJson(project)))

    } catch {
      case e: Exception => None
    }
  }
}


object ProjectDetailsRetriever {
  def props(out: ActorRef): Props = Props(new ProjectDetailsRetriever(out))
}