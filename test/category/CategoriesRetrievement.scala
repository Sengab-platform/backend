package category

import core.AbstractSpec
import messages.CategoryManagerMessages.RetrieveCategories
import models.{DetailedCategory, Response}

class CategoriesRetrievement extends AbstractSpec {

  "Receptionist Actor" should "Return categories Successfully" in {
    receptionist ! RetrieveCategories(0, 20)
    val response = expectMsgType[Response]
    assert(response.jsonResult.validate[Seq[DetailedCategory]].isSuccess)
  }

}
