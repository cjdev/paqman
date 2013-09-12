package com.cj.paqman

import java.io.File
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule

case class ConfigFile(val ldapUrl:String, val ldapUser:String, val ldapPassword:String)

object ConfigFile {
  val jackson = new ObjectMapper() with ScalaObjectMapper
  jackson.registerModule(DefaultScalaModule)
  
  def read(path:File):ConfigFile = jackson.readValue(path, classOf[ConfigFile])
}