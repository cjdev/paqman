package com.cj.paqman

import org.junit.Test
import org.junit.Assert._

class ServiceTest {

  @Test
  def challengesAreStillPassedIfSubsequentEditsAreNotSignificant(){
    // given
    val service = new Service(null, null)
    val theHunkBefore = Hunk(kind="challenge", name="do it", description="like this", whenAdded=1, replacementInfo=None)
    val theHunkAfter = theHunkBefore.updateFrom(theHunkBefore.copy(name="do it faster", replacementInfo=Some(HunkReplacementInfo(isSignificantEdit=false, replacesId=theHunkBefore.id))), 2)
    
    val before = Qual(name = "foo", description="bar", hunks = Seq(theHunkBefore), administrator = "joe@schmoe.com")
    val after = before.copy(hunks=Seq(theHunkAfter))
    val record = new Record[Qual](){
      def latest=after;
      def history=Seq(after, before)
    }
    val q = QualificationInfo(id=before.id, passedChallenges=Set(theHunkBefore.id))
    val user = UserInfo(id="larry@company.com", qualifications=Seq(q))
    
    // when
    val result = service.userStatus(record, user)
    
    // then
    println("Result:" + Jackson.generate(result))
    assertEquals(true, result.wasCurrent)
    assertEquals(true, result.isCurrent)
    assertEquals(true, result.hasPassedSomeChallenges)
    assertEquals(true, result.passedChallenges.contains(HunkInfo(id=theHunkBefore.id, name=theHunkBefore.name)))
    
  }
}