package core

import actors.Receptionist
import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, FlatSpecLike, MustMatchers}

class AbstractSpec extends TestKit(ActorSystem("test-system"))
  with FlatSpecLike
  with ImplicitSender
  with BeforeAndAfterAll
  with MustMatchers {

  val receptionist = system.actorOf(Receptionist.props(), "receptionist")


  override def afterAll = {
    TestKit.shutdownActorSystem(system)
  }

}