package actors.category

import actors.AbstractBulkDBHandler
import actors.AbstractBulkDBHandler.{BulkResult, ItemResult}
import actors.AbstractDBActor.Terminate
import akka.actor.{ActorRef, Props}
import com.couchbase.client.java.document.json.JsonArray
import helpers.Helper._
import messages.CategoryManagerMessages.RetrieveCategories
import models.errors.GeneralErrors.CouldNotParseJSON
import models.{DetailedCategory, Response}
import play.api.Logger
import play.api.libs.json._


class CategoriesRetriever(out: ActorRef) extends AbstractBulkDBHandler(out) {

  // this is the msg to user when error happens while querying from db
  override val ErrorMsg: String = "Retrieving categories failed"


  override def receive = {
    case RetrieveCategories(offset, limit) =>
      Logger.info(s"actor ${self.path} - received msg : ${RetrieveCategories(offset, limit)}")
      executeQuery(DBUtilities.Category.bulkGetCategories(offset, limit))

    case ItemResult(jsonObject) =>
      // received new item , aggregate it to the final result Array
      Logger.info(s"actor ${self.path} - received msg : ${ItemResult(jsonObject)}")

      if (jsonObject.get("id") != DBUtilities.DBConfig.EMPTY_JSON_OBJECT) {
        appendFinalResult(jsonObject)
      } else {
        unhandled(jsonObject)
      }

    case BulkResult(jsArray) =>
      val response = constructResponse(jsArray)
      response match {
        case Some(Response(jsonResult)) =>
          out ! Response(jsonResult)

        case None =>
          out ! CouldNotParseJSON("failed to get categories",
            "couldn't parse json retrieved from the db ", this.getClass.toString)
      }

    // self terminate
    case Terminate =>
      Logger.info(s"actor ${self.path} - received msg : Terminate ")
      context stop self
  }

  // try to convert the retrieved JsonDocument from db to a GetCategoriesResponse
  override def constructResponse(jsonArray: JsonArray): Option[Response] = {
    val parsedJson = Json.parse(jsonArray.toString).as[JsArray]
    val categories = parsedJson.value.seq.map { categoryItem => {
      val CategoryObj = categoryItem.as[JsObject]
      // add category url to the json retrieved
      val FullCategoryObj = addField(CategoryObj, "url", helpers.Helper.CategoryPath + (CategoryObj \ "id").as[String])
      FullCategoryObj.as[DetailedCategory]
    }
    }
    if (categories.isEmpty) None else Some(Response(Json.toJson(categories)))
  }
}

object CategoriesRetriever {
  def props(out: ActorRef): Props = Props(new CategoriesRetriever(out))
}


