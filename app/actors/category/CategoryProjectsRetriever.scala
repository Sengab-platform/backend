package actors.category

import akka.actor.{Actor, Props}
import messages.CategoryManagerMessages.RetrieveCategoryProjects

class CategoryProjectsRetriever extends Actor {
  override def receive = {
    case RetrieveCategoryProjects(categoryId, offset, limit) => ???
  }
}

object CategoryProjectsRetriever {
  def props(): Props = Props(new CategoryProjectsRetriever)
}
