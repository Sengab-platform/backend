package project

import core.AbstractSpec
import messages.ProjectManagerMessages.CreateProject
import models.Response
import utils.Constants

class ProjectCreationSpec extends AbstractSpec {

  // test Create Project Request

  "Receptionist Actor" should "Create Project of template 1 Successfully" in {
    receptionist ! CreateProject(Constants.ValidProjectTemplateOne, Constants.ValidUserID)
    expectMsgType[Response]
  }

  it should "Create Project of template 2 Successfully" in {
    receptionist ! CreateProject(Constants.ValidProjectTemplateTwo, Constants.ValidUserID)
    expectMsgType[Response]
  }

  it should "Create Project of template 3 Successfully" in {
    receptionist ! CreateProject(Constants.ValidProjectTemplateThree, Constants.ValidUserID)
    expectMsgType[Response]
  }

  it should "Create Project of template 4 Successfully" in {
    receptionist ! CreateProject(Constants.ValidProjectTemplateFour, Constants.ValidUserID)
    expectMsgType[Response]
  }
}
