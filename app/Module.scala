
import actors.Receptionist
import auth.services.AuthEnvironment
import com.google.inject.{AbstractModule, TypeLiteral}
import play.api.libs.concurrent.AkkaGuiceSupport
import securesocial.core.RuntimeEnvironment

class Module extends AbstractModule with AkkaGuiceSupport {

  override def configure() = {
    // SecureSocial
    val environment: AuthEnvironment = new AuthEnvironment
    bind(new TypeLiteral[RuntimeEnvironment] {}).toInstance(environment)
    // Akka
    bindActor[Receptionist]("receptionist")

  }

}
