package actors.category

import akka.actor.{Actor, Props}
import messages.CategoryMangerMessages.RetrieveCategoryProjects

class CategoryProjectsRetriever extends Actor {
  override def receive = {
    case RetrieveCategoryProjects(categoryId, offset, limit) => ???
  }
}

object CategoryProjectsRetriever {
  def props(): Props = Props(new CategoryProjectsRetriever)
}
