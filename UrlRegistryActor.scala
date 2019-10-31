package com.ngs.url


import akka.actor.{Actor, ActorLogging, Props}

final case class Url( url: String )
final case class Urls( urls: Seq[Url] )

object UrlRegistryActor {

  final case class GetUrlByShort( url: String )
  final case class GetUrlByShortResult( result: Option[String] )

  final case class GetUrl( url: String )
  final case class GetUrlResult( result: Option[Long] )

  final case class CreateUrl( url: String )
  final case class CreateUrlResult( result: String )

  def props: Props = Props[UrlRegistryActor]
}

class UrlRegistryActor extends Actor with Util with ActorLogging {
  import UrlRegistryActor._

  var urls = scala.collection.mutable.HashMap.empty[String, Long]
  var shortUrls = scala.collection.mutable.HashMap.empty[String, String]

  private[UrlRegistryActor] var id: Long = 0
  def nextId():Long = { id += 1; id }

  def receive: Receive = {
    case GetUrl( url ) =>
      val res =
        if ( !urls.contains( url ) ) GetUrlResult( None )
        else GetUrlResult( Some( urls( url ) ) )
      sender() !  res
    case CreateUrl( url ) =>
      val id = nextId
      urls( url ) = id
      shortUrls( urlEncode( id ) ) = url
      val newurl = s"${BaseConfig.httpHost}:${BaseConfig.httpPort}/${urlEncode( id )}"
      sender() ! CreateUrlResult( newurl )
    case GetUrlByShort( short ) =>
      val res =
        if ( !shortUrls.contains( short ) ) GetUrlResult( None )
        else GetUrlByShortResult( Some( shortUrls( short ) ) )
      sender() !  res


  }
}
//#user-registry-actor
