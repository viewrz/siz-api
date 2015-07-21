package models

import formats.APIJsonFormats
import play.api.data.validation.ValidationError
import play.api.i18n.{Lang, Messages}
import play.api.libs.json._
import play.api.Play.current
import play.api.i18n.Messages.Implicits._

case class Error(
                  title: String,
                  id: Option[String] = None,
                  detail: Option[String] = None)

object Error extends APIJsonFormats {
  def toTopLevelJson(errors: Option[Either[Error, Seq[Error]]]): JsValue = Json.toJson(TopLevel(errors = errors))

  def toTopLevelJson(error: Error): JsValue = toTopLevelJson(Some(Left(error)))

  def toTopLevelJson(validationErrors: Seq[(JsPath, Seq[ValidationError])])(implicit lang: Lang): JsValue =
    toTopLevelJson(
      Some(
        Right(
          validationErrors.map {
            error =>
              Error(
                Messages(
                  "error.field",
                  error._1.toString(),
                  error._2.map { er =>
                    // We try to prevent accidental disclosure of sensitive information.
                    Messages(er.message.takeWhile(_ != '{'))
                  }.mkString(",")
                ))
          })))
}