package utils

import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.JsValue
import play.api.libs.ws._
import play.api.{Application, PlayException}
import play.modules.aws.SQSPlugin

object Queue {
  private val SQS_REGEX = "sqs://.*"
  private val POST_REGEX = "https?://.*"

  private def sendToSQSQueue(url: String, message: JsValue) = SQSPlugin.json(url.replace("sqs://","")).send(message).map {
    _ => true
  }.recover {
    case _ => false
  }

  private def sendByPOST(url: String, message: JsValue) = WS.url(url).post(message).map {
    response =>
      response.status >= 200 && response.status <= 300
  }

  def send(name: String, message: JsValue)(implicit app: Application) = {
    val configKey = s"queue.$name"
    app.configuration.getString(configKey) match {
      case Some(queueURI) if queueURI.matches(SQS_REGEX) =>
        sendToSQSQueue(queueURI,message)
      case Some(queueURI) if queueURI.matches(POST_REGEX) =>
        sendByPOST(queueURI,message)
      case _ =>
        throw new PlayException("QueuePlugin Error", s"Key $configKey don't exists in configuration")
    }
  }
}
