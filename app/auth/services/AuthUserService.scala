package auth.services

import auth.models.UserAuth
import securesocial.core._
import securesocial.core.providers.MailToken
import securesocial.core.services.{SaveMode, UserService}

import scala.concurrent.Future

class AuthUserService extends UserService[UserAuth] {

  // to be implemented
  def find(providerId: String, userId: String): Future[Option[BasicProfile]] = {
    ???
  }

  def findByEmailAndProvider(email: String, providerId: String): Future[Option[BasicProfile]] = {
    ???
  }

  // to be implemented
  def save(user: BasicProfile, mode: SaveMode): Future[UserAuth] = {
    mode match {
      case SaveMode.SignUp => ???
      case SaveMode.LoggedIn =>
        // first see if there is a user with this BasicProfile already.
        findProfile(user) match {
          case Some(existingUser) => ???
          case None => ???
        }

      case SaveMode.PasswordChange => ???
    }
  }

  private def findProfile(p: BasicProfile) = {
    ???
  }

  def link(current: UserAuth, to: BasicProfile): Future[UserAuth] = {
    ???
  }

  def saveToken(token: MailToken): Future[MailToken] = {
    ???
  }

  def findToken(token: String): Future[Option[MailToken]] = {
    ???
  }

  def deleteToken(uuid: String): Future[Option[MailToken]] = {
    ???
  }

  def deleteExpiredTokens() {
    ???
  }

  override def updatePasswordInfo(user: UserAuth, info: PasswordInfo): Future[Option[BasicProfile]] = {
    ???
  }

  override def passwordInfoFor(user: UserAuth): Future[Option[PasswordInfo]] = {
    ???
  }

  private def updateProfile(user: BasicProfile, entry: ((String, String), UserAuth)): Future[UserAuth] = {
    ???
  }
}