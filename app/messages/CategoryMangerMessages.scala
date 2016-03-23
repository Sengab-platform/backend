package messages

object CategoryMangerMessages {

  trait CategoryMessage

  case class RetrieveCategories(offset: Integer, limit: Integer) extends CategoryMessage

  case class RetrieveCategoryProjects(categoryId: Integer, offset: Integer, limit: Integer) extends CategoryMessage

}
