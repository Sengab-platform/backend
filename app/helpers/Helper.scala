package helpers

import play.api.libs.json.{JsError, _}

object Helper {
  // URLs
  val StatsPath = "http://api.sengab.com/v1/stats/"
  val ResultPath = "http://api.sengab.com/v1/results/"
  val CategoryPath = "http://api.sengab.com/v1/categories/"
  val ProjectPath = "http://api.sengab.com/v1/projects/"
  val UserPath = "http://api.sengab.com/v1/users/"

  val Created = "/created_projects"
  val Contributions = "/contributions"

  // IDs
  val UserIDPrefix = "user::"
  val ActivityIDPrefix = "activity::"

  /**
    * Transforms a JsArray using the provided Reads; cumulating errors.
    * Used to transform a list of objects.
    *
    * @author David P
    * @param reads the transforming Reads
    * @tparam A the Type deserialized by the given Reads
    * @return a JsArray transforming Reads
    */
  def tfList[A <: JsValue](reads: Reads[A])
  : Reads[JsArray] = Reads {
    case arr: JsArray =>
      val init: JsResult[Seq[JsValue]] = JsSuccess(Seq[JsValue]())
      arr.value.foldLeft(init) { (acc, e) =>
        acc.flatMap(seq => e.transform(reads).map(seq :+ _))
      } map JsArray
    case _ => JsError("expected JsArray")
  }

  //to add a JS Transformer to be used for adding a field in a sub Js Object
  def addTransformer(TargetObjectPath: JsPath,
                     NewKey: String,
                     NewValue: String
                    ): Reads[JsObject] = {
    TargetObjectPath.json.update(
      __.read[JsObject].map { o => o ++ Json.obj(NewKey ->
        JsString(NewValue))
      }
    )
  }

  // to add a field directly to a given Js Object
  def addField(TargetObject: JsObject,
               NewKey: String,
               NewValue: String): JsObject = {
    TargetObject + (NewKey -> JsString(NewValue))
  }
}

