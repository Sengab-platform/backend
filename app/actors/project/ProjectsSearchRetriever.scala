package actors.project

import actors.AbstractBulkDBHandler
import actors.AbstractBulkDBHandler.{BulkResult, ItemResult}
import actors.AbstractDBActor.Terminate
import akka.actor.{ActorRef, Props}
import com.couchbase.client.java.document.json.JsonArray
import helpers.Helper._
import messages.ProjectManagerMessages.SearchProjects
import models.Response
import models.errors.GeneralErrors.{CouldNotParseJSON, NotFoundError}
import models.project.Project.EmbeddedProject
import play.api.Logger
import play.api.libs.json._

class ProjectsSearchRetriever(out: ActorRef) extends AbstractBulkDBHandler(out) {

  override val ErrorMsg: String = "Failed to search for projects"

  def receive = {
    case SearchProjects(keyword, offset, limit) =>
      Logger.info(s"actor ${self.path} - received msg : ${SearchProjects(keyword, offset, limit)}")
      executeQuery(DBUtilities.Project.searchForProjectsByName(keyword, offset, limit))

    case ItemResult(jsonObject) =>
      // received new item , aggregate it to the final result Array
      Logger.info(s"actor ${self.path} - received msg : ${ItemResult(jsonObject)}")

      if (!jsonObject.isEmpty) {
        appendFinalResult(jsonObject)
      } else {
        unhandled(jsonObject)
      }

    case BulkResult(jsonArray) =>
      Logger.info(s"actor ${self.path} - received msg : ${BulkResult(jsonArray)}")

      if (jsonArray.isEmpty) {
        out ! NotFoundError("Couldn't find projects",
          "Constructed json array is empty", this.getClass.toString)
      } else {
        val response = constructResponse(jsonArray)
        response match {
          case Some(Response(jsonResult)) =>

            out ! Response(jsonResult)

          case None =>
            out ! CouldNotParseJSON("failed to get projects",
              "couldn't parse json retrieved from the db ", this.getClass.toString)
        }

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
      val projects = parsedJson.value.seq.map { projectItem => {

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
      if (projects.isEmpty) None else Some(Response(Json.toJson(projects)))


    } catch {
      case e: Exception => None
    }

  }
}

object ProjectsSearchRetriever {
  def props(out: ActorRef): Props = Props(new ProjectsSearchRetriever(out))
}
