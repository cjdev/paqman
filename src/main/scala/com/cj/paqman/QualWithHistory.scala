package com.cj.paqman

class QualWithHistory(qual:Qual, record:Record[Qual]){
  def this(record:Record[Qual]) = this(record.latest, record)
  
  lazy val pastVersions = record.history.takeWhile(!_.eq(qual))
  lazy val allVersions = pastVersions :+ qual
  lazy val hunkHistoriesUpToThisPoint = Service.hunkHistories(versionsEarliestToLatest = allVersions)
  lazy val allChallengesEver = hunkHistoriesUpToThisPoint.filter(_.latestVersion.kind =="challenge")
  def currentChallenges = allChallengesEver.filter{hh=>
    	qual.hunks.map(_.hunkId).contains(hh.hunkId)
  }
  def isCurrent(user:UserInfo) = currentChallenges.forall(_.isCurrent(user)) && currentChallenges.size>0
  def wasCurrent(user:UserInfo) = allVersions.exists(new QualWithHistory(_, record).isCurrent(user))
  
  def userStatus(user:UserInfo):QualPersonStatus = {
      val isAdministrator = record.latest.administrator == user.id
      
      val challengesStatus = currentChallenges.map{challenge=>
        ChallengeStatus(
            challengeId=challenge.hunkId, 
            name=challenge.name,
            hasPassed=challenge.hasPassed(user), 
            isCurrent = challenge.isCurrent(user))
      }
      
      QualPersonStatus(qualId = qual.id,
                   email=user.id, 
                   isAdministrator=isAdministrator, 
                   isCurrent=isCurrent(user), 
                   wasCurrent=wasCurrent(user), 
                   challenges=challengesStatus)
    }
}