package messages

object CategoryManagerMessages {

  trait CategoryMessage

  case class RetrieveCategories(offset: Int, limit: Int) extends CategoryMessage

  case class RetrieveCategoryProjects(categoryID: String, offset: Int, limit: Int) extends CategoryMessage

}
