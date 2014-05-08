package com.cj.paqman

import org.junit.Test
import org.junit.Assert._

class ServiceTest {
  class MockRecord[T](val history:T *) extends Record[T] {
      def latest=history.last;
  }
  
  @Test
  def detectsHistories(){
    // given
    val service = new Service(null, null)
    val challengeV1 = Hunk(kind="challenge", name="version 1", description="A", whenAdded=1, replacementInfo=None)
    val challengeV2 = challengeV1.updateFrom(challengeV1.copy(name="version 2", replacementInfo=Some(HunkReplacementInfo(isSignificantEdit=true, replacesId=challengeV1.id))), 2)
    val initial = Qual(name = "foo", description="bar", hunks = Seq(), administrator = "joe@schmoe.com")
    val second = initial.copy(hunks=Seq(challengeV1))
    val third = second.copy(hunks=Seq(challengeV2))
    val record = new MockRecord(initial, second, third)
    
    // when
    val result = service.hunkHistories(record)
    
    // then
    assertEquals(1, result.size)
    assertEquals(2, result(0).versions.size)
  }
  
  @Test
  def detectsStatusOfUsersWhoHaveLapsed(){
    // given
    val service = new Service(null, null)
    val challengeV1 = Hunk(kind="challenge", name="challenge a", description="A", whenAdded=1, replacementInfo=None)
    val challengeV2 = challengeV1.updateFrom(challengeV1.copy(name="el challenge", replacementInfo=Some(HunkReplacementInfo(isSignificantEdit=true, replacesId=challengeV1.id))), 2)
    
    val initial = Qual(name = "foo", description="bar", hunks = Seq(), administrator = "joe@schmoe.com")
    val second = initial.copy(hunks=Seq(challengeV1))
    val third = second.copy(hunks=Seq(challengeV2))
    val record = new MockRecord(initial, second, third)
    
    val q = QualificationInfo(id=initial.id, passedChallenges=Set(challengeV1.id))
    val user = UserInfo(id="larry@company.com", qualifications=Seq(q))
    
    // when
    val result = service.userStatus(record, user)
    
    // then
    assertEquals(true, result.wasCurrent)
    assertEquals(false, result.isCurrent)
    assertEquals(true, result.hasPassedSomeChallenges)
    assertEquals(Seq(), result.passedChallenges)
  }
  
  @Test
  def usersWhoHaveNeverCompletedAQualAreNotLapsed(){
    // given
    val service = new Service(null, null)
    val hunkA = Hunk(kind="challenge", name="challenge a", description="A", whenAdded=1, replacementInfo=None)
    val hunkB = Hunk(kind="challenge", name="challenge b", description="B", whenAdded=1, replacementInfo=None)
    
    val initial = Qual(name = "foo", description="bar", hunks = Seq(), administrator = "joe@schmoe.com")
    val second = initial.copy(hunks=Seq(hunkA, hunkB))
    val record = new MockRecord(initial, second)
    val q = QualificationInfo(id=initial.id, passedChallenges=Set())
    val user = UserInfo(id="larry@company.com", qualifications=Seq(q))
    
    // when
    val result = service.userStatus(record, user)
    
    // then
    assertEquals(false, result.wasCurrent)
    assertEquals(false, result.isCurrent)
    assertEquals(false, result.hasPassedSomeChallenges)
    assertEquals(Seq(), result.passedChallenges)
  }
  
  @Test
  def challengesAreStillPassedIfSubsequentEditsAreNotSignificant(){
    // given
    val service = new Service(null, null)
    val theHunkBefore = Hunk(kind="challenge", name="do it", description="like this", whenAdded=1, replacementInfo=None)
    val theHunkAfter = theHunkBefore.updateFrom(theHunkBefore.copy(name="do it faster", replacementInfo=Some(HunkReplacementInfo(isSignificantEdit=false, replacesId=theHunkBefore.id))), 2)
    
    val before = Qual(name = "foo", description="bar", hunks = Seq(theHunkBefore), administrator = "joe@schmoe.com")
    val after = before.copy(hunks=Seq(theHunkAfter))
    val record = new MockRecord(before, after)
    
    val q = QualificationInfo(id=before.id, passedChallenges=Set(theHunkBefore.id))
    val user = UserInfo(id="larry@company.com", qualifications=Seq(q))
    
    // when
    val result = service.userStatus(record, user)
    
    // then
    assertEquals(true, result.wasCurrent)
    assertEquals(true, result.isCurrent)
    assertEquals(true, result.hasPassedSomeChallenges)
    assertEquals(true, result.passedChallenges.contains(HunkInfo(id=theHunkBefore.id, name=theHunkBefore.name)))
  }
}