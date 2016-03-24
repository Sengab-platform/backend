package messages

object CategoryManagerMessages {

  trait CategoryMessage

  case class RetrieveCategories(offset: Integer, limit: Integer) extends CategoryMessage

  case class RetrieveCategoryProjects(categoryId: String, offset: Integer, limit: Integer) extends CategoryMessage

}
