package actors.project

import actors.AbstractDBActor.Terminate
import actors.AbstractDBHandler
import actors.AbstractDBHandler.QueryResult
import akka.actor.{ActorRef, Props}
import com.couchbase.client.java.document.json.{JsonArray, JsonObject}
import helpers.Helper
import messages.ProjectManagerMessages.CreateProject
import models.Response
import models.errors.Error
import models.errors.GeneralErrors.CouldNotParseJSON
import play.api.Logger
import play.api.libs.json._

class ProjectCreator(out: ActorRef) extends AbstractDBHandler(out) {

  // this msg would sent to user when error happens while querying from db
  override val ErrorMsg: String = "Creating project failed"


  override def receive: Receive = {


    case CreateProject(project, userID) =>
      Logger.info(s"actor ${self.path} - received msg : ${CreateProject(project, userID)}")

      // add contributions_count , enrollments_count with default values = 0  and entity_type with value = project
      val completedProject = Json.toJson(project).as[JsObject].
        +("contributions_count" -> JsNumber(0)).
        +("enrollments_count" -> JsNumber(0)).
        +("entity_type" -> JsString("project"))



      // construct Json Object to be inserted into DB
      val projectObj = toJsonObject(completedProject)
      executeQuery(DBUtilities.Project.createProject(userID, projectObj))


    // terminate self
    case Terminate =>
      Logger.info(s"actor ${self.path} - received msg : Terminate ")
      context.stop(self)


    // error happened send to Out
    case err: Error =>
      Logger.info(s"actor ${self.path} - received msg : $err")
      out ! err


    // got a json object , construct proper response from it and send the response to out
    case QueryResult(jsonObject) =>
      Logger.info(s"actor ${self.path} - received msg : ${QueryResult(jsonObject)}")

      val response = constructResponse(jsonObject)

      response match {
        case Some(Response(jsonResult)) =>

          // TODO try better solution
          // project created successfully , execute side effects now

          val createdProjectID = jsonObject.getString("id")
          val trimmedProjectID = Helper.trimProjectID(createdProjectID)
          executeSideEffectsQueries(
            DBUtilities.Stats.createStats("stats::" + trimmedProjectID, generateInitialStats()),
            DBUtilities.Result.createResult("result::" + trimmedProjectID, generateInitialResult(jsonObject)))

          // send response to out
          out ! Response(jsonResult)

        case None =>
          out ! CouldNotParseJSON("project created successfully, but error has happened",
            "couldn't parse json retrieved from the db ", this.getClass.toString)

      }
  }

  // try to convert the retrieved JsonDocument from db to a CreateProjectResponse
  override def constructResponse(jsonObj: JsonObject): Option[Response] = {

    try {
      val parsedJson: JsValue = Json.parse(jsonObj.toString)

      val jsResponse = JsObject(Seq(
        "id" -> JsString((parsedJson \ "id").as[String]),
        "name" -> JsString((parsedJson \ "name").as[String]),
        "created_at" -> JsString((parsedJson \ "created_at").as[String]),
        "url" -> JsString(Helper.ProjectPath + (parsedJson \ "id").as[String])))

      Some(Response(jsResponse))
    } catch {
      case _: Exception => None
    }
  }

  def generateInitialStats(): JsonObject = {
    JsonObject.create()
      .put("entity_type", "stats")
      .put("enrollments_count", 0)
      .put("contributions_count", 0)
      .put("contributors_gender",
        JsonObject.create()
          .put("male", 0)
          .put("female", 0))
  }

  def generateInitialResult(jsonObject: JsonObject): JsonObject = {

    val tempID: Int = jsonObject.getInt("template_id")

    tempID match {
      case 1 =>
        JsonObject.create()
          .put("contributions_count", 0)
          .put("results", JsonObject.create().put("yes", JsonArray.create()).put("no", JsonArray.create()))

      case 2 =>
        JsonObject.create()
          .put("contributions_count", 0)
          .put("results", JsonArray.create())

      case 3 =>

        val questions = jsonObject.getObject("template_body").getArray("questions")
        val questionsCount = jsonObject.getObject("template_body").getInt("questions_count")

        val resultsArray: JsonArray = JsonArray.create()

        for (i <- 0 until questionsCount) {
          resultsArray.add(JsonObject.create().put("id", questions.getObject(i).get("id"))
            .put("title", questions.getObject(i).get("title"))
            .put("yes_count", 0)
            .put("no_count", 0))
        }

        JsonObject.create()
          .put("contributions_count", 0)
          .put("results", resultsArray)


      case 4 =>
        JsonObject.create()
          .put("contributions_count", 0)
          .put("results", JsonArray.create())
    }
  }
}

object ProjectCreator {
  def props(out: ActorRef): Props = Props(new ProjectCreator(out))
}
