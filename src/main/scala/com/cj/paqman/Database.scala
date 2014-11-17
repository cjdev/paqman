package com.cj.paqman

import java.io.File
import org.apache.commons.io.FileUtils
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import java.util.UUID

case class RecordHistory(versions:Seq[String]){
  def latest:String = versions.last
}

trait Record[T] {
  def latest:T
  def history():Seq[T]
}

class Database[T <: AnyRef](val d:File, val t:Class[T]) extends DatabaseInterface[T]{
  val valuesDir = new File(d, "versions")
  val historiesDir = new File(d, "histories")
  valuesDir.mkdirs()
  historiesDir.mkdirs()
  val mapper = new ObjectMapper() with ScalaObjectMapper
  mapper.registerModule(DefaultScalaModule)
  
  private def historyPathForKey(key:String) = new File(historiesDir, key)
  private def valuePathForKey(key:String) = new File(valuesDir, key)
  
  private def generate(o:AnyRef) = {
    mapper.writeValueAsString(o)
  }
  
  def put(key:String, value:T) = this.synchronized {
    val newVersionId = UUID.randomUUID().toString
    
    val file = historyPathForKey(key)
    val existingHistory = if(file.exists()){
      mapper.readValue(file, classOf[RecordHistory])
    }else{
      RecordHistory(versions=Seq())
    }
    
    val newHistory = RecordHistory(existingHistory.versions :+ newVersionId)
    FileUtils.write(file, generate(newHistory))
    
    FileUtils.write(valuePathForKey(newVersionId), generate(value))
    
  }
  
  def size() = historiesDir.list().size
  
  def toSeq():Seq[Record[T]] = {
    val keys = historiesDir.listFiles().map(_.getName).toSeq
    keys.map{getHistory(_).get}
  }
  
  private class RecordImpl(val h:RecordHistory) extends Record[T] {
    private def readValue(id:String) = mapper.readValue(valuePathForKey(id), t)
    def latest:T  = readValue(h.latest)
    def history():Seq[T] = h.versions.map(readValue)
  }
  
  def get(key:String):Option[T] = getHistory(key) match {
    case Some(h) => Some(h.latest)
    case None => None
  }
  
   def getHistory(key:String):Option[Record[T]] = this.synchronized {
    val file = historyPathForKey(key)
    if(file.exists()){
      val history = mapper.readValue(file, classOf[RecordHistory]);
      val currentVersion = history.latest
      Some(new RecordImpl(history))
    }else{
      None
    }
  }
}