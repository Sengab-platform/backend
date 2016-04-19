package helpers

import models.project.Project
import models.project.Project.EmbeddedProject
import play.api.libs.json.{JsError, _}

object Helper {
  // URLs
  val StatsPath = "http://api.sengab.com/v1/stats/"
  val ResultPath = "http://api.sengab.com/v1/results/"
  val CategoryPath = "http://api.sengab.com/v1/categories/"
  val ProjectPath = "http://api.sengab.com/v1/projects/"
  val UserPath = "http://api.sengab.com/v1/users/"
  val ContributionsPath = "http://api.sengab.com/v1/contributions/"

  val CreatedPostfixPath = "/created_projects"
  val ContributionsPostfixPath = "/contributions"

  // IDs
  val UserIDPrefix = "user::"
  val ActivityIDPrefix = "activity::"
  val StatsIDPrefix = "stats::"
  val ResultIDPrefix = "result::"

  // expected filter keywords on list projects
  val PopularKeyword = "popular"
  val FeaturedKeyword = "featured"
  val LatestKeyword = "latest"

  val EnrolledKeyword = "enrolled"
  val CreatedKeyword = "created"

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

  def BulkProjectsResponseHelper(parsedJson: JsArray): Seq[Project.EmbeddedProject] = {
    val projects = parsedJson.value.seq.map { projectItem => {

      val ProjectObj = projectItem.as[JsObject]

      // add project url to the json retrieved
      val ModifiedProject = addField(ProjectObj, "url", helpers.Helper.ProjectPath + (ProjectObj \ "id").as[String])

      // add owner url to the json retrieved
      val jsonTransformer = addTransformer(__ \ 'owner, "url", helpers.Helper.UserPath + (ProjectObj \ "owner" \ "id").as[String])

        // add category url to the json retrieved
        .compose(addTransformer(__ \ 'category, "url", helpers.Helper.CategoryPath + (ProjectObj \ "category" \ "category_id").as[String]))

      val EmbeddedProject = ModifiedProject
        .transform(jsonTransformer).get

      EmbeddedProject.as[EmbeddedProject]
    }
    }
    projects
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

  /**
    * trim the word before the UUID number
    */
  def trimEntityID(EntityID: String) = EntityID.substring(EntityID.indexOf("::") + 2)

}

