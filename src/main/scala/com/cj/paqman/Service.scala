package com.cj.paqman

import org.joda.time.DateTime

class Service(val datas:DataInterface, val authMechanism:AuthMechanism) {
    def getUserWithCreateIfNeeded(emailAddress:String) = {
        datas.users.get(emailAddress) match{
        case None => if(authMechanism.emailExists(emailAddress)){
            val user =  UserInfo(id=emailAddress, events=List())
                    datas.users.put(emailAddress, user)
                    Some(user)
        }else{
            None
        }
        case Some(user)=> Some(user)
        }
    }
    
    
    def hunkHistories(versionsEarliestToLatest:Seq[Qual]) = {
      
      val headsOfEachHunkStreamEver = versionsEarliestToLatest.foldLeft(List[HunkVersion]()){(accum, qual)=>
        val hunksNotInAccum = qual.hunks.filter{h=> !accum.map(_.hunkId).contains(h.hunkId)}
        println(hunksNotInAccum.size + " new hunks in " + qual.id + " - " + qual.name +": " + hunksNotInAccum.map{h=>h.name + " added " +  new DateTime(h.whenAdded) + " " + h.hunkId  + " " + h.isSignificantEdit  })
        accum ::: hunksNotInAccum.toList
      }
      println("Looking for " + headsOfEachHunkStreamEver.size + " streams")
      val hunkHistories = headsOfEachHunkStreamEver.map{firstVersion=>
          val hunkHistory = versionsEarliestToLatest.foldLeft(List[HunkVersion](firstVersion)){(hunksSoFar, nextQualVersion) =>
            if(hunksSoFar.isEmpty) hunksSoFar
            else {
            	val latestSoFar = hunksSoFar.last
    			val maybeReplacement = nextQualVersion.hunks.find{h=> h.hunkId == latestSoFar.hunkId}
            	
            	maybeReplacement match {
            	case Some(replacement) => {
            		hunksSoFar :+ replacement
            	}
            	case None => hunksSoFar
            	}
            }
          }
          HunkHistory(hunkHistory.distinct)
      }
      
      hunkHistories
    }
    
    def getHunkHistory(qualId:String, h:HunkVersion): HunkHistory = getHunkHistory(qualId, h.hunkId)
    
    def getHunkHistory(qualId:String, hunkId:String): HunkHistory = {
      val qual = datas.qualifications.getHistory(qualId).get
      
      val histories = hunkHistories(versionsEarliestToLatest = qual.history)
      histories.find(_.latestVersion.hunkId ==hunkId).get
    }
    
    
    
    def userHasPassed(user:UserInfo, record:Record[Qual]):Boolean = userHasPassedThisVersion(user, record.latest, record)
    
    def userHasPassedThisVersion(user:UserInfo, qual:Qual, record:Record[Qual]) = {
      val priorHistory = record.history.takeWhile(!_.eq(qual))
      val h = priorHistory :+ qual
      val hunkHistoriesUpToThisPoint = this.hunkHistories(versionsEarliestToLatest = h)
      
      val hasPassedEachHunkAtThisPoint = hunkHistoriesUpToThisPoint.forall(_.isCurrent(user))
      hasPassedEachHunkAtThisPoint && hunkHistoriesUpToThisPoint.size>0
    }
    def userStatus(qual:Record[Qual], user:UserInfo):PersonStatus = userStatus(qual, hunkHistories(qual.history), user)
    def userStatus(qual:Record[Qual], hunkHistories:List[HunkHistory], user:UserInfo):PersonStatus = {
      val qualId = qual.latest.id
      val challenges = hunkHistories.filter(_.latestVersion.kind =="challenge")
      
      val currentChallenges = challenges.filter{hh=>
        	qual.latest.hunks.map(_.hunkId).contains(hh.hunkId)
      }
      val isCurrent = currentChallenges.forall(_.isCurrent(user))
      val isAdministrator = qual.latest.administrator == user.id
      val challengesStatus:Seq[ChallengeStatus] = currentChallenges.map{challenge=>
        println("The latest name for " + challenge.hunkId  + " is " + challenge.name)
        ChallengeStatus(
            challengeId=challenge.hunkId, 
            name=challenge.name,
            hasPassed=challenge.hasPassed(user), 
            isCurrent = challenge.isCurrent(user))
      }
      
      val wasCurrent = qual.history.exists(userHasPassedThisVersion(user, _, qual))
      
      PersonStatus(email=user.id, 
                   isAdministrator=isAdministrator, 
                   isCurrent=isCurrent, 
                   wasCurrent=wasCurrent, 
                   challenges=challengesStatus)
    }
}