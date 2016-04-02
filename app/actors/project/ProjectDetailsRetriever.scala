package actors.project

import actors.AbstractDBHandlerActor
import actors.AbstractDBHandlerActor.{QueryResult, Terminate}
import akka.actor.{ActorRef, Props}
import com.couchbase.client.java.document.json.JsonObject
import messages.ProjectManagerMessages.GetProjectDetails
import models.Response
import models.errors.Error
import play.api.Logger


class ProjectDetailsRetriever(out: ActorRef) extends AbstractDBHandlerActor(out) {

  // this msg would sent to user when error happens while querying from db
  override val ErrorMsg: String = "failed to get project details"


  override def onComplete: () => Unit = { () => {
    self ! Terminate
  }
  }

  override def onNext(): (JsonObject) => Unit = { doc: JsonObject => {
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

    //      if (doc.content() != null) {
    //        val response = constructResponse(doc)
    //        response match {
    //          case Some(response) =>
    //            out ! response
    //
    //          // TODO self or out? hmm.
    //          case None =>
    //            out ! CouldNotParseJSON("failed to get project details",
    //              "couldn't parse json retrieved from the db ", this.getClass.toString)
    //
    //        }
    //      } else {
    //        out ! NotFoundError("no such project", "received null content document from DB", this.getClass.toString)
    //      }


  }

  override def constructResponse(doc: JsonObject): Option[Response] = {
    //
    //    try {
    //      val parsedJson: JsValue = Json.parse(doc.content().toString)
    //      val project = parsedJson.as[DetailedProject]
    //      Some(Response(Json.toJson(project)))
    //      ???
    //    } catch {
    //      case e: Exception => None
    //    }
    ???
  }


}


object ProjectDetailsRetriever {
  def props(out: ActorRef): Props = Props(new ProjectDetailsRetriever(out))
}