package project

import core.AbstractSpec
import messages.ProjectManagerMessages.CreateProject
import models.Response
import utils.Constants

class ProjectCreationSpec extends AbstractSpec {

  // test Create Project Request

  "Receptionist Actor" should "Create Project Successfully" in {
    val newProject = Constants.ValidProject
    receptionist ! CreateProject(newProject, Constants.ValidUserID)
    expectMsgType[Response]
  }
}
