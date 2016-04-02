package auth.models

import com.couchbase.client.java.document.json.JsonObject
import models.{About, NewUser, Stats}
import org.joda.time.DateTime
import play.api.libs.json.Json
import securesocial.core._

case class UserAuth(main: BasicProfile, gender: Option[String] = None, bio: Option[String] = None, identities: List[BasicProfile]) {
  create(main: BasicProfile, gender: Option[String], bio: Option[String], identities: List[BasicProfile])

  def create(main: BasicProfile, gender: Option[String], bio: Option[String], identities: List[BasicProfile]) {
    val userID = "user::" + main.userId
    // val entity_type = "user"
    val first_name = main.firstName
    val last_name = main.lastName
    val image = Some(main.avatarUrl.get.split("\\?")(0))
    val about = Some(About(main.email, bio))
    val stats = Some(Stats(0, 0))
    val dateTime: DateTime = DateTime.now
    val created_at = dateTime.toString

    // TODO fix this :

    val user = NewUser(
      userID, first_name = first_name, last_name = last_name, image = image,
      about = about, stats = stats, created_at = created_at)
    DBUtilities.User.createUser(
      JsonObject.fromJson(
        Json.stringify(
          Json.toJson(
            user
          )
        )
      )
    ).subscribe()
  }
}