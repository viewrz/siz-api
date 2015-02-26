package formats

import models._
import play.api.libs.json._
import scala.language.implicitConversions

trait CommonJsonFormats {
   implicit def stringToError(s: String): Error = new Error(s)
   implicit def eitherWrites[A,B](implicit fma: Writes[A], fmb: Writes[B]): Writes[Either[A,B]] = new Writes[Either[A,B]] {
      def writes(o: Either[A, B]) = o match {
         case Left(value) => fma.writes(value)
         case Right(value) => fmb.writes(value)
      }
   }

   def typeWrites[T](w : Writes[T]): Writes[T] = {
      w.transform( js => js.as[JsObject] - "_type"  ++ Json.obj("type" ->  js \ "_type") )
   }

   def typeReads[T](r: Reads[T]): Reads[T] = {
      __.json.update((__ \ '_type).json.copyFrom((__ \ 'type).json.pick[JsString] )) andThen r
   }

   implicit val shortlistRead = Json.reads[Shortlist]
}
