package com.ngs.url

import akka.actor.{ActorRef, ActorSystem}
import akka.event.Logging

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration._
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl._
import com.ngs.url.UrlRegistryActor.{CreateUrlResult}
import akka.http.scaladsl.model.HttpRequest
import akka.util.Timeout
import akka.pattern.ask
import  scala.util.{ Success, Failure }

trait RequestHandler extends HtmlContent with Util {

  implicit def system: ActorSystem
  def urlRegistryActor: ActorRef
  implicit def ec: ExecutionContext

  lazy val log = Logging(system, classOf[RequestHandler])
  implicit lazy val timeout = Timeout(5.seconds)
  def baseAddress = s"${BaseConfig.httpHost}:${BaseConfig.httpPort}"

  val requestHandler: HttpRequest => Future[HttpResponse] = {

    case HttpRequest( GET, a, b, c, d) =>
      val s = a.toString
      val suffix = if ( s.contains('/') ) s.split('/').last else ""
  //      val res: UrlRegistryActor.GetUrlByShortResult =
//        Await.result( (urlRegistryActor ? UrlRegistryActor.GetUrlByShort( suffix )), 2.seconds )
//          .asInstanceOf[UrlRegistryActor.GetUrlByShortResult]

//      if ( res.result.isDefined ) {
//        val urlRedirectUri = akka.http.javadsl.model.Uri.create(s"${baseAddress}/${suffix}/").asScala
//        Future(HttpResponse(status = StatusCodes.PermanentRedirect,
//                            headers = headers.Location(urlRedirectUri) :: Nil,
//          entity = StatusCodes.PermanentRedirect.htmlTemplate match {
//            case "" =>
//              HttpEntity.Empty
//            case template => HttpEntity(ContentTypes.`text/html(UTF-8)`, template format urlRedirectUri)
//          })
//        )
//      } else
        Future( HttpResponse(entity = HttpEntity(ContentTypes.`text/html(UTF-8)`, getDefault( "ShortUrl" ) ) ) )

    case HttpRequest( POST, a, b, c, d ) if ( a.toString.contains( s"${baseAddress}/url" ) ) =>
      val url = c.asInstanceOf[HttpEntity.Strict].data.utf8String.split("=") match {
        case arr if ( arr.length < 2 ) => ""
        case arr => java.net.URLDecoder.decode( arr.last, "UTF-8").trim
      }
      responseFilter( url )
    case _ =>
      Future( HttpResponse(entity = HttpEntity(ContentTypes.`text/html(UTF-8)`, getDefault( "ShortUrl" ) ) ) )
  }

  def responseFilter( url: String ): Future[HttpResponse] = {
    if (url.length == 0)
      Future(HttpResponse(entity = HttpEntity(ContentTypes.`text/html(UTF-8)`, getDefault("ShortUrl"))))
    else {
      val res: UrlRegistryActor.GetUrlResult = Await.result( (urlRegistryActor ? UrlRegistryActor.GetUrl( url )), 2.seconds ).asInstanceOf[UrlRegistryActor.GetUrlResult]
      val newurl =
      if ( res.result.isEmpty ) {
        val r:CreateUrlResult = Await.result( (urlRegistryActor ? UrlRegistryActor.CreateUrl( url )), 2.seconds).asInstanceOf[UrlRegistryActor.CreateUrlResult]
        r.result
      } else
        s"${BaseConfig.httpHost}:${BaseConfig.httpPort}/${urlEncode( res.result.get )}"

      Future(HttpResponse(entity = HttpEntity(ContentTypes.`text/html(UTF-8)`, getDefault("ShortUrl", url=newurl))))
    }
  }
}


