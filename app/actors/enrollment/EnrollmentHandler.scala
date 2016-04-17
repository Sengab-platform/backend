package actors.enrollment

import actors.AbstractDBHandler
import actors.AbstractDBHandler.QueryResult
import akka.actor.{ActorRef, Props}
import com.couchbase.client.java.document.json.JsonObject
import messages.EnrollmentManagerMessages.Enroll
import models.Response
import models.errors.GeneralErrors.CouldNotParseJSON
import play.api.Logger
import play.api.libs.json._


class EnrollmentHandler(out: ActorRef) extends AbstractDBHandler(out) {
  // this is the msg to user when error happens while querying from db
  override val ErrorMsg: String = "Enroll to project failed"

  override def receive = {
    case Enroll(userID, projectID) =>
      Logger.info(s"actor ${self.path} - received msg : ${Enroll(userID, projectID)}")
      val DBProjectID = projectID.projectID.toString

      executeQuery(DBUtilities.User.addProjectToEnrolledProjects(userID, DBProjectID))


    case QueryResult(jsonObject) =>
      val response = constructResponse(jsonObject)
      response match {
        case Some(Response(jsonResult)) =>
          out ! Response(jsonResult)

        case None =>
          out ! CouldNotParseJSON("failed to enroll user",
            "couldn't parse json retrieved from the db ", this.getClass.toString)
      }
  }

  override def constructResponse(jsonObject: JsonObject): Option[Response] = {
    try {
      val parsedJson = Json.parse(jsonObject.toString)
      Some(Response(parsedJson))
    } catch {
      case _: Exception => None
    }
  }
}

object EnrollmentHandler {
  def props(out: ActorRef): Props = Props(new EnrollmentHandler(out: ActorRef))
}