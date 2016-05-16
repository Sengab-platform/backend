package actors.project

import actors.AbstractBulkDBHandler
import actors.AbstractBulkDBHandler.{BulkResult, ItemResult}
import actors.AbstractDBActor.Terminate
import akka.actor.{ActorRef, Props}
import com.couchbase.client.java.document.json.JsonArray
import helpers.Helper._
import messages.ProjectManagerMessages.ListProjects
import models.Response
import models.errors.GeneralErrors.CouldNotParseJSON
import models.project.Project.EmbeddedProject
import play.api.Logger
import play.api.libs.json._

class BulkProjectsRetriever(out: ActorRef) extends AbstractBulkDBHandler(out) {

  override val ErrorMsg: String = "Failed to retrieve projects"

  private val contributionsCountField = "contributions_count"
  private val createdAtField = "created_at"

  def receive = {
    case ListProjects(filter, offset, limit) =>
      Logger.info(s"actor ${self.path} - received msg : ${ListProjects(filter, offset, limit)}")

      filter match {
        case FeaturedKeyword =>
          executeQuery(DBUtilities.Project.getFeaturedProjets(offset, limit))

        case PopularKeyword =>
          executeQuery(DBUtilities.Project.bulkGetProjects(contributionsCountField, offset, limit))

        case LatestKeyword =>
          executeQuery(DBUtilities.Project.bulkGetProjects(createdAtField, offset, limit))

      }

    case ItemResult(jsonObject) =>
      // received new item , aggregate it to the final result Array
      Logger.info(s"actor ${self.path} - received msg : ${ItemResult(jsonObject)}")

      if (!jsonObject.isEmpty) {
        appendFinalResult(jsonObject)
      } else {
        unhandled(jsonObject)
      }

    case BulkResult(jsArray) =>
      val response = constructResponse(jsArray)
      response match {
        case Some(Response(jsonResult)) =>
          out ! Response(jsonResult)

        // todo always send to out
        case None =>
          out ! CouldNotParseJSON("failed to get projects",
            "couldn't parse json retrieved from the db ", this.getClass.toString)
      }

    case Terminate =>
      Logger.info(s"actor ${self.path} - received msg : $Terminate ")
      context.stop(self)
  }

  /**
    * convert Json Object got from DB to a proper Response
    */
  override def constructResponse(jsonArray: JsonArray): Option[Response] = {
    try {
      val parsedJson = Json.parse(jsonArray.toString).as[JsArray]
      val categoryProjects: Seq[EmbeddedProject] = BulkProjectsResponseHelper(parsedJson)
      if (categoryProjects.isEmpty) None else Some(Response(Json.toJson(categoryProjects)))
    } catch {
      case e: Exception => None
    }
  }
}

object BulkProjectsRetriever {
  def props(out: ActorRef): Props = Props(new BulkProjectsRetriever(out))
}

