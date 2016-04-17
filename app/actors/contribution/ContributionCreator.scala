package actors.contribution

import actors.AbstractDBActor.Terminate
import actors.AbstractDBHandler
import actors.AbstractDBHandler.QueryResult
import akka.actor.{ActorRef, Props}
import com.couchbase.client.java.document.json.JsonObject
import helpers.Helper
import messages.ContributionManagerMessages.CreateContribution
import models.Response
import models.errors.Error
import models.errors.GeneralErrors.CouldNotParseJSON
import play.api.Logger
import play.api.libs.json.{JsObject, JsString, JsValue, Json}

class ContributionCreator(out: ActorRef) extends AbstractDBHandler(out) {
  override val ErrorMsg: String = "Failed to add contribution"

  override def receive = {
    case CreateContribution(contribution, contributor) =>
      Logger.info(s"actor ${self.path} - received msg : ${CreateContribution(contribution, contributor)} ")

      val contributionObj = toJsonObject(Json.toJson(contribution))

      val trimmedUserID = Helper.trimUserID(contributor.id)
      val trimmedProjectID = Helper.trimProjectID(contribution.project_id)

      executeQuery(DBUtilities.Contribution.createContribution(trimmedProjectID, trimmedUserID, contributionObj))


    case Terminate =>
      Logger.info(s"actor ${self.path} - received msg : Terminate ")
      context.stop(self)

    // error happened send to Out
    case err: Error =>
      Logger.info(s"actor ${self.path} - received msg : $err")
      out ! err

    case QueryResult(doc) =>
      Logger.info(s"actor ${self.path} - received msg : ${QueryResult(doc)}")

      val response = constructResponse(doc)

      response match {
        case Some(response) =>
          out ! response

        case None =>
          out ! CouldNotParseJSON("project created successfully, but error has happened",
            "couldn't parse json retrieved from the db ", this.getClass.toString)

      }

  }

  /**
    * convert Json Object got from DB to a proper Response
    */
  override def constructResponse(jsonObject: JsonObject): Option[Response] = {
    try {
      val parsedJson: JsValue = Json.parse(jsonObject.toString)

      val jsResponse = JsObject(Seq(
        "id" -> JsString((parsedJson \ "id").as[String]),
        "url" -> JsString(Helper.ContributionPath + (parsedJson \ "id").as[String])))

      Some(Response(jsResponse))
    } catch {
      case _: Exception => None
    }
  }
}

object ContributionCreator {
  def props(out: ActorRef): Props = Props(new ContributionCreator(out))
}