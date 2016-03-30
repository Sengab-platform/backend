package auth.services

import auth.models.UserAuth
import play.Logger
import play.api.libs.json.Json
import rx.lang.scala.JavaConversions.toScalaObservable
import securesocial.core._
import securesocial.core.providers.MailToken
import securesocial.core.services.{SaveMode, UserService}

import scala.concurrent.{Future, Promise}

class AuthUserService extends UserService[UserAuth] {


  // to be implemented
  def find(providerId: String, userId: String): Future[Option[BasicProfile]] = {

    val promise = Promise[Option[BasicProfile]]
    import scala.concurrent.ExecutionContext.Implicits.global

    Logger.info(userId)

    toScalaObservable(DBUtilities.User.getUserWithId("user::" + userId))
      .subscribe(doc => {
        if (!(doc.content() == null)) {
          val json = Json.parse(doc.content().toString)
          val user = Some(BasicProfile(
            "google",
            doc.id,
            (json \ "first_name").asOpt[String],
            (json \ "last_name").asOpt[String],
            None,
            None,
            (json \ "image").asOpt[String],
            AuthenticationMethod.OAuth2
          ))

          promise.success(user)
        }
        else promise.success(None)
      }, error => promise.success(None))

    promise.future.map {
      case Some(basic) =>
        Some(basic)
      case None =>
        None
    }
  }

  def findByEmailAndProvider(email: String, providerId: String): Future[Option[BasicProfile]] = {
    ???
  }

  // to be implemented
  def save(user: BasicProfile, mode: SaveMode): Future[UserAuth] = {

    mode match {
      case SaveMode.SignUp =>

        val response = scala.io.Source.fromURL(
          "https://www.googleapis.com/plus/v1/people/me?fields=gender,tagline&access_token="
            + user.oAuth2Info.get.accessToken).mkString
        val json_response = Json.parse(response)
        val gender = (json_response \ "gender").asOpt[String]
        val bio = (json_response \ "tagline").asOpt[String]

        val newUser = UserAuth(user, gender, bio, List(user))
        Logger.info("SIGNUP")
        Future.successful(newUser)

      case SaveMode.LoggedIn =>

        // JUST TESTING
        Logger.info("SIGNIN")
        val g: Option[String] = Option("a")
        val newUser = UserAuth(user, g, g, List(user))
        Future.successful(newUser)
      // first see if there is a user with this BasicProfile already.
      //              findProfile(user) match {
      //                case Some(existingUser) =>
      //
      //
      //
      //                case None =>
      //              }

      case SaveMode.PasswordChange => ???
    }
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

  private def findProfile(p: BasicProfile) = {
    ???
  }

  private def updateProfile(user: BasicProfile, entry: ((String, String), UserAuth)): Future[UserAuth] = {
    ???
  }
}