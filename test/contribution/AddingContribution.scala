package contribution

import core.AbstractSpec
import messages.ContributionManagerMessages.CreateContribution
import models.Response
import utils.Constants

class AddingContribution extends AbstractSpec {

  "Receptionist Actor" should "Add contribution of template one successfully" in {
    receptionist ! CreateContribution(Constants.ContributionTemplateOne, Constants.Contributor)
    expectMsgType[Response]
  }

  it should "Add contribution of template two successfully" in {
    receptionist ! CreateContribution(Constants.ContributionTemplateTwo, Constants.Contributor)
    expectMsgType[Response]
  }

  it should "Add contribution of template three successfully" in {
    receptionist ! CreateContribution(Constants.ContributionTemplateThree, Constants.Contributor)
    expectMsgType[Response]
  }

  it should "Add contribution of template four successfully" in {
    receptionist ! CreateContribution(Constants.ContributionTemplateFour, Constants.Contributor)
    expectMsgType[Response]
  }

}
