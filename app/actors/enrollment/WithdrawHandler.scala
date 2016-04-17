package actors.enrollment

import actors.AbstractDBHandler
import actors.AbstractDBHandler.QueryResult
import akka.actor.{ActorRef, Props}
import com.couchbase.client.java.document.json.JsonObject
import helpers.Helper._
import messages.EnrollmentManagerMessages.Withdraw
import models.Response
import models.errors.GeneralErrors.CouldNotParseJSON
import play.api.Logger
import play.api.libs.json.{JsObject, JsString, Json}

class WithdrawHandler(out: ActorRef) extends AbstractDBHandler(out) {
  // this is the msg to user when error happens while querying from db
  override val ErrorMsg: String = "Withdraw from project failed"

  override def receive = {
    case Withdraw(userID: String, projectID: String) => Logger.info(s"actor ${self.path} - received msg : ${Withdraw(userID, projectID)}")
      executeQuery(DBUtilities.User.removeProjectFromEnrolledProjects(userID, projectID))


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

object WithdrawHandler {
  def props(out: ActorRef): Props = Props(new WithdrawHandler(out: ActorRef))
}