package messages

object UserManagerMessages {

  trait UserMessage

  case class GetUserProfile(userId: String) extends UserMessage

  case class ListUserActivity(userId: String, offset: Integer, limit: Integer) extends UserMessage

  case class ListProjectsOfUser(userId: String, sort: String, offset: Integer, limit: Integer) extends UserMessage

}
