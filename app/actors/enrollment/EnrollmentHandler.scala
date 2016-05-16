package actors.enrollment

import actors.AbstractDBHandler
import actors.AbstractDBHandler.QueryResult
import akka.actor.{ActorRef, Props}
import com.couchbase.client.java.document.json.JsonObject
import helpers.Helper
import helpers.Helper._
import messages.EnrollmentManagerMessages.Enroll
import models.Response
import models.errors.GeneralErrors.{AlreadyExists, CouldNotParseJSON, NotFoundError}
import play.Logger
import play.api.libs.json.{JsObject, JsString, Json}


class EnrollmentHandler(out: ActorRef) extends AbstractDBHandler(out) {
  // this is the msg to user when error happens while querying from db
  override val ErrorMsg: String = "Enroll to project failed"

  override def receive = {
    case Enroll(userID, projectID) =>
      Logger.info(s"actor ${self.path} - received msg : ${Enroll(userID, projectID)}")

      executeQuery(DBUtilities.User.addProjectToEnrolledProjects(userID, projectID))

    case QueryResult(jsonObject) =>
      Logger.info(s"actor ${self.path} - received msg : ${QueryResult(jsonObject)} ")

      if (jsonObject.containsKey("projectId")) {
        val response = constructResponse(jsonObject)
        response match {
          case Some(Response(jsonResult)) =>
            // apply side effects

            // get project id as String
            val projectID = (jsonResult \ "project_id").as[String]
            executeQuery(DBUtilities.Project.add1ToProjectEnrollmentsCount(projectID))
            // get statsID
            val statsID = Helper.StatsIDPrefix + trimEntityID(projectID)
            executeQuery(DBUtilities.Stats.add1ToStatsEnrollmentsCount(statsID))
            out ! Response(jsonResult)

          case None =>
            out ! CouldNotParseJSON("failed to enroll user",
              "couldn't parse json retrieved from the db ", this.getClass.toString)
        }
      }
      else if (jsonObject.getString("id") == DBUtilities.DBConfig.ALREADY_ENROLLED) {
        out ! AlreadyExists("You are already enrolled to this project", "User's already enrolled to this project", this.getClass.toString)
      } else if (jsonObject.getString("id") == DBUtilities.DBConfig.NO_SUCH_PROJECT) {
        out ! NotFoundError("No such project", s"No such project with this ID", this.getClass.toString)
      }
  }


  override def constructResponse(jsonObject: JsonObject): Option[Response] = {
    try {
      val parsedJson = Json.parse(jsonObject.toString)
      //add project url
      val url = addField(parsedJson.as[JsObject], "url", helpers.Helper.ProjectPath + (parsedJson \ "projectId").as[String])

      val project = JsObject(Seq(
        "project_id" -> JsString((parsedJson \ "projectId").as[String]),
        "url" -> JsString((url \ "url").as[String])))

      Some(Response(project))

    } catch {
      case _: Exception => None
    }
  }
}

object EnrollmentHandler {
  def props(out: ActorRef): Props = Props(new EnrollmentHandler(out: ActorRef))
}