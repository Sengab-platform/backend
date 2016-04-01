package auth.services

import auth.models.UserAuth
import org.joda.time.DateTime
import play.Logger
import play.api.libs.json.Json
import rx.lang.scala.JavaConversions.toScalaObservable
import securesocial.core._
import securesocial.core.providers.MailToken
import securesocial.core.services.{SaveMode, UserService}

import scala.concurrent.{Future, Promise}

class AuthUserService extends UserService[UserAuth] {


  def find(providerId: String, userId: String): Future[Option[BasicProfile]] = {

    val promise = Promise[Option[BasicProfile]]
    import scala.concurrent.ExecutionContext.Implicits.global

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
    Future.successful(None)
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
      //
      //      case SaveMode.PasswordChange => ???
    }
  }

  def link(current: UserAuth, to: BasicProfile): Future[UserAuth] = {
    // Dummy implementation
    Future.successful(UserAuth(to, identities = List(to)))
  }

  def saveToken(token: MailToken): Future[MailToken] = {
    // Dummy implementation
    val dateTime = DateTime
    Future.successful(MailToken("uuid", "email", dateTime, dateTime, isSignUp = false))
  }

  def findToken(token: String): Future[Option[MailToken]] = {
    // Dummy implementation
    Future.successful(None)
  }

  def deleteToken(uuid: String): Future[Option[MailToken]] = {
    // Dummy implementation
    Future.successful(None)
  }

  def deleteExpiredTokens() {
  }

  override def updatePasswordInfo(user: UserAuth, info: PasswordInfo): Future[Option[BasicProfile]] = {
    // Dummy implementation
    Future.successful(None)
  }

  override def passwordInfoFor(user: UserAuth): Future[Option[PasswordInfo]] = {
    // Dummy implementation
    Future.successful(None)
  }
}