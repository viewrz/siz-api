package models

import java.util.Date

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

object User {
  type UserName = String
  type Email = String
  type Password = String
  type BcryptPassword = String
}
