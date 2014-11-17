package com.cj.paqman

trait DatabaseInterface[T <: AnyRef]{

  def put(key:String, value:T)
  def size():Int
  def toSeq():Seq[Record[T]]
  def get(key:String):Option[T]
  def getHistory(key:String):Option[Record[T]]
}