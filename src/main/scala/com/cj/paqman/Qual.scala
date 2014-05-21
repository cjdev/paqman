package com.cj.paqman

import java.util.UUID

case class Qual(id:String = UUID.randomUUID().toString, name:String, description:String, hunks:Seq[Hunk], administrator:String) {
  def hasPassed(userHistory:QualificationInfo):Boolean = {
    if(userHistory.id == id){
//      val challenges = 
      val allChallenges = hunks.filter(_.kind == "challenge")
      if(allChallenges.size==0){false}else{
          val meaningfulChallenges = allChallenges.filter(_.replacementInfo match {
          case None => true
          case Some(replacementInfo) => replacementInfo.isSignificantEdit
          })
          
          val idsOfMeaningfulChallenges = meaningfulChallenges.map(_.id).toSet
          val passedChallengeIds = idsOfMeaningfulChallenges.filter(userHistory.passedChallenges.contains)
          idsOfMeaningfulChallenges.size==passedChallengeIds.size
      }
    }else{
      false
    }
  }
  
  def hunkOrReplacmentForHunk(hunkId:String) = {
    val maybeMatchingHunk = hunks.find{h=>
      val isReplacement = h.replacementInfo match {
        case None => false
        case Some(replacementInfo)=>replacementInfo.replacesId == hunkId
      }
      val isHunk = h.id == hunkId
      isHunk || isReplacement
    }
    maybeMatchingHunk
  }
      
  
}

case class HunkReplacementInfo(
    isSignificantEdit:Boolean,
    replacesId:String)

case class Hunk(
    id:String = UUID.randomUUID().toString,
    kind:String,
    name:String,
    url:String = "",
    description:String = "",
    whenAdded:Long,
    replacementInfo:Option[HunkReplacementInfo]
){
  def updateFrom(hunk:Hunk, now:Long) = copy(id=UUID.randomUUID().toString, name=hunk.name, url = hunk.url, description = hunk.description, whenAdded = now, replacementInfo=hunk.replacementInfo)
  def toHunkInfo=HunkInfo(id=id, name=name)
}