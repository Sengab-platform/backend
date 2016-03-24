package messages

object CategoryManagerMessages {

  trait CategoryMessage

  case class RetrieveCategories(offset: Integer, limit: Integer) extends CategoryMessage

  case class RetrieveCategoryProjects(categoryID: String, offset: Integer, limit: Integer) extends CategoryMessage

}
