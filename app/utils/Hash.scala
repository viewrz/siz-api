package utils

import java.security.MessageDigest
import org.mindrot.jbcrypt.BCrypt

object Hash {
  def md5(s: String): String = {
    MessageDigest.getInstance("MD5").digest(s.getBytes).map("%02x".format(_)).mkString
  }
  def sha256(s: String): String = {
    MessageDigest.getInstance("SHA256").digest(s.getBytes).map("%02x".format(_)).mkString
  }
  def bcrypt(password: String): String = {
    BCrypt.hashpw(password, BCrypt.gensalt())
  }
  def bcrypt_compare(password: String, passwordHash: String): Boolean = {
    BCrypt.checkpw(password, passwordHash)
  }
}
