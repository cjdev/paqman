package com.cj.paqman

import java.util.UUID

import org.scalatest.FunSuite

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

class ServiceTest extends FunSuite {
  
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
    
    // when
    val result = Service.hunkHistories(record.history)
    
    // then
    assert(result.size === 1)
    assert(result(0).versions.size === 2)
  }
  
    
}
