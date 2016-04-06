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

      if (jsonObject.get("id") != DBUtilities.DBConfig.EMPTY_JSON_DOC) {
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
    val parsedJson = Json.parse(jsonArray.toString).as[JsArray]
    val categoryProjects = parsedJson.value.seq.map { projectItem => {

      val ProjectObj = projectItem.as[JsObject]

      // add project url to the json retrieved
      val ModifiedProject = addField(ProjectObj, "url", helpers.Helper.ProjectPath + (ProjectObj \ "id").as[String])

      // add owner url to the json retrieved
      val jsonTransformer = addTransformer(__ \ 'owner, "url", helpers.Helper.UserPath + (ProjectObj \ "owner" \ "id").as[String])

        // add category url to the json retrieved
        .compose(addTransformer(__ \ 'category, "url", helpers.Helper.CategoryPath + (ProjectObj \ "category" \ "category_id").as[String]))

      val EmbeddedProject = ModifiedProject
        .transform(jsonTransformer).get

      EmbeddedProject.as[EmbeddedProject]
    }
    }
    if (categoryProjects.isEmpty) None else Some(Response(Json.toJson(categoryProjects)))
  }
}

object CategoryProjectsRetriever {
  def props(out: ActorRef): Props = Props(new CategoryProjectsRetriever(out))
}
