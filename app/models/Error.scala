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

  // l'usage d'implicit implique que la classe appelante doit avoir un Lang, et le passera à cette méthode.
  def toTopLevelJson(validationErrors: Seq[(JsPath, Seq[ValidationError])])(implicit lang: Lang): JsValue =
    toTopLevelJson(
      Some(
        // marche avvec Either, qui est soit le type de gauche, soit le type de droite.
        Right(
          // tableau de Error
          validationErrors.map {
            error =>
              // dto maison
              Error(
                //classe de gestion des langues de Play, renvoie une string
                Messages(
                  "error.field",
                  error._1.toString(),
                  error._2.map { er =>
                    //classe de gestion des langues de Play, renvoie une string
                    Messages(er.message)
                  }.mkString(",")
                ))
          })))
}