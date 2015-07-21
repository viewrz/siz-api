package dto

import java.util.Date

import models.User
import reactivemongo.bson.BSONObjectID
import utils.Hash

case class NewUser(email: Option[User.Email],
                   password: Option[User.Password],
                   username: Option[User.UserName],
                   facebookToken: Option[String]) {
  def toUser(facebookUserId: Option[String] = None, userId: Option[String] = None): User = User(
    email,
    password.map(Hash.bcrypt),
    userId.getOrElse(BSONObjectID.generate.stringify),
    username,
    facebookToken,
    facebookUserId,
    new Date())
}

