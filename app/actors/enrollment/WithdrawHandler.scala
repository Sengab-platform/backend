package actors.enrollment

import actors.AbstractDBHandler
import actors.AbstractDBHandler.QueryResult
import akka.actor.{ActorRef, Props}
import com.couchbase.client.java.document.json.JsonObject
import messages.EnrollmentManagerMessages.Withdraw
import models.errors.GeneralErrors.CouldNotParseJSON
import models.{Enrollment, Response}
import play.api.Logger
import play.api.libs.json.Json

class WithdrawHandler(out: ActorRef) extends AbstractDBHandler(out) {
  // this is the msg to user when error happens while querying from db
  override val ErrorMsg: String = "Withdraw from project failed"

  override def receive = {
    case Withdraw(userID: String, projectID: Enrollment) => Logger.info(s"actor ${self.path} - received msg : ${Withdraw(userID, projectID)}")
      val DBProjectID = projectID.projectID
      executeQuery(DBUtilities.User.removeProjectFromEnrolledProjects(userID, DBProjectID))


    case QueryResult(jsonObject) =>
      val response = constructResponse(jsonObject)
      response match {
        case Some(Response(jsonResult)) =>
          out ! Response(jsonResult)

        case None =>
          out ! CouldNotParseJSON("failed to withdraw user",
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

object WithdrawHandler {
  def props(out: ActorRef): Props = Props(new WithdrawHandler(out: ActorRef))
}