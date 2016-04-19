package actors

import actors.AbstractDBActor.Terminate
import actors.AbstractDBHandler.QueryResult
import akka.actor.ActorRef
import com.couchbase.client.java.document.json.JsonObject
import models.Response

/**
  * this class should be inherited by any actor communicating with db
  * these are the 3 params for subscribe method called in executeQuery method.
  * method executeQuery has to be called within the actor and pass Observable[JsonObject].
  */

abstract class AbstractDBHandler(out: ActorRef) extends AbstractDBActor[JsonObject](out) {


  /**
    * called when the db query get data back as JsonObject
    */
  override def onNext(): (JsonObject) => Unit = {
    jsonObject: JsonObject => {
      self ! QueryResult(jsonObject)
    }
  }


  /**
    * called when the db query completes and all Json Objects retrieved
    */
  override def onComplete(): () => Unit = { () => {
    self ! Terminate
  }
  }

  /**
    * convert Json Object got from DB to a proper Response
    */
  override def constructResponse(jsonObject: JsonObject): Option[Response]

}

object AbstractDBHandler {

  /**
    * this message should be sent to self when the db query successes to get data which is wrapped
    * by this message as jsonObject
    *
    */
  case class QueryResult(jsonObject: JsonObject)

}
