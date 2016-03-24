package messages

object UserManagerMessages {

  trait UserMessage

  case class GetUserProfile(userID: String) extends UserMessage

  case class ListUserActivity(userID: String, offset: Integer, limit: Integer) extends UserMessage

  case class ListProjectsOfUser(userID: String, sort: String, offset: Integer, limit: Integer) extends UserMessage

}
