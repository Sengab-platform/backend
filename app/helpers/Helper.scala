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
    * Implemented by a Scala developer on Google Forms.
    * Used to transform a list of objects.
    *
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
}
