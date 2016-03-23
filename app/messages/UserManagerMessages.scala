package messages

object UserManagerMessages {

  trait UserMessage

  case class GetUserProfile(userId: Integer) extends UserMessage

  case class ListUserActivity(userId: Integer, offset: Integer, limit: Integer) extends UserMessage

  case class ListProjectsOfUser(userId: Integer, sort: String, offset: Integer, limit: Integer) extends UserMessage

}
