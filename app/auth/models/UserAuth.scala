package auth.models

import org.joda.time.DateTime
import play.api.libs.json.{JsObject, Json}
import securesocial.core._

case class UserAuth(main: BasicProfile, gender: Option[String] = None, bio: Option[String] = None, identities: List[BasicProfile]) {
  create(main: BasicProfile, gender: Option[String], bio: Option[String], identities: List[BasicProfile])

  def create(main: BasicProfile, gender: Option[String], bio: Option[String], identities: List[BasicProfile]) {
    val userID = "user::" + main.userId
    val entity_type = "user"
    val first_name = main.firstName
    val last_name = main.lastName
    val image = Some(main.avatarUrl.get.split("\\?")(0))
    val about: Option[JsObject] = Some(Json.obj(
      "email" -> main.email,
      "bio" -> bio
    ))
    val stats = Map("projects" -> 0, "contributions" -> 0)
    val dateTime: DateTime = DateTime.now
    val created_at = dateTime.toString

    // TODO fix this :

    //    val user = User(userID, entity_type, first_name, last_name, image, about, stats, None, created_at)/
    //    DBUtilities.User.createUser(
    //      JsonObject.fromJson(
    //        Json.stringify(
    //          Json.toJson(
    //            user
    //          )
    //        )
    //      )
    //    ).subscribe()
  }
}