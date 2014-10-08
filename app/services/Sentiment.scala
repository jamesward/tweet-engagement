package services

import models.Tweet
import play.api.Application
import play.api.libs.json.JsValue
import play.api.libs.ws.WS

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object Sentiment {

  val url = "http://text-processing.com/api/sentiment/"

  def get(text: String)(implicit app: Application): Future[Int] = {
    WS.url(url).post(Map("text" -> Seq(text))).map { response =>
      val posPercent = (response.json \ "probability" \ "pos").as[Double]
      (posPercent * 100).toInt
    }
  }

}
