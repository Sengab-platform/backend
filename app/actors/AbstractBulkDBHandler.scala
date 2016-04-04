package actors

import actors.AbstractBulkDBHandler.{BulkResult, ItemResult}
import actors.AbstractDBActor.Terminate
import akka.actor.ActorRef
import com.couchbase.client.java.document.json.{JsonArray, JsonObject}
import models.Response


abstract class AbstractBulkDBHandler(out: ActorRef) extends AbstractDBActor[JsonArray](out) {


  /**
    * field to aggregate the json objects retrieved from DB
    */
  // todo reduce null values
  var finalResultArray: JsonArray = JsonArray.empty()


  override def onComplete: () => Unit = { () => {
    self ! BulkResult(finalResultArray)
    self ! Terminate
  }
  }

  /**
    * convert Json Object got from DB to a proper Response
    */
  override def constructResponse(jsonArray: JsonArray): Option[Response]


  /**
    * received new item , send to self as a QueryResult
    *
    */
  override def onNext(): (JsonObject) => Unit = {
    obj: JsonObject => {
      self ! ItemResult(obj)
    }
  }


  /**
    * aggregate new JsonOject
    *
    * @param jsonObject
    */
  def appendFinalResult(jsonObject: JsonObject): Unit = {
    finalResultArray.add(jsonObject)
  }


}


object AbstractBulkDBHandler {

  case class BulkResult(jsonArray: JsonArray)

  case class ItemResult(jsonObject: JsonObject)

}