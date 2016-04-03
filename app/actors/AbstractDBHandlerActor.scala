package actors

import actors.AbstractDBHandlerActor.{QueryResult, Terminate}
import akka.actor.{Actor, ActorRef}
import com.couchbase.client.core.{BucketClosedException, CouchbaseException}
import com.couchbase.client.java.document.json.JsonObject
import models.Response
import models.errors.DBErrors.{BucketClosedError, CouchbaseError, GeneralServerError}
import models.errors.GeneralErrors.NotFoundError
import play.api.libs.json.{JsValue, Json}
import rx.lang.scala.JavaConversions.toScalaObservable

/**
  * this class should be inherited by any actor communicating with db
  * these are the 3 params for subscribe method called in executeQuery method.
  * method executeQuery has to be called within the actor and pass Observable[JsonObject].
  */

abstract class AbstractDBHandlerActor(out: ActorRef) extends Actor {

  /**
    * define the default message that will be sent to the user when error happen
    */
  val ErrorMsg: String


  /**
    * called when the db query get data back as JsonObject
    */
  def onNext(): (JsonObject) => Unit = {
    doc: JsonObject => {
      self ! QueryResult(doc)
    }
  }


  /**
    * convert the observable got from the DB queries methods into Scala Observable
    * then pass onNext() ,onError() and onComplete to subscribe method
    *
    * @param observable got from the DB queries methods
    */
  def executeQuery(observable: rx.Observable[JsonObject]): Unit = {
    toScalaObservable(observable).subscribe(onNext(), onError(), onComplete)
  }

  /**
    * called when error happens within the db query
    */
  def onError(msg: String = ErrorMsg): (Throwable) => Unit = {
    { y => {
      y match {
        case ex: NoSuchElementException =>
          self ! NotFoundError(msg, ex.getMessage, this.getClass.toString)

        case ex: BucketClosedException =>
          self ! BucketClosedError(msg, ex.getMessage, this.getClass.toString)

        case ex: CouchbaseException =>
          self ! CouchbaseError(msg, ex.getMessage, this.getClass.toString)

        case ex: Exception =>
          self ! GeneralServerError(msg, ex.getMessage, this.getClass.toString)
      }
      self ! Terminate
    }
    }

  }

  /**
    * this methods takes any number of observables and subscribe for it
    *
    * @param observables any number of observables to subscribe on
    */
  def executeSideEffectsQueries(observables: rx.Observable[JsonObject]*): Unit = {
    observables.foreach(o => o.subscribe())
  }

  /**
    * called when the db query completes and all Json Objects retrieved
    */
  def onComplete: () => Unit = { () => {
    self ! Terminate
  }
  }

  /**
    * convert Json Object got from DB to a proper Response
    */
  def constructResponse(jsonObject: JsonObject): Option[Response]

  /**
    * convert JsValue to JsonObject value
    */
  def toJsonObject(js: JsValue): JsonObject = JsonObject.fromJson(Json.stringify(js))

}

object AbstractDBHandlerActor {

  /**
    * this message should be sent to self when the db query successes to get data which is wrapped
    * by this message as jsonObject
    *
    */
  case class QueryResult(jsonObject: JsonObject)

  /**
    * this message is sent to self when completing the actor job to be killed
    */
  case object Terminate

}
