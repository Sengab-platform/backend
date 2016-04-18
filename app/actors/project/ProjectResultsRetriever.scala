package actors.project

import actors.AbstractDBActor.Terminate
import actors.AbstractDBHandler
import actors.AbstractDBHandler.QueryResult
import akka.actor.{ActorRef, Props}
import com.couchbase.client.java.document.json.JsonObject
import helpers.Helper
import messages.ProjectManagerMessages.GetProjectResults
import models.Response
import models.errors.Error
import models.errors.GeneralErrors.{CouldNotParseJSON, NotFoundError}
import models.results.ProjectResult
import play.api.Logger
import play.api.libs.json._

class ProjectResultsRetriever(out: ActorRef) extends AbstractDBHandler(out) {
  override val ErrorMsg: String = "Failed to retrieve project results"

  override def receive: Receive = {
    case GetProjectResults(projectID, offset, limit) =>
      Logger.info(s"actor ${self.path} - received msg : ${GetProjectResults(projectID, offset, limit)} ")

      executeQuery(DBUtilities.Result.getResultWithId(Helper.ResultIDPrefix + projectID, offset, limit))

    case Terminate =>
      Logger.info(s"actor ${self.path} - received msg : $Terminate ")
      context.stop(self)

    case err: Error =>
      Logger.info(s"actor ${self.path} - received msg : $err")
      out ! err

    case QueryResult(jsonObj) =>
      Logger.info(s"actor ${self.path} - received msg : ${QueryResult(jsonObj)} ")

      if (jsonObj.get("id") != DBUtilities.DBConfig.EMPTY_JSON_OBJECT) {
        val response = constructResponse(jsonObj)
        response match {
          case Some(Response(jsonResult)) =>
            out ! Response(jsonResult)

          case None =>
            out ! CouldNotParseJSON("failed to get project details with results",
              "couldn't parse json retrieved from the db ", this.getClass.toString)

        }
      } else {
        out ! NotFoundError("no such project", "received empty Json Object from DB", this.getClass.toString)
      }


  }

  /**
    * convert Json Object got from DB to a proper Response
    */
  override def constructResponse(jsonObject: JsonObject): Option[Response] =
    try {
      val results = Json.parse(jsonObject.toString).as[ProjectResult]
      Some(Response(Json.toJson(results)))

    } catch {
      case e: Exception => None
    }
}

object ProjectResultsRetriever {
  def props(out: ActorRef): Props = Props(new ProjectResultsRetriever(out))
}
