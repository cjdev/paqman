package com.cj.paqman

import org.junit.Test
import org.junit.Assert._

class QualTest {

  @Test
  def usersWhoHaveNotPassedTheChallengeAreNotQualified(){
    // given
    val challenge = Hunk(kind="challenge", name="challenge a", description="A", whenAdded=1, replacementInfo=None)
    
    val qual = Qual(name = "foo", description="bar", hunks = Seq(challenge), administrator = "joe@schmoe.com")
    
    val q = QualificationInfo(id=qual.id, passedChallenges=Set())
    val user = UserInfo(id="larry@company.com", qualifications=Seq(q))
    
    // when
    val result = qual.hasPassed(q)
    
    // then
    assertEquals(false, result)
  }
}