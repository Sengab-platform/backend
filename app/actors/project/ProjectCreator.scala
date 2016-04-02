package actors.project

import actors.AbstractDBHandlerActor
import actors.AbstractDBHandlerActor.{QueryResult, Terminate}
import akka.actor.{ActorRef, Props}
import com.couchbase.client.java.document.JsonDocument
import messages.ProjectManagerMessages.CreateProject
import models.Response
import models.errors.Error
import models.errors.GeneralErrors.CouldNotParseJSON
import models.project.Project._
import play.api.Logger
import play.api.libs.json.{JsValue, Json}

class ProjectCreator(out: ActorRef) extends AbstractDBHandlerActor(out) {

  // this msg would sent to user when error happens while querying from db
  override val ErrorMsg: String = "Creating project failed"

  // when the query is completed, terminate self
  override def onComplete: () => Unit = { () => {
    self ! Terminate
  }
  }

  // send the retrieved JsonDocument to self wrapped in QueryResult message to be handled
  override def onNext(): (JsonDocument) => Unit = {
    doc: JsonDocument => {
      self ! QueryResult(doc)
    }
  }

  override def receive: Receive = {


    case CreateProject(project, userID) =>
      Logger.info(s"actor ${self.path} - received msg : ${CreateProject(project, userID)}")

      // construct Json Object to be inserted into DB
      // TODO

      val obj = toJsonObject(Json.toJson(project))
      executeQuery(DBUtilities.Project.createProject(userID, obj))


    // terminate self
    case Terminate =>
      Logger.info(s"actor ${self.path} - received msg : Terminate ")
      context.stop(self)


    // error happened send to Out
    case err: Error =>
      Logger.info(s"actor ${self.path} - received msg : $err")
      out ! err


    // got a document , construct proper response from it and send the response to out
    case QueryResult(doc) =>
      Logger.info(s"actor ${self.path} - received msg : $doc")

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
  override def constructResponse(doc: JsonDocument): Option[Response] = {

    // TODO implement this
    val parsedJson: JsValue = Json.parse(doc.content().toString)
    val createdProject = parsedJson.as[DetailedProject]
    Some(Response(Json.parse(doc.content().toString)))
    //    try {
    //      val name = (parsedJson \ "name").as[String]
    //      val createdAt = (parsedJson \ "created_at").as[String]
    //      val url = s"sengab.com/projects/${doc.id()}" // place holder
    //
    //      // deserializing succeeded, return the response object
    //      Some(CreateProjectResponse(doc.id(), url, name, createdAt))
    //
    //    } catch {
    //      // deserializing failed, return None
    //      case e: Exception => None
    //    }
    //  }
  }
}

object ProjectCreator {
  def props(out: ActorRef): Props = Props(new ProjectCreator(out))
}
