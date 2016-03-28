package auth.services

import auth.models.UserAuth
import securesocial.core.RuntimeEnvironment

class AuthEnvironment extends RuntimeEnvironment.Default {
  override type U = UserAuth
  override lazy val userService: AuthUserService = new AuthUserService()
  override implicit val executionContext = play.api.libs.concurrent.Execution.defaultContext
}
