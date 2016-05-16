package actors.contribution

import actors.AbstractDBActor.Terminate
import actors.AbstractDBHandler
import actors.AbstractDBHandler.QueryResult
import actors.contribution.ContributionCreator.ExtendedQueryResult
import akka.actor.{ActorRef, Props}
import com.couchbase.client.java.document.json.{JsonArray, JsonObject}
import helpers.Helper
import messages.ContributionManagerMessages.CreateContribution
import models.contribution.Contribution
import models.contribution.ContributionDataTypes._
import models.errors.Error
import models.errors.GeneralErrors.{CouldNotParseJSON, Forbidden}
import models.{Contributor, Response}
import play.api.Logger
import play.api.libs.json.{JsObject, JsString, JsValue, Json}
import rx.lang.scala.JavaConversions._

class ContributionCreator(out: ActorRef) extends AbstractDBHandler(out) {
  override val ErrorMsg: String = "Failed to add contribution"


  override def receive = {
    case CreateContribution(contribution, contributor) =>
      Logger.info(s"actor ${self.path} - received msg : ${CreateContribution(contribution, contributor)} ")

      val contributionObj = toJsonObject(Json.toJson(contribution)).put("contributor", contributor.id)

      toScalaObservable(DBUtilities.Contribution.createContribution(contribution.project_id, contributor.id, contributionObj))
        .subscribe(jsonObj => {
          self ! ExtendedQueryResult(jsonObj, contribution, contributor)
        }, onError(), onComplete())

    case Terminate =>
      Logger.info(s"actor ${self.path} - received msg : Terminate ")
      context.stop(self)

    // error happened send to Out
    case err: Error =>
      Logger.info(s"actor ${self.path} - received msg : $err")
      out ! err

    // if the user not enrolled to the project
    case ExtendedQueryResult(jsonObject, contribution, contributor) if jsonObject.get("id") == DBUtilities.DBConfig.NOT_ENROLLED =>
      Logger.info(s"actor ${self.path} - received msg : ${QueryResult(jsonObject)}")

      out ! Forbidden("can't submit contribution, enroll to the project first",
        "User is not enrolled in this project", this.getClass.toString)

    // if the user is enrolled to the project

    case ExtendedQueryResult(jsonObject, contribution, contributor) =>
      Logger.info(s"actor ${self.path} - received msg : ${QueryResult(jsonObject)}")

      val response = constructResponse(jsonObject)

      response match {
        case Some(Response(jsResponse)) =>

          //           project created successfully , execute side effects now
          doSideEffects(ExtendedQueryResult(jsonObject, contribution, contributor))

          out ! Response(jsResponse)

        case None =>
          out ! CouldNotParseJSON("project created successfully, but error has happened",
            "couldn't parse json retrieved from the db ", this.getClass.toString)

      }

  }

  def doSideEffects(result: ExtendedQueryResult) = {


    val contribution = result.contribution
    val contributor = result.contributor

    val projectID = contribution.project_id
    val trimmedProjectID = Helper.trimEntityID(projectID)

    // construct new activity object
    val activityUUID = java.util.UUID.randomUUID.toString

    val activityObject = JsonObject.create()
      .put("activity_id", activityUUID)
      .put("activity_type", "cont")
      .put("created_at", contribution.created_at)
      .put("project", JsonObject.create().put("project_id", projectID))

    // construct proper observable to handle adding new result

    val addResult = result.contribution.data match {
      // add Result for Template Type One
      case ContributionDataTypeOne(location, answer) =>
        DBUtilities.Result.addResult(Helper.ResultIDPrefix + trimmedProjectID,
          answer,
          JsonObject.fromJson(Json.stringify(Json.toJson(location))))

      // add Result for Template Type Two
      case ContributionDataTypeTwo(image, caption) =>
        DBUtilities.Result.addResult(Helper.ResultIDPrefix + trimmedProjectID,
          JsonObject.fromJson(Json.stringify(Json.toJson(ContributionDataTypeTwo(image, caption)))))

      // add Result for Template Type Three
      case ContributionDataTypeThree(answers) =>
        DBUtilities.Result.addResult(Helper.ResultIDPrefix + trimmedProjectID, JsonArray.fromJson(Json.stringify(Json.toJson(answers))))

      // add Result for Template Type Four
      case ContributionDataTypeFour(image, caption, location) =>
        DBUtilities.Result.addResult(Helper.ResultIDPrefix + trimmedProjectID,
          JsonObject.fromJson(Json.stringify(Json.toJson(ContributionDataTypeFour(image, caption, location)))))

    }

    // execute side effects
    executeSideEffectsQueries(

      // add 1 to contributions count in the User document
      DBUtilities.User.add1ToUserContributionCount(contributor.id),
      // add 1 to contributions count in the Project document
      DBUtilities.Project.add1ToProjectContributionCount(projectID),
      // add 1 to contributions count in the Project Stats document
      DBUtilities.Stats.add1ToStatsContributionCount(Helper.StatsIDPrefix + trimmedProjectID),
      // add 1 to contributions count in the Project Results document
      DBUtilities.Result.add1ToResultsContributionCount(Helper.ResultIDPrefix + trimmedProjectID),
      // update gender numbers in the Project Stats document
      DBUtilities.Stats.updateContributorsGender(Helper.StatsIDPrefix + trimmedProjectID, contributor.id, contributor.gender),
      // add new Activity in the User Activity document
      DBUtilities.Activity.addActivity(projectID, Helper.ActivityIDPrefix + trimmedProjectID, activityObject),
      // add new Result in the Project Results document
      addResult

    )
  }


  /**
    * convert Json Object got from DB to a proper Response
    */
  override def constructResponse(jsonObject: JsonObject): Option[Response] = {
    try {
      val parsedJson: JsValue = Json.parse(jsonObject.toString)

      val jsResponse = JsObject(Seq(
        "id" -> JsString((parsedJson \ "id").as[String]),
        "url" -> JsString(Helper.ContributionsPath + (parsedJson \ "id").as[String])))

      Some(Response(jsResponse))
    } catch {
      case _: Exception => None
    }
  }
}

object ContributionCreator {
  def props(out: ActorRef): Props = Props(new ContributionCreator(out))

  case class ExtendedQueryResult(jsonObject: JsonObject, contribution: Contribution, contributor: Contributor)

}