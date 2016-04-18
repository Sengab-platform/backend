import actors.Receptionist
import akka.actor.ActorSystem
import akka.testkit._
import messages.ProjectManagerMessages.GetProjectDetails
import models.errors.GeneralErrors.NotFoundError
import org.scalatest._


class ReceptionistSpec extends TestKit(ActorSystem("test-system"))
  with FlatSpecLike
  with BeforeAndAfterAll
  with MustMatchers {

  override def afterAll = {
    TestKit.shutdownActorSystem(system)
  }

  "Receptionist Actor" should "Return Not Found Error" in {

    val sender = TestProbe()
    val receptionist = system.actorOf(Receptionist.props())
    sender.send(receptionist, GetProjectDetails("5"))

    val state = sender.expectMsgType[NotFoundError]

    //    state must equal(1)

  }


}
