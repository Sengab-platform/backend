package actors.category

import actors.AbstractBulkDBHandler
import actors.AbstractBulkDBHandler.{BulkResult, ItemResult}
import actors.AbstractDBActor.Terminate
import akka.actor.{ActorRef, Props}
import com.couchbase.client.java.document.json.JsonArray
import helpers.Helper._
import messages.CategoryManagerMessages.RetrieveCategoryProjects
import models.Response
import models.errors.GeneralErrors.CouldNotParseJSON
import models.project.Project.EmbeddedProject
import play.api.Logger
import play.api.libs.json._

class CategoryProjectsRetriever(out: ActorRef) extends AbstractBulkDBHandler(out) {

  override val ErrorMsg: String = "Retrieving category failed"

  override def receive = {
    case RetrieveCategoryProjects(categoryID, offset, limit) =>
      Logger.info(s"actor ${self.path} - received msg : ${RetrieveCategoryProjects(categoryID, offset, limit)}")
      executeQuery(DBUtilities.Project.getProjectWithSpecificCategory(categoryID, offset, limit))

    case ItemResult(jsonObject) =>
      // received new item , aggregate it to the final result Array
      Logger.info(s"actor ${self.path} - received msg : ${ItemResult(jsonObject)}")

      if (jsonObject.get("id") != DBUtilities.DBConfig.EMPTY_JSON_OBJECT) {
        appendFinalResult(jsonObject)
      } else {
        unhandled(jsonObject)
      }

    case BulkResult(jsArray) =>
      val response = constructResponse(jsArray)
      response match {
        case Some(Response(jsonResult)) =>
          out ! Response(jsonResult)

        case None =>
          out ! CouldNotParseJSON("failed to get categories",
            "couldn't parse json retrieved from the db ", this.getClass.toString)
      }

    // self terminate
    case Terminate =>
      Logger.info(s"actor ${self.path} - received msg : Terminate ")
      context stop self
  }


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

object CategoryProjectsRetriever {
  def props(out: ActorRef): Props = Props(new CategoryProjectsRetriever(out))
}
