package dao


import java.util.Date
import javax.inject.{Singleton, Inject}

import models.User
import dto.NewUser
import models.User._
import play.api.libs.json.Json
import play.modules.reactivemongo.{ReactiveMongoComponents, ReactiveMongoApi}


import reactivemongo.api.indexes.{IndexType, Index}
import reactivemongo.bson.BSONObjectID
import utils.Hash
import play.modules.reactivemongo.json._
import play.modules.reactivemongo.json.collection._
import formats.MongoJsonFormats._



import play.api.libs.concurrent.Execution.Implicits.defaultContext

@Singleton
class UserDao @Inject()(val reactiveMongoApi: ReactiveMongoApi) extends ReactiveMongoComponents {
  lazy val db = reactiveMongoApi.db

  def collection: JSONCollection = db.collection[JSONCollection]("users")

  // migrate data before app startup and after injection
  updateDB

  def findByField(field: String, value: String) = collection.find(
    Json.obj(field -> value)).cursor[User]().collect[List]()

  def updateDB = {
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

  def findById(id: String) = collection.find(Json.obj("_id" -> Json.obj("$oid" -> id))).cursor[User]().collect[List]()

  def findByEmail(email: Email) = findByField("email", email)

  def findByUsername(username: UserName) = findByField("username", username)

  def findByFacebookUserId(facebookUserId: String) = findByField("facebookUserId", facebookUserId)

}
