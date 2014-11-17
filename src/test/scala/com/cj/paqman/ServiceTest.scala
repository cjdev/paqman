package com.cj.paqman

import org.scalatest.FunSuite
import com.cj.paqman.HunkVersion
import java.util.UUID

class ServiceTest extends FunSuite {
  class MockRecord[T](val history:T *) extends Record[T] {
      def latest=history.last
  }
  
  private def randomUUID() = UUID.randomUUID().toString()
  
  private def newChallengeVersion(
    hunkId:String,
    name:String,
    url:String = "",
    description:String = "",
    whenAdded:Long,
    isSignificantEdit:Boolean) = {
       HunkVersion(hunkId=hunkId, 
                   versionId=randomUUID,
    		   		kind="challenge", name=name, url=url, description=description, whenAdded=whenAdded, isSignificantEdit=isSignificantEdit)
  }
  
  test("detects histories"){
    // given
    val service = new Service(null, null)
    val challengeV1 = newChallengeVersion(hunkId=randomUUID, name="version 1", description="A", whenAdded=1, isSignificantEdit=true)
    val challengeV2 = challengeV1.withUpdatesFromDto(challengeV1.toDto.copy(name="version 2", isSignificantEdit=true), 2)
    val initial = Qual(name = "foo", description="bar", hunks = Seq(), administrator = "joe@schmoe.com")
    val second = initial.copy(hunks=Seq(challengeV1))
    val third = second.copy(hunks=Seq(challengeV2))
    val record = new MockRecord(initial, second, third)
    
    println(record.history.zipWithIndex.mkString("", "\n", ""))
    // when
    val result = service.hunkHistories(record.history)
    
    // then
    assert(result.size === 1)
    println("VERSIONS:\n" + result(0).versions.mkString("\n"))
    assert(result(0).versions.size === 2)
  }
  
  test("detects status of users who have lapsed"){
    // given
    val challengeV1 = newChallengeVersion(hunkId=randomUUID, name="challenge a", description="A", whenAdded=1, isSignificantEdit=true)
    val challengeV2 = challengeV1.withUpdatesFromDto(challengeV1.toDto.copy(name="el challenge", isSignificantEdit=true), 2)
    
    val initial = Qual(name = "foo", description="bar", hunks = Seq(), administrator = "joe@schmoe.com")
    val second = initial.copy(hunks=Seq(challengeV1))
    val third = second.copy(hunks=Seq(challengeV2))
    val record = new MockRecord(initial, second, third)
    
    val q = ChallengePassedEvent(qualId=initial.id , challengeId=challengeV1.hunkId, challengeVersion=challengeV1.versionId , when=3)
    val user = UserInfo(id="larry@company.com", events=Seq(q))
    val service = new Service(datas = new DataInterface(){
        val qualifications = new DatabaseStub(Map(initial.id -> record))
        val users:DatabaseInterface[UserInfo] = null
        val sessions:DatabaseInterface[Session] = null
    }, null)
    
    // when
    val result = service.userStatus(record, user)
    
    // then
    println(Jackson.generate(result))
    assert(result.wasCurrent === true)
    assert(result.isCurrent === false)
    assert(result.challenges.filter(_.hasPassed).size > 0)
    assert(result.challenges.filter(_.isCurrent).size === 0)
  }
  
  test("users who have never completed a qual are not lapsed"){
    // given
    val hunkA = newChallengeVersion(hunkId=randomUUID, name="challenge a", description="A", whenAdded=1, isSignificantEdit=true)
    val hunkB = newChallengeVersion(hunkId=randomUUID, name="challenge b", description="B", whenAdded=1, isSignificantEdit=true)
    
    val initial = Qual(name = "foo", description="bar", hunks = Seq(), administrator = "joe@schmoe.com")
    val second = initial.copy(hunks=Seq(hunkA, hunkB))
    val record = new MockRecord(initial, second)
    val user = UserInfo(id="larry@company.com", events=Seq())
    val service = new Service(datas = new DataInterface(){
        val qualifications = new DatabaseStub(Map(initial.id -> record))
        val users:DatabaseInterface[UserInfo] = null
        val sessions:DatabaseInterface[Session] = null
    }, null)
    
    // when
    val result = service.userStatus(record, user)
    
    // then
    assert(result.wasCurrent === false)
    assert(result.isCurrent === false)
    assert(result.challenges.filter(_.hasPassed).size === 0)
    assert(result.challenges.filter(_.isCurrent).size === 0)
  }
  
  test("challenges are still passed if subsequent edits are not significant"){
    // given
    
    val theHunkBefore = newChallengeVersion(hunkId=randomUUID, name="do it", description="like this", whenAdded=1, isSignificantEdit=true)
    val theHunkAfter = theHunkBefore.withUpdatesFromDto(theHunkBefore.toDto.copy(name="do it faster", isSignificantEdit=false), 2)
    
    val before = Qual(name = "foo", description="bar", hunks = Seq(theHunkBefore), administrator = "joe@schmoe.com")
    val after = before.copy(hunks=Seq(theHunkAfter))
    val record = new MockRecord(before, after)
    
    val event = ChallengePassedEvent(qualId=before.id , challengeId=theHunkBefore.hunkId, challengeVersion=theHunkBefore.versionId , when=3)
    val user = UserInfo(id="larry@company.com", events=Seq(event))
    val service = new Service(datas = new DataInterface(){
        val qualifications = new DatabaseStub(Map(before.id -> record))
        val users:DatabaseInterface[UserInfo] = null
        val sessions:DatabaseInterface[Session] = null
    }, null)
    
    // when
    val result = service.userStatus(record, user)
    
    // then
    assert(result.wasCurrent === true)
    assert(result.isCurrent === true)
    assert(result.challenges.filter(_.hasPassed).size >0)
    assert(result.challenges.filter(_.isCurrent) === Seq(ChallengeStatus(
        challengeId=theHunkBefore.hunkId, name=theHunkAfter.name , hasPassed=true, isCurrent=true)))
  }
  
    test("users were current if they passed all the challenges that /used/ to exist, even if some have since been removed"){
    // given
    val theHunkThatUsedToBeThere = newChallengeVersion(hunkId=randomUUID, name="do it", description="like this", whenAdded=1, isSignificantEdit=true)
    val someOtherHunk =  newChallengeVersion(hunkId=randomUUID, name="do something else", description="like this", whenAdded=2, isSignificantEdit=true)
    
    val before = Qual(name = "foo", description="bar", hunks = Seq(theHunkThatUsedToBeThere), administrator = "joe@schmoe.com")
    val after = before.copy(hunks=Seq(someOtherHunk))
    val record = new MockRecord(before, after)
    
     val event = ChallengePassedEvent(qualId=before.id , challengeId=theHunkThatUsedToBeThere.hunkId, challengeVersion=theHunkThatUsedToBeThere.versionId , when=3)
    val user = UserInfo(id="larry@company.com", events=Seq(event))
    val service = new Service(datas = new DataInterface(){
        val qualifications = new DatabaseStub(Map(before.id -> record))
        val users:DatabaseInterface[UserInfo] = null
        val sessions:DatabaseInterface[Session] = null
    }, null)
    
    
    // when
    val result = service.userStatus(record, user)
    
    // then
    assert(result.wasCurrent === true)
    assert(result.isCurrent === false)
    assert(result.challenges ==  Seq(ChallengeStatus(
        challengeId=someOtherHunk.hunkId, name=someOtherHunk.name , hasPassed=false, isCurrent=false)))
  }
    
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
}
