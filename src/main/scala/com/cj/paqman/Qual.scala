package com.cj.paqman

import java.util.UUID

case class Qual(val id:String = UUID.randomUUID().toString(), val name:String, val description:String, val hunks:Seq[Hunk], val administrator:String) {
  def hasPassed(userHistory:QualificationInfo):Boolean = {
    if(userHistory.id == id){
      val challenges = hunks.filter(_.kind == "challenge").map(_.id).toSet
      challenges.forall(userHistory.passedChallenges.contains)
    }else{
      false
    }
  }
}

case class Hunk(
    val id:String = UUID.randomUUID().toString,
    val kind:String, 
    val name:String, 
    val url:String = "",
    val description:String = "",
    val whenAdded:Long
){
  def updateFrom(hunk:Hunk, now:Long) = copy(id=UUID.randomUUID().toString(), name=hunk.name, url = hunk.url, description = hunk.description, whenAdded = now)
}