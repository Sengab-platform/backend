package messages

object UserManagerMessages {

  trait UserMessage

  case class GetUserProfile(userID: String) extends UserMessage

  case class ListUserActivity(userID: String, offset: Int, limit: Int) extends UserMessage

  case class ListProjectsOfUser(userID: String, sort: String, offset: Int, limit: Int) extends UserMessage

}
