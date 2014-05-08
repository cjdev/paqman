package com.cj.paqman

import java.util.UUID

case class Qual(val id:String = UUID.randomUUID().toString(), val name:String, val description:String, val hunks:Seq[Hunk], val administrator:String) {
  def hasPassed(userHistory:QualificationInfo):Boolean = {
    if(userHistory.id == id){
//      val challenges = 
      val allChallenges = hunks.filter(_.kind == "challenge");
      if(allChallenges.size==0){false}else{
          val meaningfulChallenges = allChallenges.filter(_.replacementInfo match {
          case None => true
          case Some(replacementInfo) => replacementInfo.isSignificantEdit
          });
          
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
    val isSignificantEdit:Boolean,
    val replacesId:String)

case class Hunk(
    val id:String = UUID.randomUUID().toString,
    val kind:String, 
    val name:String, 
    val url:String = "",
    val description:String = "",
    val whenAdded:Long,
    val replacementInfo:Option[HunkReplacementInfo]
){
  def updateFrom(hunk:Hunk, now:Long) = copy(id=UUID.randomUUID().toString(), name=hunk.name, url = hunk.url, description = hunk.description, whenAdded = now, replacementInfo=hunk.replacementInfo)
  def toHunkInfo=HunkInfo(id=id, name=name)
}