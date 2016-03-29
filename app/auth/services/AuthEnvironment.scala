package auth.services

import auth.models.UserAuth
import securesocial.core.RuntimeEnvironment
import securesocial.core.providers.GoogleProvider

import scala.collection.immutable.ListMap

class AuthEnvironment extends RuntimeEnvironment.Default {
  override type U = UserAuth
  override lazy val userService: AuthUserService = new AuthUserService()
  override lazy val providers = ListMap(
    include(new GoogleProvider(routes, cacheService, oauth2ClientFor(GoogleProvider.Google))))
  override implicit val executionContext = play.api.libs.concurrent.Execution.defaultContext
}
