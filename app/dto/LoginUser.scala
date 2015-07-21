package dto

import models.User

case class LoginUser(email: Option[User.Email],
                     password: Option[User.Password],
                     username: Option[User.UserName],
                     facebookToken: Option[String])
