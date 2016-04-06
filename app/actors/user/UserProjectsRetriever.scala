package actors.user

import actors.AbstractBulkDBHandler
import actors.AbstractBulkDBHandler.{BulkResult, ItemResult}
import actors.AbstractDBActor.Terminate
import akka.actor.{ActorRef, Props}
import com.couchbase.client.java.document.json.JsonArray
import helpers.Helper._
import messages.UserManagerMessages.ListProjectsOfUser
import models.Response
import models.errors.GeneralErrors.CouldNotParseJSON
import models.project.Project.EmbeddedProject
import play.Logger
import play.api.libs.json.{JsArray, Json}

class UserProjectsRetriever(out: ActorRef) extends AbstractBulkDBHandler(out) {

  override val ErrorMsg: String = "ERROR"

  override def receive = {
    case ListProjectsOfUser(userID, sort, offset, limit) =>
      Logger.info(s"actor ${self.path} - received msg : ${ListProjectsOfUser(userID, sort, offset, limit)} ")
      sort match {
        case "enrolled" =>
          executeQuery(DBUtilities.User.getEnrolledProjectsForUser(userID, offset, limit))
        case _ =>
          Logger.info("ERROR")
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

object UserProjectsRetriever {
  def props(out: ActorRef): Props = Props(new UserProjectsRetriever(out))
}
