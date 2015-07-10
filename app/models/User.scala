package models

import play.api.libs.json.Json
import reactivemongo.api.indexes.{IndexType, Index}
import reactivemongo.bson.BSONObjectID
import utils.Hash
import scala.concurrent.ExecutionContext.Implicits.global
import java.util.Date

import scala.concurrent.Future

case class User(email: Option[User.Email],
                passwordHash: Option[User.BcryptPassword],
                id: String,
                username: Option[User.UserName],
                facebookToken: Option[String],
                facebookUserId: Option[String],
                creationDate: Date,
                state: Option[String] = None)
case class NewUser(email: Option[User.Email],
                   password: Option[User.Password],
                   username: Option[User.UserName],
                   facebookToken: Option[String])
case class LoginUser(email: Option[User.Email],
                     password: Option[User.Password],
                     username: Option[User.UserName],
                     facebookToken: Option[String])

object User extends MongoModel("users") {
  type UserName = String
  type Email = String
  type Password = String
  type BcryptPassword = String

  def findByField(field: String, value: String) = collection.find(
    Json.obj(field -> value)).cursor[User].collect[List]()

  def ensureIndexes = {
    collection.indexesManager.ensure(Index(Seq("email" -> IndexType.Ascending), name = Some("emailUniqueIndex"), unique = true, sparse = true))
    collection.indexesManager.ensure(Index(Seq("username" -> IndexType.Ascending), name = Some("usernameUniqueIndex"), unique = true, sparse = true))
    collection.indexesManager.ensure(Index(Seq("facebookUserId" -> IndexType.Ascending), name = Some("facebookUserIdUniqueIndex"), unique = true, sparse = true))
  }

  def fromNewUser(newUser: NewUser, facebookUserId: Option[String] = None, userId: Option[String] = None): User = new User(
    newUser.email,
    newUser.password.map(Hash.bcrypt),
    userId.getOrElse(BSONObjectID.generate.stringify),
    newUser.username,
    newUser.facebookToken,
    facebookUserId,
    new Date())

  def create(user: User) = collection.insert(user)
  def findById(id: String) = collection.find(Json.obj("_id" -> Json.obj("$oid" -> id))).cursor[User].collect[List]()
  def findByEmail(email: Email) = findByField("email", email)
  def findByUsername(username: UserName) = findByField("username", username)
  def findByFacebookUserId(facebookUserId: String) = findByField("facebookUserId", facebookUserId)
}
