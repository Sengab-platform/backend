package actors.project

import akka.actor.{Actor, ActorRef, Props}
import com.couchbase.client.core.BucketClosedException
import com.couchbase.client.java.document.JsonDocument
import com.couchbase.client.java.document.json.JsonObject
import messages.ProjectManagerMessages.CreateProject
import models.responses.ProjectResponses.CreateProjectResponse
import models.responses.{Error, ErrorMsg}
import play.api.Logger
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Results
import rx.lang.scala.JavaConversions.toScalaObservable

class ProjectCreator(out: ActorRef) extends Actor {


  override def receive = {

    case CreateProject(project, userID) => {
      Logger.info(s"actor ${self.path} - received msg : ${CreateProject(project, userID)}")

      // construct Json Object to be inserted into DB
      val obj = JsonObject.fromJson(Json.stringify(Json.toJson(project)))

      toScalaObservable(DBUtilities.Project.createProject(obj))
        .subscribe(myOnNext, myError, myComplete)

    }

    // terminate self
    case "Terminate" => {
      Logger.info(s"actor ${self.path} - received msg : Terminate ")
      context.stop(self)
    }

    // error happened send to Out
    case err: Error =>
      Logger.info(s"actor ${self.path} - received msg : $err")

      out ! err


    // got a document , construct proper response from it and send the response to out
    case d: Option[JsonDocument] => {
      Logger.info(s"actor ${self.path} - received msg : $d")

      d match {
        case Some(doc) =>
          val response = constructResponse(doc)
          response match {
            case Some(response) =>
              out ! response

            case None =>
              out ! Error(Results.InternalServerError(
                ErrorMsg("project created successfully, but error has happened",
                  "couldn't parse json retrieved from the db ").toJson))
          }
        case None =>
          unhandled("not handled")
      }
    }
  }


  def constructResponse(doc: JsonDocument): Option[CreateProjectResponse] = {
    val parsedJson: JsValue = Json.parse(doc.content().toString)
    try {
      val name = (parsedJson \ "name").as[String]
      val createdAt = (parsedJson \ "created_at").as[String]
      val url = s"sengab.com/projects/${doc.id()}" // place holder
      val image = "sengab.com/images/s45454" // place holder
      Option(CreateProjectResponse(doc.id(), url, name, image, createdAt))

    } catch {
      case e: Exception => None
    }
  }

  def myOnNext = { doc: JsonDocument => {
    self ! Option(doc)
  }
  }

  def myError = { e: Throwable =>

    e match {
      case ex: BucketClosedException =>
        self ! Error(Results.ServiceUnavailable(
          ErrorMsg("project creation failed", ex.getMessage).toJson))

      case _: Exception =>

        self ! Error(Results.ServiceUnavailable(
          ErrorMsg("project creation failed", "couldn't insert project into DB").toJson))
    }
  }

  def myComplete = { () => {

    self ! "Terminate"
  }
  }
}


object ProjectCreator {
  def props(out: ActorRef): Props = Props(new ProjectCreator(out))
}
