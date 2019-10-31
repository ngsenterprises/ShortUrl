package com.ngs.url

import scala.collection.mutable.ListBuffer

trait Util {

  def shortUrlBase = s"${BaseConfig.httpHost}:${BaseConfig.httpPort}"

  val alphanum = "abcdefghijkmnopqrstuvwxyz23456789".toList
  val base = alphanum.length.toLong

  def urlEncode( n : Long ): String = {
    require( 0 <= n )

    if ( n == 0 ) alphanum.head.toString else {
      val buf = ListBuffer.empty[Char]
      var k = n
      while ( 0 < k ) {
        alphanum( (k % base).toInt ) +=: buf
        k = k / base
      }
      buf.mkString
    }
  }

  def urlDecode( s: String ): Option[Long] = {
    if ( !s.forall( alphanum.contains( _ ) ) ) None else
      Some( s.foldLeft( 0L ) { (ac, c) => ac * base + alphanum.indexOf(c).toLong } )
  }

  def decodeLongFromUrl( s: String ): Option[Long] = {
    val st = s.trim
    if ( st.startsWith( shortUrlBase ) && shortUrlBase.length < st.length ) {
      val split = st.split( "/" )
      val ext = if ( 1 < split.length ) split.last else ""

      if ( ext.forall( alphanum.contains( _ ) ) )
        urlDecode( ext )
      else
        None
    }
    else None
  }

}

