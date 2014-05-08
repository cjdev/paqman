package com.cj.paqman

import com.fasterxml.jackson.databind.ObjectMapper
import org.httpobjects.Representation
import java.io.ByteArrayOutputStream
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule

object Jackson {
  val mapper = new ObjectMapper() with ScalaObjectMapper
  mapper.registerModule(DefaultScalaModule)
  
  def readJson[T](r:Representation)(implicit mf:Manifest[T]):T = {
    val bytes = new ByteArrayOutputStream()
    r.write(bytes);
    bytes.close();
    
    mapper.readValue(bytes.toByteArray());
  }
  
  def generate(o:AnyRef) = Jackson.mapper.writeValueAsString(o);
}