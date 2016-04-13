package auth.models

import com.couchbase.client.java.document.json.JsonObject
import helpers.Helper
import models.{About, NewUser, Stats}
import org.joda.time.DateTime
import play.api.libs.json.Json
import securesocial.core._

case class UserAuth(main: BasicProfile, gender: Option[String] = None, bio: Option[String] = None,
                    identities: List[BasicProfile], isSignUp: Boolean) {

  create(main: BasicProfile, gender: Option[String], bio: Option[String], identities: List[BasicProfile])

  def create(main: BasicProfile, gender: Option[String], bio: Option[String],
             identities: List[BasicProfile]) {

    val userID = Helper.UserIDPrefix + main.userId
    val first_name = main.firstName
    val last_name = main.lastName
    val image = Some(main.avatarUrl.get.split("\\?")(0))
    val about = Some(About(main.email, bio))
    val stats = Some(Stats(0, 0))
    val dateTime: DateTime = DateTime.now
    val created_at = dateTime.toString

    if (isSignUp) {
      val user = NewUser(
        userID, first_name = first_name, last_name = last_name, image = image,
        about = about, stats = stats, created_at = created_at,
        contributions = Helper.UserPath + userID + Helper.Contributions,
        projects = Helper.UserPath + userID + Helper.Created, enrolled_projects = List())

      DBUtilities.User.createUser(
        JsonObject.fromJson(
          Json.stringify(
            Json.toJson(
              user
            )
          )
        )
      ).subscribe()
    } else {
      val aboutObj = JsonObject.fromJson(Json.stringify(Json.toJson(about)))
      DBUtilities.User.updateSigningInUser(userID, first_name.get, last_name.get, image.get, aboutObj)
        .subscribe()
    }
  }
}