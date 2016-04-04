package actors.category

import akka.actor.{Actor, Props}
import messages.CategoryManagerMessages.RetrieveCategories
import play.Logger

class CategoryManager extends Actor {

  override def receive = {
    case RetrieveCategories(offset, limit) =>
      Logger.info(s"actor ${self.path} - received msg : ${RetrieveCategories(offset, limit)}")
      // get an instance of CategoriesRetriever actor
      val categoriesRetriever = context.actorOf(CategoriesRetriever.props(sender()))
      // forward message to categoriesRetriever
      categoriesRetriever forward RetrieveCategories(offset, limit)
  }
}

object CategoryManager {
  def props(): Props = Props(new CategoryManager)
}