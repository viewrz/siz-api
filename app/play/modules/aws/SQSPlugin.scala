package play.modules.aws

import play.api._
import com.kifi.franz._
import com.amazonaws.regions.Regions
import play.api.libs.json.JsValue

import scala.concurrent.ExecutionContext
import scala.util.control.NonFatal

class SQSPlugin(app: Application) extends Plugin {
  private var _helper: Option[SQSHelper] = None
  def helper = _helper.getOrElse(throw new RuntimeException("SQSPlugin error: no SQSHelper available?"))

  override def onStart {
    Logger info "SQS Plugin starting..."
    try {
      val conf = SQSPlugin.parseConf(app)
      _helper = Some(SQSHelper(conf, app))
      Logger.info("SQSPlugin successfully started with aws.access.key '%s'! "
        .format(
          conf.accessKey))
    } catch {
      case NonFatal(e) =>
        throw new PlayException("SQSPlugin Initialization Error", "An exception occurred while initializing the SQSPlugin.", e)
    }
  }
}

object SQSPlugin {
  case class AWSConf(accessKey: String, secretKey: String, region: Regions)

  private def client(implicit app: Application) = current.helper.client

  def json(queueConfigName: String)(implicit app: Application): SQSQueue[JsValue] = {
    app.configuration.getString(queueConfigName) match {
      case Some(queueName) =>
        client.json(QueueName(queueName))
      case _ =>
        throw new PlayException("SQSPlugin Error", "Queue %s don't exits in configuration")
    }
  }

  def current(implicit app: Application): SQSPlugin = app.plugin[SQSPlugin] match {
    case Some(plugin) => plugin
    case _            => throw new PlayException("SQSPlugin Error", "The SQSPlugin has not been initialized! Please edit your conf/play.plugins file and add the following line: '600:play.modules.aws.SQSPlugin' (600 is an arbitrary priority and may be changed to match your needs).")
  }

  private def parseConf(app: Application): AWSConf = {
    val region = app.configuration.getString("aws.region").getOrElse("us-west-2")
    (app.configuration.getString("aws.access.key"), app.configuration.getString("aws.secret.key")) match {
      case (Some(accessKey), Some(secretKey)) =>
        AWSConf(accessKey,secretKey,Regions.fromName(region))
      case _ =>
        throw app.configuration.globalError(s"Missing aws.access.key or aws.secret.key")
    }
  }
}

private[aws] case class SQSHelper(conf: SQSPlugin.AWSConf, app: Application) {
  implicit val ec: ExecutionContext = ExecutionContext.Implicits.global
  lazy val client = SimpleSQSClient(conf.accessKey, conf.secretKey, conf.region)
}