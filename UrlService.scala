package com.ngs.url

import scala.concurrent.{ExecutionContext, Future}
import scala.io.StdIn
import akka.actor.{ActorRef, ActorSystem}
import akka.stream.ActorMaterializer
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.{ServerBinding}

object UrlService extends App with RequestHandler  {

  implicit val system: ActorSystem = ActorSystem("ShortUrlServer")
  implicit val mat: ActorMaterializer = ActorMaterializer()
  implicit override val ec: ExecutionContext = system.dispatcher

  lazy val conf = BaseConfig.conf
  lazy val httpHost = conf.getString("akka.host")
  lazy val httpPort = conf.getInt("akka.port")
  println( s"httpHost ${httpHost}" )
  println( s"httpPort ${httpPort}" )


  val urlRegistryActor: ActorRef =
    system.actorOf( UrlRegistryActor.props, "urlRegistryActor")

  val serverBindingFuture: Future[ServerBinding] =
    Http().bindAndHandleAsync( requestHandler, httpHost, httpPort )

  println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
  StdIn.readLine()

  serverBindingFuture
    .flatMap( _.unbind() )
    .onComplete { done =>
      done.failed.map { ex => println(ex, "Failed unbinding") }
      system.terminate()
    }

}
