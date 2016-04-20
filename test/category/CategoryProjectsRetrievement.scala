package category

import core.AbstractSpec
import messages.CategoryManagerMessages.RetrieveCategoryProjects
import models.Response
import models.errors.GeneralErrors.NotFoundError
import models.project.Project.EmbeddedProject

class CategoryProjectsRetrievement extends AbstractSpec {

  // valid category AND has a project or more
  "Receptionist Actor" should "Return Project of category with id=1 Successfully" in {
    receptionist ! RetrieveCategoryProjects("category::1", 0, 20)
    val response = expectMsgType[Response]
    assert(response.jsonResult.validate[Seq[EmbeddedProject]].isSuccess)
  }

  // no such category OR category has no projects yet
  it should "Return NOT FOUND error" in {
    receptionist ! RetrieveCategoryProjects("category::20", 0, 20)
    expectMsgType[NotFoundError]
  }

}
