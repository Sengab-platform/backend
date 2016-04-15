package actors.project

import actors.AbstractDBActor.Terminate
import actors.AbstractDBHandler
import actors.AbstractDBHandler.QueryResult
import akka.actor.{ActorRef, Props}
import com.couchbase.client.java.document.json.JsonObject
import helpers.Helper
import messages.ProjectManagerMessages.GetProjectStats
import models.errors.GeneralErrors.{CouldNotParseJSON, NotFoundError}
import models.{Response, Stats}
import play.Logger
import play.api.libs.json.Json

class ProjectStatsRetriever(out: ActorRef) extends AbstractDBHandler(out) {
  override val ErrorMsg: String = "Retrieving project stats failed"

  override def receive = {

    case GetProjectStats(projectID) =>
      Logger.info(s"actor ${self.path} - received msg : ${GetProjectStats(projectID)} ")
      executeQuery(DBUtilities.Stats.getStatsWithId(Helper.StatsIDPrefix + projectID))

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
            self ! CouldNotParseJSON("failed to get project stats",
              "couldn't parse json retrieved from the db ", this.getClass.toString)

        }
      } else {
        out ! NotFoundError("no such project stats", "received null content document from DB", this.getClass.toString)
      }

  }

  /**
    * convert Json Object got from DB to a proper Response
    */
  override def constructResponse(jsonObject: JsonObject): Option[Response] = {
    try {
      val stats = Json.parse(jsonObject.toString).as[Stats]
      Some(Response(Json.toJson(stats)))
    } catch {
      case e: Exception =>
        Logger.info(e.getMessage)
        None
    }
  }
}

object ProjectStatsRetriever {
  def props(out: ActorRef): Props = Props(new ProjectStatsRetriever(out))
}
