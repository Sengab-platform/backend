package auth.models

import models.User
import org.joda.time.DateTime
import securesocial.core._

case class UserAuth(main: BasicProfile, gender: Option[String], bio: Option[String], identities: List[BasicProfile]) {
  create(main: BasicProfile, gender: Option[String], bio: Option[String], identities: List[BasicProfile])

  def create(main: BasicProfile, gender: Option[String], bio: Option[String], identities: List[BasicProfile]) {
    val userID = "user::" + main.userId
    val entity_type = "user"
    val first_name = main.firstName
    val last_name = main.lastName
    val image = main.avatarUrl
    val about = ???
    val stats: Map[String, Int] = Map("projects" -> 0, "contributions" -> 0)
    val dateTime: DateTime = DateTime.now
    val created_at = dateTime.toString

    User(userID, entity_type, first_name, last_name, image, about, stats, created_at)
  }
}