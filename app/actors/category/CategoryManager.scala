package actors.category

import akka.actor.{Actor, Props}
import messages.CategoryManagerMessages.{RetrieveCategories, RetrieveCategoryProjects}

class CategoryManager extends Actor {
  override def receive = {
    case RetrieveCategories(offset, limit) => ???

    case RetrieveCategoryProjects(categoryId, offset, limit) => ???
  }
}

object CategoryManager {
  def props(): Props = Props(new CategoryManager)
}