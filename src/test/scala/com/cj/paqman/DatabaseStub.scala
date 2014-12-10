package com.cj.paqman

class DatabaseStub[T <: AnyRef](initRecords:Map[String, MockRecord[T]]) extends DatabaseInterface[T] {
    private var recordsById = initRecords
    def put(key:String, value:T) {
      val record = recordsById.getOrElse(key, new MockRecord[T]())
      val newHistory = record.history.toList :+ value
      recordsById =recordsById + (key->new MockRecord(newHistory :_*))
    }
    def size() = recordsById.size
    def toSeq():Seq[Record[T]] = recordsById.values.toSeq
    def get(key:String):Option[T] = recordsById.get(key).map(_.latest)
    def getHistory(key:String):Option[Record[T]] = recordsById.get(key)
}