package com.ngs.url

import com.typesafe.config.{Config, ConfigFactory}

object BaseConfig {
  def conf = ConfigFactory.load()
  def httpHost = conf.getString("akka.host")//"localhost"
  def httpPort = conf.getInt("akka.port")//9000

}
