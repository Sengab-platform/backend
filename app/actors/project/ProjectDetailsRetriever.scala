package actors.project

import actors.AbstractDBHandlerActor
import actors.AbstractDBHandlerActor.{QueryResult, Terminate}
import akka.actor.{ActorRef, Props}
import com.couchbase.client.java.document.JsonDocument
import messages.ProjectManagerMessages.GetProjectDetails
import models.errors.Error
import models.errors.GeneralErrors.CouldNotParseJSON
import models.responses.ProjectResponses.ProjectDetailsResponse
import models.responses.Response
import play.api.Logger
import play.api.libs.json.{JsValue, Json}


class ProjectDetailsRetriever(out: ActorRef) extends AbstractDBHandlerActor(out) {

  // this msg would sent to user when error happens while querying from db
  override val ErrorMsg: String = "failed to get project details"


  override def onComplete: () => Unit = { () => {
    self ! Terminate
  }
  }

  override def onNext(): (JsonDocument) => Unit = { doc: JsonDocument => {
    self ! QueryResult(doc)
  }
  }

  override def receive: Receive = {
    case GetProjectDetails(projectID) =>
      Logger.info(s"actor ${self.path} - received msg : ${GetProjectDetails(projectID)} ")
      executeQuery(DBUtilities.Project.getProjectWithId(projectID))

    case Terminate =>
      Logger.info(s"actor ${self.path} - received msg : $Terminate ")
      context.stop(self)

    case err: Error =>
      Logger.info(s"actor ${self.path} - received msg : $err")
      out ! err

    case QueryResult(doc) =>
      Logger.info(s"actor ${self.path} - received msg : ${QueryResult(doc)} ")

      val response = constructResponse(doc)
      response match {
        case Some(response) =>
          out ! response

        case None =>
          CouldNotParseJSON("failed to get project details",
            "couldn't parse json retrieved from the db ", this.getClass.toString)

      }

  }

  override def constructResponse(doc: JsonDocument): Option[Response] = {
    val parsedJson: JsValue = Json.parse(doc.content().toString)
    try {
      val name = (parsedJson \ "name").as[String]
      val createdAt = (parsedJson \ "created_at").as[String]
      val briefDescription = (parsedJson \ "brief_description").as[String]
      val detailedDescription = (parsedJson \ "detailed_description").as[String]
      val isFeatured = (parsedJson \ "is_featured").as[Boolean]
      val url = s"sengab.com/projects/${doc.id()}" // place holder
      val stats = s"sengab.com/projects/${doc.id()}/stats" // place holder
      val results = s"sengab.com/projects/${doc.id()}/results" // place holder
      Option(ProjectDetailsResponse(doc.id,
        url,
        name,
        createdAt,
        briefDescription,
        detailedDescription,
        isFeatured,
        results,
        stats
      ))

    } catch {
      case e: Exception => None
    }
  }
}


object ProjectDetailsRetriever {
  def props(out: ActorRef): Props = Props(new ProjectDetailsRetriever(out))
}