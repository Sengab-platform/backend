
import auth.models.UserAuth
import auth.services.AuthUserService
import securesocial.core.RuntimeEnvironment
import securesocial.core.providers.GoogleProvider

import scala.collection.immutable.ListMap

object Global extends play.api.GlobalSettings {

  /**
    * Application's custom Runtime Environment
    */
  object MyRuntimeEnvironment extends RuntimeEnvironment.Default {
    type U = UserAuth
    override lazy val userService: AuthUserService = new AuthUserService()
    override lazy val providers = ListMap(
      include(new GoogleProvider(routes, cacheService, oauth2ClientFor(GoogleProvider.Google))))
    override implicit val executionContext = play.api.libs.concurrent.Execution.defaultContext
  }

}