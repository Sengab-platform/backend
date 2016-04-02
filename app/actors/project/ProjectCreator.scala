package actors.project

import actors.AbstractDBHandlerActor
import actors.AbstractDBHandlerActor.{QueryResult, Terminate}
import akka.actor.{ActorRef, Props}
import com.couchbase.client.java.document.json.JsonObject
import messages.ProjectManagerMessages.CreateProject
import models.Response
import models.errors.Error
import models.errors.GeneralErrors.CouldNotParseJSON
import play.api.Logger
import play.api.libs.json.{JsObject, JsString, JsValue, Json}

class ProjectCreator(out: ActorRef) extends AbstractDBHandlerActor(out) {

  // this msg would sent to user when error happens while querying from db
  override val ErrorMsg: String = "Creating project failed"

  // when the query is completed, terminate self
  override def onComplete: () => Unit = { () => {
    self ! Terminate
  }
  }

  // send the retrieved JsonDocument to self wrapped in QueryResult message to be handled
  override def onNext(): (JsonObject) => Unit = {
    doc: JsonObject => {
      self ! QueryResult(doc)
    }
  }

  override def receive: Receive = {


    case CreateProject(project, userID) =>
      Logger.info(s"actor ${self.path} - received msg : ${CreateProject(project, userID)}")

      // construct Json Object to be inserted into DB

      val projectObj = toJsonObject(Json.toJson(project))
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

  // try to convert the retrieved JsonDocument from db to a CreateProjectResponse
  override def constructResponse(jsonObj: JsonObject): Option[Response] = {

    try {
      val parsedJson: JsValue = Json.parse(jsonObj.toString)

      val jsResponse = JsObject(Seq(
        "id" -> JsString((parsedJson \ "id").as[String]),
        "name" -> JsString((parsedJson \ "name").as[String]),
        "created_at" -> JsString((parsedJson \ "created_at").as[String]),
        "url" -> JsString(helpers.Helper.PROJECT_PATH + (parsedJson \ "id").as[String])))

      Some(Response(jsResponse))
    } catch {
      case _: Exception => None
    }
  }
}

object ProjectCreator {
  def props(out: ActorRef): Props = Props(new ProjectCreator(out))
}
