package actors.user

import actors.AbstractBulkDBHandler
import actors.AbstractBulkDBHandler.{BulkResult, ItemResult}
import actors.AbstractDBActor.Terminate
import akka.actor.{ActorRef, Props}
import com.couchbase.client.java.document.json.JsonArray
import helpers.Helper._
import messages.UserManagerMessages.ListProjectsOfUser
import models.Response
import models.errors.GeneralErrors.{CouldNotParseJSON, NotFoundError}
import models.project.Project.EmbeddedProject
import play.Logger
import play.api.libs.json.{JsArray, Json}

class UserProjectsRetriever(out: ActorRef) extends AbstractBulkDBHandler(out) {

  override val ErrorMsg: String = "Failed to retrieve user projects"

  override def receive = {
    case ListProjectsOfUser(userID, sort, offset, limit) =>
      Logger.info(s"actor ${self.path} - received msg : ${ListProjectsOfUser(userID, sort, offset, limit)} ")
      sort match {
        case "enrolled" =>
          executeQuery(DBUtilities.User.getEnrolledProjectsForUser(userID, offset, limit))
        case "created" =>
          executeQuery(DBUtilities.User.getProjectsCreatedByUser(userID, offset, limit))
        case _ =>
          Logger.info("ERROR: Only created and enrolled projects supported.")
      }

    case ItemResult(jsonObject) =>
      // received new item , aggregate it to the final result Array
      Logger.info(s"actor ${self.path} - received msg : ${ItemResult(jsonObject)}")
      if (jsonObject.getString("id") != DBUtilities.DBConfig.EMPTY_JSON_OBJECT) {
        appendFinalResult(jsonObject)
      } else {
        unhandled(jsonObject)
      }

    case BulkResult(jsArray) =>
      if (jsArray.isEmpty) {
        out ! NotFoundError("Couldn't find projects",
          "Constructed json array is empty", this.getClass.toString)
      } else {
        val response = constructResponse(jsArray)
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
      val projects: Seq[EmbeddedProject] = BulkProjectsResponseHelper(parsedJson)
      if (projects.isEmpty) None else Some(Response(Json.toJson(projects)))
    } catch {
      case e: Exception => None
    }
  }
}

object UserProjectsRetriever {
  def props(out: ActorRef): Props = Props(new UserProjectsRetriever(out))
}
