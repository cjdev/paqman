package com.cj.paqman

import java.util.UUID
import org.scalatest.FunSuite

class QualWithHistoryTest extends FunSuite {

  
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
    val result = new QualWithHistory(record).userStatus(user)
    
    // then
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
    val result = new QualWithHistory(record).userStatus(user)
    
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
    val result = new QualWithHistory(record).userStatus(user)
    
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
    val result = new QualWithHistory(record).userStatus(user)
    
    // then
    assert(result.wasCurrent === true)
    assert(result.isCurrent === false)
    assert(result.challenges ==  Seq(ChallengeStatus(
        challengeId=someOtherHunk.hunkId, name=someOtherHunk.name , hasPassed=false, isCurrent=false)))
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
}