package project

import core.AbstractSpec
import messages.ProjectManagerMessages.CreateProject
import models.Response
import models.project.Project.NewProject
import models.project.Templates.TemplateOne

class ProjectCreationSpec extends AbstractSpec {

  // test Create Project Request

  "Receptionist Actor" should "Create Project Successfully" in {
    val newProject = NewProject("Recognize",
      2000,
      "asasas",
      1,
      TemplateOne("What?"),
      "2015",
      "short",
      "detailed one",
      "category::5")
    receptionist ! CreateProject(newProject, "user::117521628211683444029")
    val response = expectMsgType[Response]
    //    assert(response.jsonResult \ "")
  }
}
