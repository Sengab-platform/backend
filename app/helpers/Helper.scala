package helpers

import play.api.libs.json.{JsError, _}

object Helper {
  val STAT_PATH = "http://api.sengab.com/v1/stats/"
  val RESULT_PATH = "http://api.sengab.com/v1/results/"
  val CATEGORY_PATH = "http://api.sengab.com/v1/categories/"
  val PROJECT_PATH = "http://api.sengab.com/v1/projects/"
  val USER_PATH = "http://api.sengab.com/v1/users/"

  val CREATED = "/created_projects"
  val CONTRIBUTIONS = "/contributions"

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
