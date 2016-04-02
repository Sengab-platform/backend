package actors.project

import actors.AbstractDBHandlerActor
import actors.AbstractDBHandlerActor.{QueryResult, Terminate}
import akka.actor.{ActorRef, Props}
import com.couchbase.client.java.document.JsonDocument
import messages.ProjectManagerMessages.GetProjectDetails
import models.errors.Error
import models.errors.GeneralErrors.{CouldNotParseJSON, NotFoundError}
import models.project.Project
import models.{Category, Response, User}
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

    //      executeQuery(DBUtilities.Project.getProjectWithId(projectID))

    case Terminate =>
      Logger.info(s"actor ${self.path} - received msg : $Terminate ")
      context.stop(self)

    case err: Error =>
      Logger.info(s"actor ${self.path} - received msg : $err")
      out ! err

    case QueryResult(doc) =>
      Logger.info(s"actor ${self.path} - received msg : ${QueryResult(doc)} ")

      if (doc.content() != null) {
        val response = constructResponse(doc)
        response match {
          case Some(response) =>
            out ! response

          // TODO self or out? hmm.
          case None =>
            out ! CouldNotParseJSON("failed to get project details",
              "couldn't parse json retrieved from the db ", this.getClass.toString)

        }
      } else {
        out ! NotFoundError("no such project", "received null content document from DB", this.getClass.toString)
      }


  }

  override def constructResponse(doc: JsonDocument): Option[Response] = {

    try {
      val parsedJson: JsValue = Json.parse(doc.content().toString)

      // get embedded category
      val category = Category.generateEmbeddedCategory(
        (parsedJson \ "category" \ "id").as[String],
        (parsedJson \ "category" \ "name").as[String]
      )

      // get embedded user
      val owner = User.generateEmbeddedOwner(
        (parsedJson \ "owner" \ "id").as[String],
        (parsedJson \ "owner" \ "name").as[String],
        (parsedJson \ "owner" \ "image").as[String]
      )

      // get project details
      val project = Project.generateDetailedProject(
        doc.id(),
        (parsedJson \ "name").as[String],
        owner,
        (parsedJson \ "goal").as[Int],
        (parsedJson \ "image").as[String],
        (parsedJson \ "template_id").as[Int],
        (parsedJson \ "created_at").as[String],
        (parsedJson \ "brief_description").as[String],
        (parsedJson \ "detailed_description").as[String],
        (parsedJson \ "enrollments_count").as[Int], // return results ID
        (parsedJson \ "contributions_count").as[Int], // return results ID
        (parsedJson \ "is_featured").as[Boolean],
        category,
        (parsedJson \ "results").as[String], // return results ID
        (parsedJson \ "stats").as[String] // return stats ID
      )

      Some(Response(Json.toJson(project)))

    } catch {
      case e: Exception => None
    }
  }
}


object ProjectDetailsRetriever {
  def props(out: ActorRef): Props = Props(new ProjectDetailsRetriever(out))
}