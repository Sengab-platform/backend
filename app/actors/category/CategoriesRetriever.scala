package actors.category

import akka.actor.{Actor, Props}
import messages.CategoryManagerMessages.RetrieveCategories

class CategoriesRetriever extends Actor {
  override def receive = {
    case RetrieveCategories(offset, limit) => ???
  }
}

object CategoriesRetriever {
  def props(): Props = Props(new CategoriesRetriever)
}
