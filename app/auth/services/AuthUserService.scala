package auth.services

import auth.models.UserAuth
import org.joda.time.DateTime
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
        if (!(doc.getString("id") == DBUtilities.DBConfig.EMPTY_JSON_DOC)) {
          val json = Json.parse(doc.toString)
          val user = Some(BasicProfile(
            "google",
            (json \ "id").as[String],
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

  def save(user: BasicProfile, mode: SaveMode): Future[UserAuth] = {

    def getUserInfo(isSignUp: Boolean): UserAuth = {
      val response = scala.io.Source.fromURL(
        "https://www.googleapis.com/plus/v1/people/me?fields=gender,tagline&access_token="
          + user.oAuth2Info.get.accessToken).mkString
      val json_response = Json.parse(response)
      val gender = (json_response \ "gender").asOpt[String]
      val bio = (json_response \ "tagline").asOpt[String]
      val newUser = UserAuth(user, gender, bio, List(user), isSignUp)
      newUser
    }
    mode match {
      case SaveMode.SignUp =>

        val newUser: UserAuth = getUserInfo(isSignUp = true)
        Future.successful(newUser)

      case SaveMode.LoggedIn =>

        val updatedUser: UserAuth = getUserInfo(isSignUp = false)
        Future.successful(updatedUser)
    }
  }

  def link(current: UserAuth, to: BasicProfile): Future[UserAuth] = {
    // Dummy implementation
    Future.successful(UserAuth(to, identities = List(to), isSignUp = false))
  }

  def saveToken(token: MailToken): Future[MailToken] = {
    // Dummy implementation
    val dateTime = DateTime.now()
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